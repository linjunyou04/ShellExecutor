package com.shellexecutor;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 终端Activity
 * 提供交互式终端界面，显示脚本执行输出，支持命令输入
 */
public class TerminalActivity extends AppCompatActivity implements RootShell.OutputListener {
    
    private Script script;
    private RootShell rootShell;
    
    private TextView scriptNameText;
    private TextView scriptPathText;
    private TextView outputText;
    private ScrollView outputScrollView;
    private EditText commandInput;
    private ImageButton sendButton;
    private ImageButton backButton;
    private ImageButton clearButton;
    private ImageButton stopButton;
    private LinearLayout executingIndicator;
    
    private StringBuilder outputBuilder;
    private SimpleDateFormat timeFormat;
    private List<String> commandHistory;
    private int historyIndex = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        
        // 获取传递的脚本对象
        script = (Script) getIntent().getSerializableExtra("script");
        if (script == null) {
            Toast.makeText(this, "脚本数据错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 初始化
        initViews();
        initData();
        setupListeners();
        
        // 初始化Root Shell
        initRootShell();
        
        // 开始执行脚本
        executeScript();
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        scriptNameText = findViewById(R.id.scriptNameText);
        scriptPathText = findViewById(R.id.scriptPathText);
        outputText = findViewById(R.id.outputText);
        outputScrollView = findViewById(R.id.outputScrollView);
        commandInput = findViewById(R.id.commandInput);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        clearButton = findViewById(R.id.clearButton);
        stopButton = findViewById(R.id.stopButton);
        executingIndicator = findViewById(R.id.executingIndicator);
    }
    
    /**
     * 初始化数据
     */
    private void initData() {
        outputBuilder = new StringBuilder();
        timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        commandHistory = new ArrayList<>();
        
        // 显示脚本信息
        scriptNameText.setText(script.getName());
        scriptPathText.setText(script.getPath());
    }
    
    /**
     * 设置监听器
     */
    private void setupListeners() {
        // 返回按钮
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        // 清屏按钮
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                outputBuilder.setLength(0);
                outputText.setText("");
            }
        });
        
        // 停止按钮
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rootShell != null && rootShell.isRunning()) {
                    rootShell.forceStop();
                    appendOutput("已停止执行", true);
                    executingIndicator.setVisibility(View.GONE);
                }
            }
        });
        
        // 发送按钮
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand();
            }
        });
        
        // 输入框回车事件
        commandInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && 
                     event.getAction() == KeyEvent.ACTION_DOWN)) {
                    sendCommand();
                    return true;
                }
                return false;
            }
        });
        
        // 命令历史导航（上下键）
        commandInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        navigateHistory(-1);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        navigateHistory(1);
                        return true;
                    }
                }
                return false;
            }
        });
    }
    
    /**
     * 初始化Root Shell
     */
    private void initRootShell() {
        rootShell = new RootShell();
        rootShell.setOutputListener(this);
    }
    
    /**
     * 执行脚本
     */
    private void executeScript() {
        appendOutput("开始执行脚本: " + script.getName(), false);
        appendOutput("路径: " + script.getPath(), false);
        appendOutput("时间: " + timeFormat.format(new Date()), false);
        appendOutput("────────────────────────────────────", false);
        appendOutput("", false);
        
        executingIndicator.setVisibility(View.VISIBLE);
        
        if (rootShell.startShell()) {
            rootShell.executeScript(script.getPath());
        } else {
            appendOutput("无法获取Root权限", true);
            executingIndicator.setVisibility(View.GONE);
        }
    }
    
    /**
     * 发送命令
     */
    private void sendCommand() {
        String command = commandInput.getText().toString().trim();
        if (command.isEmpty()) {
            return;
        }
        
        // 添加到历史记录
        commandHistory.add(command);
        historyIndex = commandHistory.size();
        
        // 显示命令
        appendOutput("$ " + command, false);
        
        // 执行命令
        if (rootShell != null && rootShell.isRunning()) {
            rootShell.executeCommand(command);
        } else {
            if (rootShell.startShell()) {
                rootShell.executeCommand(command);
            } else {
                appendOutput("无法获取Root权限", true);
            }
        }
        
        // 清空输入框
        commandInput.setText("");
    }
    
    /**
     * 导航命令历史
     * @param direction 方向，-1为上一条，1为下一条
     */
    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) {
            return;
        }
        
        historyIndex += direction;
        
        if (historyIndex < 0) {
            historyIndex = 0;
        } else if (historyIndex >= commandHistory.size()) {
            historyIndex = commandHistory.size();
            commandInput.setText("");
            return;
        }
        
        commandInput.setText(commandHistory.get(historyIndex));
        commandInput.setSelection(commandInput.getText().length());
    }
    
    /**
     * 追加输出内容
     * @param text 输出文本
     * @param isError 是否为错误信息
     */
    private void appendOutput(String text, boolean isError) {
        String timestamp = timeFormat.format(new Date());
        
        if (isError) {
            outputBuilder.append("[").append(timestamp).append("] [ERROR] ")
                    .append(text).append("\n");
        } else {
            outputBuilder.append("[").append(timestamp).append("] ")
                    .append(text).append("\n");
        }
        
        // 更新UI
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                outputText.setText(outputBuilder.toString());
                
                // 滚动到底部
                outputScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        outputScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }
    
    // ========== RootShell.OutputListener 接口实现 ==========
    
    @Override
    public void onOutput(final String output, final boolean isError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isError) {
                    appendOutput(output, true);
                } else {
                    appendOutput(output, false);
                }
            }
        });
    }
    
    @Override
    public void onComplete(final int exitCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                executingIndicator.setVisibility(View.GONE);
                appendOutput("", false);
                appendOutput("────────────────────────────────────", false);
                appendOutput("脚本执行完成，退出码: " + exitCode, exitCode != 0);
                
                if (exitCode == 0) {
                    Toast.makeText(TerminalActivity.this, R.string.script_completed, 
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TerminalActivity.this, R.string.script_failed, 
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rootShell != null) {
            rootShell.closeShell();
        }
    }
    
    @Override
    public void onBackPressed() {
        // 如果脚本正在执行，询问是否退出
        if (executingIndicator.getVisibility() == View.VISIBLE) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("确认退出")
                    .setMessage("脚本正在执行中，确定要退出吗？")
                    .setPositiveButton("退出", (dialog, which) -> {
                        if (rootShell != null) {
                            rootShell.forceStop();
                        }
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
