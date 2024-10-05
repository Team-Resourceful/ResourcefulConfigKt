# Resourceful Config Kotlin
<hr>

The extensions to Resourceful Config for Kotlin.

### License and Availability

The mod is licensed under MIT, this is an extension of Resourceful Config and is available only on the maven.

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
    implementation fg.deobf("com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-neoforge-1.21:3.0.6")
}
```

### Fabric:
```gradle
dependencies {
    <--- Other dependencies here --->
    implementation "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21:3.0.6"
}
```

### Architectury:

#### Common `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-common-1.21:3.0.6"
}
```

#### Fabric `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-fabric-1.21:3.0.6"
}
```

#### NeoForge `build.gradle`
```gradle
dependencies {
    <--- Other dependencies here --->
    modImplementation "com.teamresourceful.resourcefulconfigkt:resourcefulconfigkt-neoforge-1.21:3.0.6"
}
```

TODO: Add Jar-in-Jar syntax