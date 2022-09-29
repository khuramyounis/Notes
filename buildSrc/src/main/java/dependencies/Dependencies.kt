package dependencies

object Dependencies {

    val kotlin_standard_library = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val kotlin_reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
    const val ktx = "androidx.core:core-ktx:${Versions.ktx}"
    val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_version}"
    val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines_version}"
    const val kotlin_coroutines_play_services = "org.jetbrains.kotlinx:kotlinx-coroutines-play-services:${Versions.coroutines_play_services}"
    const val dagger = "com.google.dagger:dagger:${Versions.dagger}"
    const val navigation_fragment = "androidx.navigation:navigation-fragment-ktx:${Versions.nav_components}"
    val navigation_runtime = "androidx.navigation:navigation-runtime:${Versions.nav_components}"
    const val navigation_ui = "androidx.navigation:navigation-ui-ktx:${Versions.nav_components}"
    const val navigation_dynamic = "androidx.navigation:navigation-dynamic-features-fragment:${Versions.nav_components}"
    const val material_dialogs = "com.afollestad.material-dialogs:core:${Versions.material_dialogs}"
    const val material_dialogs_input = "com.afollestad.material-dialogs:input:${Versions.material_dialogs}"
    const val room_runtime = "androidx.room:room-runtime:${Versions.room}"
    const val room_ktx = "androidx.room:room-ktx:${Versions.room}"
    val play_core = "com.google.android.play:core:${Versions.play_core}"
    val leak_canary = "com.squareup.leakcanary:leakcanary-android:${Versions.leak_canary}"

    const val firebase_bom = "com.google.firebase:firebase-bom:${Versions.firebase_bom}"
    const val firebase_firestore = "com.google.firebase:firebase-firestore-ktx"
    const val firebase_auth = "com.google.firebase:firebase-auth"
    const val firebase_analytics = "com.google.firebase:firebase-analytics-ktx"
    const val firebase_crashlytics = "com.google.firebase:firebase-crashlytics-ktx"

    val lifecycle_runtime = "androidx.lifecycle:lifecycle-runtime:${Versions.lifecycle_version}"
    val lifecycle_coroutines = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle_version}"
    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit2_version}"
    const val retrofit_gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit2_version}"
    const val markdown_processor = "com.yydcdut:markdown-processor:${Versions.markdown_processor}"

}