plugins {
    alias(libs.plugins.installerx.test)
    alias(libs.plugins.baselineprofile)
}

android {
    namespace = "com.rosan.baselineprofile"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Instruct Gradle to use the "Unstable" flavor from the target app module
        // since this module no longer declares the "level" dimension.
        missingDimensionStrategy("level", "Unstable")
    }

    targetProjectPath = ":app"

    // Keep only the "connectivity" dimension
    flavorDimensions += listOf("connectivity")

    productFlavors {
        create("online") { dimension = "connectivity" }
        create("offline") { dimension = "connectivity" }

        // The "Unstable", "Preview", and "Stable" flavors are completely removed
        // to avoid redundant Baseline Profile generation.
    }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.uiautomator)
}

androidComponents {
    onVariants { v ->
        val artifactsLoader = v.artifacts.getBuiltArtifactsLoader()
        v.instrumentationRunnerArguments.put(
            "targetAppId",
            v.testedApks.map { artifactsLoader.load(it)?.applicationId }
        )
    }
}
