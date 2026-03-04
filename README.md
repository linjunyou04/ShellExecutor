# Shell执行器 (Shell Executor)

<p align="center">
  <img src="https://img.shields.io/badge/Android-7.0%2B-green" alt="Android Version">
  <img src="https://img.shields.io/badge/API-24%2B-blue" alt="API Level">
  <img src="https://img.shields.io/badge/License-MIT-orange" alt="License">
</p>

一款专为Root用户设计的Android Shell脚本执行器，支持脚本管理、一键执行和交互式终端操作。

## ✨ 功能特性

- 🔐 **Root权限支持** - 自动检测并请求Root权限
- 📝 **脚本管理** - 添加、编辑、删除Shell脚本
- 🚀 **一键执行** - 保存脚本后点击按钮即可执行
- 💻 **交互式终端** - 实时显示输出，支持命令输入
- 📜 **命令历史** - 上下键快速切换历史命令
- 🎨 **现代UI** - Material Design风格界面

## 📱 截图

| 主界面 | 终端界面 |
|:---:|:---:|
| 脚本列表管理 | 实时输出显示 |

## 🔧 系统要求

- Android 7.0 (API 24) 或更高版本
- 已Root的Android设备
- 存储权限（用于读取脚本文件）

## 📥 安装

### 方式一：从Releases下载

1. 前往 [Releases](../../releases) 页面
2. 下载最新的 `app-debug.apk`
3. 安装到您的设备

### 方式二：自行编译

```bash
# 克隆仓库
git clone https://github.com/YOUR_USERNAME/ShellExecutor.git
cd ShellExecutor

# 编译Debug版本
./gradlew assembleDebug

# APK位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 📖 使用说明

### 1. 添加脚本

1. 点击右下角的 **+** 按钮
2. 输入脚本名称（如：清理缓存）
3. 输入脚本路径（如：`/sdcard/clean.sh`）
4. 点击保存

### 2. 执行脚本

1. 在脚本列表中找到要执行的脚本
2. 点击 **执行** 按钮
3. 终端界面会自动打开并显示输出

### 3. 交互式操作

在终端界面中：
- 输入框可输入额外命令
- 点击发送按钮或回车执行
- 上下键可切换历史命令
- 清屏按钮清除输出
- 停止按钮终止执行

## 📝 脚本示例

### 清理缓存脚本 (`/sdcard/clean.sh`)

```bash
#!/system/bin/sh
echo "开始清理缓存..."
rm -rf /data/local/tmp/*
rm -rf /sdcard/Android/data/*/cache/*
echo "缓存清理完成"
```

### 系统信息脚本 (`/sdcard/sysinfo.sh`)

```bash
#!/system/bin/sh
echo "=== 系统信息 ==="
echo "设备: $(getprop ro.product.model)"
echo "Android版本: $(getprop ro.build.version.release)"
echo "内核版本: $(uname -r)"
echo "CPU: $(cat /proc/cpuinfo | grep 'model name' | head -1)"
echo "内存: $(free -h | grep Mem | awk '{print $2}')"
```

### 备份应用数据 (`/sdcard/backup.sh`)

```bash
#!/system/bin/sh
PKG=$1
if [ -z "$PKG" ]; then
    echo "用法: $0 <包名>"
    exit 1
fi
echo "备份 $PKG ..."
tar -czf /sdcard/backup/${PKG}_$(date +%Y%m%d).tar.gz /data/data/$PKG
echo "备份完成"
```

## 🏗️ 项目结构

```
ShellExecutor/
├── app/
│   ├── src/main/
│   │   ├── java/com/shellexecutor/
│   │   │   ├── MainActivity.java      # 主界面
│   │   │   ├── TerminalActivity.java  # 终端界面
│   │   │   ├── Script.java            # 脚本数据模型
│   │   │   ├── ScriptManager.java     # 脚本管理器
│   │   │   ├── ScriptAdapter.java     # 列表适配器
│   │   │   └── RootShell.java         # Root执行器
│   │   ├── res/
│   │   │   ├── layout/                # 布局文件
│   │   │   └── values/                # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── .github/workflows/
│   └── android.yml                    # CI配置
├── build.gradle
└── settings.gradle
```

## 🔄 自动编译

本项目使用GitHub Actions实现自动编译：

- 推送到 `main` 或 `master` 分支时自动触发
- 自动编译Debug和Release版本
- 自动创建Release并上传APK

## ⚠️ 注意事项

1. **Root权限**：本应用需要Root权限才能正常工作
2. **脚本权限**：确保脚本文件有执行权限（`chmod +x script.sh`）
3. **脚本路径**：支持绝对路径，如 `/sdcard/script.sh` 或 `/data/local/tmp/script.sh`
4. **数据安全**：执行脚本前请确认脚本内容，避免执行未知来源的脚本

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

- [AndroidX](https://developer.android.com/jetpack/androidx)
- [Material Design Components](https://material.io/develop/android)
- [Gson](https://github.com/google/gson)

---

<p align="center">
  Made with ❤️ for Android Root Users
</p>
