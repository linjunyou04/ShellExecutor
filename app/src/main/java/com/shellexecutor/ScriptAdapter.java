package com.shellexecutor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 脚本列表适配器
 * 用于在RecyclerView中显示脚本列表
 */
public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ScriptViewHolder> {
    
    private List<Script> scripts;
    private OnScriptActionListener listener;
    private SimpleDateFormat dateFormat;
    
    /**
     * 脚本操作监听器接口
     */
    public interface OnScriptActionListener {
        void onExecute(Script script);
        void onEdit(Script script);
        void onDelete(Script script);
    }
    
    /**
     * 构造函数
     * @param scripts 脚本列表
     * @param listener 操作监听器
     */
    public ScriptAdapter(List<Script> scripts, OnScriptActionListener listener) {
        this.scripts = scripts;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ScriptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_script, parent, false);
        return new ScriptViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ScriptViewHolder holder, int position) {
        Script script = scripts.get(position);
        holder.bind(script);
    }
    
    @Override
    public int getItemCount() {
        return scripts != null ? scripts.size() : 0;
    }
    
    /**
     * 更新脚本列表
     * @param newScripts 新的脚本列表
     */
    public void updateScripts(List<Script> newScripts) {
        this.scripts = newScripts;
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder类
     */
    class ScriptViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView pathTextView;
        private MaterialButton editButton;
        private MaterialButton deleteButton;
        private MaterialButton executeButton;
        
        ScriptViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.scriptNameTextView);
            pathTextView = itemView.findViewById(R.id.scriptPathTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            executeButton = itemView.findViewById(R.id.executeButton);
        }
        
        void bind(final Script script) {
            // 设置脚本名称
            nameTextView.setText(script.getName());
            
            // 设置脚本路径
            pathTextView.setText(script.getPath());
            
            // 设置执行按钮点击事件
            executeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onExecute(script);
                    }
                }
            });
            
            // 设置编辑按钮点击事件
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onEdit(script);
                    }
                }
            });
            
            // 设置删除按钮点击事件
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDelete(script);
                    }
                }
            });
            
            // 长按显示详细信息
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showScriptInfo(script);
                    return true;
                }
            });
        }
        
        private void showScriptInfo(Script script) {
            StringBuilder info = new StringBuilder();
            info.append("脚本名称: ").append(script.getName()).append("\n");
            info.append("脚本路径: ").append(script.getPath()).append("\n");
            info.append("创建时间: ").append(formatDate(script.getCreatedAt())).append("\n");
            
            if (script.getLastExecutedAt() > 0) {
                info.append("最后执行: ").append(formatDate(script.getLastExecutedAt()));
            } else {
                info.append("最后执行: 从未执行");
            }
            
            // 这里可以显示一个对话框，暂时用Toast替代
            // Toast.makeText(itemView.getContext(), info.toString(), Toast.LENGTH_LONG).show();
        }
        
        private String formatDate(long timestamp) {
            return dateFormat.format(new Date(timestamp));
        }
    }
}
