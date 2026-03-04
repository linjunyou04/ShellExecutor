package com.shellexecutor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 脚本数据模型类
 * 用于存储脚本的基本信息，包括名称、路径和唯一标识符
 */
public class Script implements Serializable {
    private String id;
    private String name;
    private String path;
    private long createdAt;
    private long lastExecutedAt;

    /**
     * 默认构造函数，自动生成唯一ID和创建时间
     */
    public Script() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.lastExecutedAt = 0;
    }

    /**
     * 带参数的构造函数
     * @param name 脚本名称
     * @param path 脚本文件路径
     */
    public Script(String name, String path) {
        this();
        this.name = name;
        this.path = path;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastExecutedAt() {
        return lastExecutedAt;
    }

    public void setLastExecutedAt(long lastExecutedAt) {
        this.lastExecutedAt = lastExecutedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Script script = (Script) obj;
        return id != null && id.equals(script.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Script{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", createdAt=" + createdAt +
                ", lastExecutedAt=" + lastExecutedAt +
                '}';
    }
}
