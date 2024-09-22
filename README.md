# Catalyst
Lightweight open source Minecraft plugin framework that aims to reduce Bukkit's verbosity while also providing useful tools for developers.

### [Documentation](https://dev.reigindustries.com/catalyst-docs) | [Examples](https://dev.reigindustries.com/catalyst-examples) 

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
    <version>master-SNAPSHOT</version>
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
		implementation 'com.github.matyas-toth:Catalyst:master-SNAPSHOT'
	}
```


### Features
- Easy event listeners with lambda expressions
- Lambda CommandExecutors
- A massive & easy-to-use Command Framework

### To-do
- [ ] GUI Framework
- [ ] Scoreboard Framework
