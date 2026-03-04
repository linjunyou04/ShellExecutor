package com.shellexecutor;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.File;
import java.util.List;

/**
 * 主Activity
 * 显示脚本列表，提供添加、编辑、删除和执行脚本的功能
 */
public class MainActivity extends AppCompatActivity implements ScriptAdapter.OnScriptActionListener {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_TERMINAL = 2001;
    
    private ScriptManager scriptManager;
    private ScriptAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private LinearLayout rootStatusLayout;
    private TextView rootStatusText;
    private FloatingActionButton addScriptFab;
    
    private boolean hasRootAccess = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化视图
        initViews();
        
        // 初始化脚本管理器
        scriptManager = new ScriptManager(this);
        
        // 设置RecyclerView
        setupRecyclerView();
        
        // 检查权限
        checkPermissions();
        
        // 检查Root权限
        checkRootAccess();
        
        // 设置添加按钮点击事件
        addScriptFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddScriptDialog(null);
            }
        });
    }
    
    /**
     * 初始化视图组件
     */
    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        recyclerView = findViewById(R.id.scriptsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        rootStatusLayout = findViewById(R.id.rootStatusLayout);
        rootStatusText = findViewById(R.id.rootStatusText);
        addScriptFab = findViewById(R.id.addScriptFab);
    }
    
    /**
     * 设置RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new ScriptAdapter(scriptManager.getAllScripts(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        updateEmptyState();
    }
    
    /**
     * 检查存储权限
     */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    /**
     * 检查Root权限
     */
    private void checkRootAccess() {
        rootStatusText.setText(R.string.requesting_root);
        rootStatusLayout.setBackgroundColor(getResources().getColor(R.color.warning));
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasRootAccess = RootShell.hasRootAccess();
                
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (hasRootAccess) {
                            rootStatusText.setText(R.string.root_granted);
                            rootStatusLayout.setBackgroundColor(getResources().getColor(R.color.success));
                        } else {
                            rootStatusText.setText(R.string.no_root);
                            rootStatusLayout.setBackgroundColor(getResources().getColor(R.color.error_color));
                        }
                    }
                });
            }
        }).start();
    }
    
    /**
     * 更新空状态显示
     */
    private void updateEmptyState() {
        if (scriptManager.getScriptCount() == 0) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * 显示添加/编辑脚本对话框
     * @param scriptToEdit 要编辑的脚本，如果为null则表示添加新脚本
     */
    private void showAddScriptDialog(final Script scriptToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_script, null);
        builder.setView(dialogView);
        
        final TextInputEditText nameInput = dialogView.findViewById(R.id.scriptNameInput);
        final TextInputEditText pathInput = dialogView.findViewById(R.id.scriptPathInput);
        final MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);
        final MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        
        // 如果是编辑模式，填充现有数据
        if (scriptToEdit != null) {
            nameInput.setText(scriptToEdit.getName());
            pathInput.setText(scriptToEdit.getPath());
        }
        
        final AlertDialog dialog = builder.create();
        dialog.show();
        
        // 保存按钮点击事件
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String path = pathInput.getText().toString().trim();
                
                // 验证输入
                if (TextUtils.isEmpty(name)) {
                    nameInput.setError(getString(R.string.script_name_empty));
                    return;
                }
                
                if (TextUtils.isEmpty(path)) {
                    pathInput.setError(getString(R.string.script_path_empty));
                    return;
                }
                
                // 检查名称是否已存在
                String excludeId = scriptToEdit != null ? scriptToEdit.getId() : null;
                if (scriptManager.isNameExists(name, excludeId)) {
                    nameInput.setError("脚本名称已存在");
                    return;
                }
                
                // 保存脚本
                if (scriptToEdit != null) {
                    // 编辑模式
                    scriptToEdit.setName(name);
                    scriptToEdit.setPath(path);
                    scriptManager.updateScript(scriptToEdit);
                    Toast.makeText(MainActivity.this, R.string.script_saved, Toast.LENGTH_SHORT).show();
                } else {
                    // 添加模式
                    Script newScript = new Script(name, path);
                    if (scriptManager.addScript(newScript)) {
                        Toast.makeText(MainActivity.this, R.string.script_saved, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                // 刷新列表
                adapter.updateScripts(scriptManager.getAllScripts());
                updateEmptyState();
                dialog.dismiss();
            }
        });
        
        // 取消按钮点击事件
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    
    /**
     * 显示删除确认对话框
     * @param script 要删除的脚本
     */
    private void showDeleteConfirmDialog(final Script script) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        scriptManager.deleteScript(script.getId());
                        adapter.updateScripts(scriptManager.getAllScripts());
                        updateEmptyState();
                        Toast.makeText(MainActivity.this, R.string.script_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
    
    // ========== ScriptAdapter.OnScriptActionListener 接口实现 ==========
    
    @Override
    public void onExecute(Script script) {
        if (!hasRootAccess) {
            Toast.makeText(this, R.string.no_root, Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 更新最后执行时间
        scriptManager.updateLastExecutedTime(script.getId());
        
        // 启动终端Activity
        Intent intent = new Intent(this, TerminalActivity.class);
        intent.putExtra("script", script);
        startActivityForResult(intent, REQUEST_TERMINAL);
    }
    
    @Override
    public void onEdit(Script script) {
        showAddScriptDialog(script);
    }
    
    @Override
    public void onDelete(Script script) {
        showDeleteConfirmDialog(script);
    }
    
    // ========== 权限请求结果 ==========
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // 权限请求结果处理
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 刷新列表
        adapter.updateScripts(scriptManager.getAllScripts());
        updateEmptyState();
    }
}
