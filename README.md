<p align="center">
  <img src="https://imgur.com/e5ZjL6Y.png" alt="">
</p>

<h2 align="center">Welcome to Osmia!</h2>

<p align="center">
  <a href="https://discord.gg/6hhYkw7mHV">
    <img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white" alt="Discord">
  </a>
  <a href="https://github.com/OsmiaMC/Osmia-Client/issues">
    <img src="https://img.shields.io/badge/Github%20Issues-181717?style=for-the-badge&logo=github&logoColor=white" alt="Github Issues">
  </a>
  <a href="https://github.com/OsmiaMC/Osmia-Client/releases">
    <img src="https://img.shields.io/badge/Releases-2EA44F?style=for-the-badge&logo=github&logoColor=white" alt="Releases">
  </a>
</p>

## Development

Osmia targets Java 25 and builds with the included Gradle wrapper:

```shell
bash ./gradlew build
```

On Windows, use `.\gradlew.bat build` from PowerShell.

Toggleable features are regular classes under `dev.osmia.module.impl`. Each module owns its name,
category, saved state, settings, and any runtime hooks it needs. `ModuleManager` keeps their order
and is the single source used by the ClickGUI. New modules are registered explicitly in
`OsmiaClient` so startup behavior remains easy to trace.
