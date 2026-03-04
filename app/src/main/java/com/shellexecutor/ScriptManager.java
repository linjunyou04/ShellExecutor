package com.shellexecutor;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 脚本管理器
 * 负责脚本的持久化存储、读取和管理
 * 使用SharedPreferences和Gson进行JSON序列化
 */
public class ScriptManager {
    private static final String PREFS_NAME = "ShellExecutorPrefs";
    private static final String KEY_SCRIPTS = "scripts";
    
    private final SharedPreferences sharedPreferences;
    private final Gson gson;
    private List<Script> scripts;
    
    /**
     * 构造函数
     * @param context 应用上下文
     */
    public ScriptManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadScripts();
    }
    
    /**
     * 从SharedPreferences加载脚本列表
     */
    private void loadScripts() {
        String json = sharedPreferences.getString(KEY_SCRIPTS, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<Script>>() {}.getType();
            scripts = gson.fromJson(json, type);
            if (scripts == null) {
                scripts = new ArrayList<>();
            }
        } else {
            scripts = new ArrayList<>();
        }
    }
    
    /**
     * 保存脚本列表到SharedPreferences
     */
    private void saveScripts() {
        String json = gson.toJson(scripts);
        sharedPreferences.edit().putString(KEY_SCRIPTS, json).apply();
    }
    
    /**
     * 获取所有脚本
     * @return 脚本列表（按创建时间倒序排列）
     */
    public List<Script> getAllScripts() {
        List<Script> sortedList = new ArrayList<>(scripts);
        Collections.sort(sortedList, new Comparator<Script>() {
            @Override
            public int compare(Script s1, Script s2) {
                return Long.compare(s2.getCreatedAt(), s1.getCreatedAt());
            }
        });
        return sortedList;
    }
    
    /**
     * 根据ID获取脚本
     * @param id 脚本ID
     * @return 脚本对象，如果不存在则返回null
     */
    public Script getScriptById(String id) {
        for (Script script : scripts) {
            if (script.getId().equals(id)) {
                return script;
            }
        }
        return null;
    }
    
    /**
     * 添加新脚本
     * @param script 要添加的脚本
     * @return 添加成功返回true，如果已存在同名脚本则返回false
     */
    public boolean addScript(Script script) {
        // 检查是否已存在同名脚本
        for (Script existingScript : scripts) {
            if (existingScript.getName().equals(script.getName())) {
                return false;
            }
        }
        scripts.add(script);
        saveScripts();
        return true;
    }
    
    /**
     * 更新脚本信息
     * @param script 要更新的脚本
     * @return 更新成功返回true，如果脚本不存在则返回false
     */
    public boolean updateScript(Script script) {
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).getId().equals(script.getId())) {
                scripts.set(i, script);
                saveScripts();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 删除脚本
     * @param id 要删除的脚本ID
     * @return 删除成功返回true，如果脚本不存在则返回false
     */
    public boolean deleteScript(String id) {
        for (int i = 0; i < scripts.size(); i++) {
            if (scripts.get(i).getId().equals(id)) {
                scripts.remove(i);
                saveScripts();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 更新脚本的最后执行时间
     * @param id 脚本ID
     */
    public void updateLastExecutedTime(String id) {
        Script script = getScriptById(id);
        if (script != null) {
            script.setLastExecutedAt(System.currentTimeMillis());
            saveScripts();
        }
    }
    
    /**
     * 获取脚本数量
     * @return 脚本数量
     */
    public int getScriptCount() {
        return scripts.size();
    }
    
    /**
     * 清空所有脚本
     */
    public void clearAllScripts() {
        scripts.clear();
        saveScripts();
    }
    
    /**
     * 检查脚本名称是否已存在
     * @param name 脚本名称
     * @param excludeId 排除的脚本ID（用于编辑时排除自身）
     * @return 如果名称已存在返回true
     */
    public boolean isNameExists(String name, String excludeId) {
        for (Script script : scripts) {
            if (script.getName().equals(name) && !script.getId().equals(excludeId)) {
                return true;
            }
        }
        return false;
    }
}
