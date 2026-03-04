package com.shellexecutor;

import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Root Shell执行器
 * 用于在Root权限下执行Shell命令和脚本
 * 支持异步执行和实时输出
 */
public class RootShell {
    private static final String TAG = "RootShell";
    
    private Process rootProcess;
    private DataOutputStream outputStream;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private ExecutorService executorService;
    private AtomicBoolean isRunning;
    private OutputListener outputListener;
    
    /**
     * 输出监听器接口
     */
    public interface OutputListener {
        /**
         * 当有新输出时调用
         * @param output 输出内容
         * @param isError 是否为错误输出
         */
        void onOutput(String output, boolean isError);
        
        /**
         * 当执行完成时调用
         * @param exitCode 退出码
         */
        void onComplete(int exitCode);
    }
    
    /**
     * 构造函数
     */
    public RootShell() {
        this.executorService = Executors.newCachedThreadPool();
        this.isRunning = new AtomicBoolean(false);
    }
    
    /**
     * 检查设备是否已获取Root权限
     * @return 如果有Root权限返回true
     */
    public static boolean hasRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            Log.e(TAG, "Root access check failed", e);
            return false;
        }
    }
    
    /**
     * 启动Root Shell会话
     * @return 启动成功返回true
     */
    public boolean startShell() {
        if (isRunning.get()) {
            return true;
        }
        
        try {
            rootProcess = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(rootProcess.getOutputStream());
            inputReader = new BufferedReader(new InputStreamReader(rootProcess.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(rootProcess.getErrorStream()));
            isRunning.set(true);
            
            // 启动输出读取线程
            startOutputReader();
            
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to start root shell", e);
            return false;
        }
    }
    
    /**
     * 启动输出读取线程
     */
    private void startOutputReader() {
        // 读取标准输出
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while (isRunning.get() && (line = inputReader.readLine()) != null) {
                        if (outputListener != null) {
                            outputListener.onOutput(line, false);
                        }
                    }
                } catch (IOException e) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Error reading stdout", e);
                    }
                }
            }
        });
        
        // 读取错误输出
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while (isRunning.get() && (line = errorReader.readLine()) != null) {
                        if (outputListener != null) {
                            outputListener.onOutput(line, true);
                        }
                    }
                } catch (IOException e) {
                    if (isRunning.get()) {
                        Log.e(TAG, "Error reading stderr", e);
                    }
                }
            }
        });
    }
    
    /**
     * 执行命令
     * @param command 要执行的命令
     */
    public void executeCommand(String command) {
        if (!isRunning.get()) {
            if (!startShell()) {
                if (outputListener != null) {
                    outputListener.onOutput("无法获取Root权限", true);
                    outputListener.onComplete(-1);
                }
                return;
            }
        }
        
        try {
            outputStream.writeBytes(command + "\n");
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to execute command", e);
            if (outputListener != null) {
                outputListener.onOutput("命令执行失败: " + e.getMessage(), true);
            }
        }
    }
    
    /**
     * 执行脚本文件
     * @param scriptPath 脚本文件路径
     */
    public void executeScript(String scriptPath) {
        File scriptFile = new File(scriptPath);
        
        if (!scriptFile.exists()) {
            if (outputListener != null) {
                outputListener.onOutput("脚本文件不存在: " + scriptPath, true);
                outputListener.onComplete(-1);
            }
            return;
        }
        
        // 先添加执行权限
        executeCommand("chmod +x " + scriptPath);
        
        // 执行脚本
        executeCommand(scriptPath);
        
        // 标记执行完成
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500); // 等待输出完成
                    if (outputListener != null) {
                        outputListener.onComplete(0);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }
    
    /**
     * 关闭Shell会话
     */
    public void closeShell() {
        isRunning.set(false);
        
        try {
            if (outputStream != null) {
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing output stream", e);
        }
        
        try {
            if (inputReader != null) {
                inputReader.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing input reader", e);
        }
        
        try {
            if (errorReader != null) {
                errorReader.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing error reader", e);
        }
        
        if (rootProcess != null) {
            rootProcess.destroy();
        }
        
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
    
    /**
     * 强制停止当前执行
     */
    public void forceStop() {
        if (isRunning.get()) {
            executeCommand("exit");
            closeShell();
        }
    }
    
    /**
     * 设置输出监听器
     * @param listener 输出监听器
     */
    public void setOutputListener(OutputListener listener) {
        this.outputListener = listener;
    }
    
    /**
     * 检查Shell是否正在运行
     * @return 如果正在运行返回true
     */
    public boolean isRunning() {
        return isRunning.get();
    }
    
    /**
     * 同步执行命令并获取输出
     * @param command 要执行的命令
     * @param timeout 超时时间（毫秒）
     * @return 命令输出结果
     */
    public static ExecutionResult executeCommandSync(String command, long timeout) {
        ExecutionResult result = new ExecutionResult();
        StringBuilder output = new StringBuilder();
        StringBuilder error = new StringBuilder();
        
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});
            
            BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));
            
            String line;
            while ((line = inputReader.readLine()) != null) {
                output.append(line).append("\n");
            }
            
            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }
            
            process.waitFor();
            result.exitCode = process.exitValue();
            result.output = output.toString();
            result.error = error.toString();
            
            inputReader.close();
            errorReader.close();
            process.destroy();
            
        } catch (Exception e) {
            result.exitCode = -1;
            result.error = e.getMessage();
        }
        
        return result;
    }
    
    /**
     * 执行结果类
     */
    public static class ExecutionResult {
        public int exitCode;
        public String output;
        public String error;
        
        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}
