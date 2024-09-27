# Catalyst
Lightweight open source Minecraft plugin framework that aims to reduce Bukkit's verbosity while also providing useful tools for developers.

![JitPack](https://img.shields.io/jitpack/version/com.github.matyas-toth/Catalyst)


### [Documentation](https://understood-diascia-0f5.notion.site/527233c73ac8499faafd7488ab8353f4?v=6611342351cc4419a2e004385402c330)

### How to use? (Maven)
Add this to your respositories:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```

Add this to your dependencies:
```xml
<dependency>
    <groupId>com.github.matyas-toth</groupId>
    <artifactId>Catalyst</artifactId>
    <version>1.0.0</version>
</dependency>
```

### How to use? (Gradle)
Repositories:
```gradle
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```
Dependencies:
```gradle
	dependencies {
		implementation 'com.github.matyas-toth:Catalyst:1.0.0'
	}
```


### Features
- Easy event listeners with lambda expressions
- Lambda CommandExecutors
- A massive & easy-to-use Command Framework

### To-do
- [ ] GUI Framework
- [ ] Scoreboard Framework
- [ ] Configuration File Framework
- [ ] Per-player & General ORM/KV Database Framework
- [ ] Velocity Support
