# Resourceful Lib Kotlin
<hr>

The extensions to Resourceful Lib for Kotlin.

### License and Availability

The mod is licensed under MIT, this is an extension of Resourceful Lib and is available only on the maven.

### Contributions

If you would like to contribute to the mod feel free to submit a PR.
<br>TODO: Add more info about importing the project in IntelliJ and any additional setup required.

## For Mod Developers
<hr>

Be sure to add our maven to your `build.gradle`:
```gradle
repositories {
    maven { url = "https://maven.teamresourceful.com/repository/maven-public/" }
    <--- other repositories here --->
}
```
You can then add our mod as a dependency:

### NeoForge:
```gradle
dependencies {
    <--- Other dependencies here --->
    implementation fg.deobf("com.teamresourceful.resourcefullibkt:resourcefullibkt-neoforge-1.20.6:1.0.3")
}
```

### Fabric:
```gradle
dependencies {
    <--- Other dependencies here --->
    implementation "com.teamresourceful.resourcefullibkt:resourcefullibkt-fabric-1.20.6:1.0.3"
}
```

### Architectury:

#### Common `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefullibkt:resourcefullibkt-common-1.20.6:1.0.3"
}
```

#### Fabric `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefullibkt:resourcefullibkt-fabric-1.20.6:1.0.3"
}
```

#### NeoForge `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefullibkt:resourcefullibkt-neoforge-1.20.6:1.0.3"
}
```

TODO: Add Jar-in-Jar syntax