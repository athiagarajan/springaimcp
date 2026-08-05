# 📚 Java Toolchain & VS Code Configuration Guide (`SPRINGAIMCP`)

---

## 🏛️ 1. Project Architecture & Stack Overview

* **Project Root Folder**: `C:\Thiagu\AGY_PROJECTS\SPRINGAIMCP`
* **GitHub Repository**: `https://github.com/athiagarajan/springaimcp`
* **Backend Application** (`backend/`):
  * **Build Tool**: Standard Groovy Gradle (`build.gradle` & `settings.gradle`)
  * **Runtime Target**: OpenJDK 25 (`JavaLanguageVersion.of(25)`)
  * **Framework Stack**: Spring Boot 4.0 / 3.4+, Jackson 3, Spring AI 2.0+ MCP Server, Spring Security
  * **MCP Annotations Engine**: `@McpTool`, `@McpResource`, `@McpPrompt`
  * **Security & API Docs**: Spring Security HTTP Basic Auth protecting Swagger UI (`http://localhost:8080/swagger-ui.html`, Credentials: `admin` / `adminpassword`)
  * **Testing**: 💯 100% JaCoCo Line & Branch Test Coverage Gate
* **Frontend Application** (`frontend/`):
  * **Stack**: React 19 + TypeScript + Vite 6 + Tailwind CSS
  * **Components**: PromptBar, StreamReasoningLog (SSE Viewer), TempleMap (Leaflet GPS pins for `hf_lat` & `hf_lan`), TempleTable, TempleDetailModal
  * **Testing**: 💯 100% Vitest Coverage Gate
* **Database**: PostgreSQL 18 on `localhost:5432` (`templeinfo` database, `temples` table with 96 records and 31 columns)

---

## ☕ 2. Side-by-Side Java 19 & Java 25 Coexistence

### How It Works:
* **System Default (`Java 19`)**: Your Windows system `JAVA_HOME` remains set to `C:\Program Files\Java\jdk-19`. Command Prompt / PowerShell commands (`java -version`) will continue using Java 19 globally for all other projects.
* **Project Specific (`Java 25`)**: In `backend/build.gradle`, specifying `languageVersion = JavaLanguageVersion.of(25)` instructs Gradle to build and run this specific project with JDK 25 without modifying your OS environment.

---

## 🛠️ 3. Foojay Toolchain Resolver Plugin

Adding the Foojay plugin to `backend/settings.gradle`:

```groovy
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.9.0'
}
rootProject.name = 'backend'
```

### Key Behaviors:
1. **Persistent Storage**: Gradle downloads and extracts JDK 25 into `C:\Users\alage\.gradle\jdks\`. It is saved permanently and reused for all future builds.
2. **Zero System Alterations**: It does **not** change your Windows Registry, environment variables, or global `JAVA_HOME`.
3. **Automatic Switching**: Gradle automatically switches to JDK 25 whenever commands are run inside the `backend` project directory.

---

## 💻 4. How to Point VS Code to JDK 25

### Method 1: Automatic Detection (Default)
The **Extension Pack for Java** in VS Code scans `build.gradle` automatically. Once Gradle downloads JDK 25 into `C:\Users\alage\.gradle\jdks\`, VS Code auto-detects it and configures your project language level to Java 25 automatically.

### Method 2: Command Palette (UI)
1. Press `Ctrl + Shift + P` in VS Code.
2. Type **`Java: Configure Java Runtime`** and press `Enter`.
3. Under **Project JDKs**, click **Add JDK**.
4. Select the folder `C:\Users\alage\.gradle\jdks\` where JDK 25 is installed.

### Method 3: `.vscode/settings.json` Configuration File
Create or update `.vscode/settings.json` in the root workspace:

```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-19",
      "path": "C:\\Program Files\\Java\\jdk-19"
    },
    {
      "name": "JavaSE-25",
      "path": "C:\\Users\\alage\\.gradle\\jdks\\<jdk-25-folder-name>",
      "default": true
    }
  ]
}
```

---

## 🚀 5. Quick Commands Summary

### Backend
```cmd
cd C:\Thiagu\AGY_PROJECTS\SPRINGAIMCP\backend
gradle bootRun
```
* **Swagger UI**: `http://localhost:8080/swagger-ui.html`
* **Test & 100% Coverage**: `gradle test jacocoTestCoverageVerification`

### Frontend
```cmd
cd C:\Thiagu\AGY_PROJECTS\SPRINGAIMCP\frontend
npm install
npm run dev
```
* **Frontend UI**: `http://localhost:3000`
* **Test & 100% Coverage**: `npm run test`
