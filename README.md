# 轻待办 Android CI/CD 练习

一个仅在本机保存数据的原生 Android 待办应用。支持添加、完成、删除任务和完成数量统计，不需要账号或网络权限。适用于 Android 8.0（API 26）及以上。

## 构建环境

- Java 17、Android Gradle Plugin 8.9.2、Gradle 8.11.1。
- compileSdk / targetSdk 35，Build Tools 35.0.0。
- 完整 Gradle Wrapper 已提交，分发包 SHA-256 已固定；Wrapper JAR 从 Gradle 官方仓库取得并核对官方校验值。
- GitHub Actions 声明构建环境，本机不需要安装 Android Studio 或 Android SDK。

## Debug 构建

向 `main` / `develop` 推送，或向 `main` 提交 Pull Request，自动运行：

1. checkout → JDK 17 → Gradle 缓存。
2. JVM 单元测试、Android Lint、`assembleDebug`。
3. 使用 `apksigner` 验证 APK 签名。
4. Android 29 模拟器安装并启动应用，测试新增、完成、Activity 重建后的本地保存和删除流程。
5. 在 Actions 的 Artifacts 中保存 `app-debug` 和测试报告。

非 main 分支只读缓存。Debug 版包名带 `.debug`，可与 Release 版同时安装。

## 正式签名与 Release 构建

在仓库 **Settings → Secrets and variables → Actions** 中添加四个 Repository secrets：

| 名称 | 内容 |
|---|---|
| `KEYSTORE_BASE64` | PKCS12 签名密钥文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | 密钥库密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

PKCS12 的密钥库密码和密钥密码使用相同值。密钥文件、密码和本地填写页均不在仓库中。Gradle 从环境变量读取签名配置，缺少配置时正式构建会明确失败，不会把未签名文件冒充正式安装包。

推送 `v*` 标签触发正式构建，例如：

```bash
git tag v1.0.0
git push origin v1.0.0
```

流程先测试和 Lint，再构建、校验签名、上传 `app-release` artifact。标签构建还会创建同名 GitHub Release，附带 APK。也可从 Actions 手动构建；手动构建保存 artifact，不创建版本标签。

签名密钥只在 runner 的临时目录中解码，任务结束时清除，不上传为 artifact。持续使用同一签名密钥，后续版本增加 `versionCode`，才能覆盖更新已安装的 Release 版。

## 下载安装包

- Debug：Actions → Android Debug Build → 成功运行 → Artifacts → app-debug，下载解压后安装 APK。
- Release：Releases → 对应版本 → app-release.apk。
- Android 如提示，可为用于打开 APK 的文件管理器或浏览器允许“安装未知应用”。

## 项目结构

```text
app/src/main/                       应用源码和资源
app/src/test/                       JVM 任务模型测试
app/src/androidTest/                Android 安装、启动及操作流程测试
.github/workflows/android-debug.yml 调试构建和模拟器验收
.github/workflows/android-release.yml 正式签名构建和发布
gradle/wrapper/                     固定版本的 Gradle Wrapper
```
