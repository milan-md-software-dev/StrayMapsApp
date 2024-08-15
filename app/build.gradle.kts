
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    //Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.straymaps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.straymaps"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    testOptions.unitTests{
        isIncludeAndroidResources = true
    }
}


dependencies {

    implementation("com.google.firebase:firebase-database:20.3.0")
    val nav_version = "2.7.4"
    val room_version = "2.5.2"
    val hilt_version = "2.48.1"
    val accompanist_version = "0.30.1"
    val camerax_version = "1.3.0"
    val androidXTestVersion = "1.6.0"
    val mockkVersion = "1.13.9"
    val mockitoVersion = "4.0.0"

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.5.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //CameraX
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")

    // Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Feature module Support
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")

    // Testing Navigation
    androidTestImplementation("androidx.navigation:navigation-testing:$nav_version")

    // Jetpack Compose Integration
    implementation("androidx.navigation:navigation-compose:$nav_version")

    //Room
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    //Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-compiler:$hilt_version")

    //Hilt Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage-ktx")

    //Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    //Accompanist
    implementation("com.google.accompanist:accompanist-permissions:$accompanist_version")

    //Mapbox
    implementation ("com.mapbox.maps:android:11.3.0")
    implementation ("com.mapbox.extension:maps-compose:11.3.0")

    //Local unit tests
    implementation("androidx.test:core:1.6.0")
    testImplementation ("androidx.test.ext:junit-ktx:1.2.0")
    testImplementation("androidx.test:runner:$androidXTestVersion")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
    testImplementation("com.google.truth:truth:1.2.0")
    testImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    testImplementation ("org.robolectric:robolectric:4.11.1")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation("app.cash.turbine:turbine:0.7.0")
    testImplementation ("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation ("org.mockito:mockito-inline:${mockitoVersion}")
    testImplementation ("org.mockito.kotlin:mockito-kotlin:${mockitoVersion}")
    testImplementation ("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation ("org.powermock:powermock-module-junit4:2.0.9")
    kaptTest("com.google.dagger:hilt-compiler:$hilt_version")

    //Instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:$hilt_version")
    kaptAndroidTest("com.google.dagger:hilt-compiler:$hilt_version")
    androidTestImplementation ("androidx.test:rules:$androidXTestVersion")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test:core:$androidXTestVersion")
    androidTestImplementation ("androidx.test:runner:$androidXTestVersion")
    androidTestImplementation("com.google.truth:truth:1.2.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.mockito:mockito-core:${mockitoVersion}")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}