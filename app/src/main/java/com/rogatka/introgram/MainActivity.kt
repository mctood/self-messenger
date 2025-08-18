package com.rogatka.introgram

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rogatka.introgram.nav.ChatScreen
import com.rogatka.introgram.nav.DetailsScreen
import com.rogatka.introgram.nav.MainScreen
import com.rogatka.introgram.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedText = handleIntent(intent = intent)

        SharedContentHolder.sharedText = sharedText

        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ScaffoldDefaults.contentWindowInsets
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "main/0",  // Стартовый экран
                ) {
                    composable(
                        "main/{folder}",
                        arguments = listOf(navArgument("folder") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val folder = backStackEntry.arguments?.getInt("folder") ?: 0
                        MainScreen(navController, folder)
                    }


                    composable(
                        "details/{folder}",
                        arguments = listOf(navArgument("folder") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val folder = backStackEntry.arguments?.getInt("folder") ?: 0
                        DetailsScreen(navController, folder)
                    }


                    composable(
                        "chat/{id}/{folder}",
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(400)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(400)
                            )
                        },
                        arguments = listOf(
                            navArgument("id") { type = NavType.IntType },
                            navArgument("folder") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id") ?: 0
                        val folder = backStackEntry.arguments?.getInt("folder") ?: 0
                        ChatScreen(id, navController, folder)
                    }


                }
            }
        }
    }
    private fun handleIntent(intent: Intent?): String? {
        intent?.let {
            when (it.action) {
                Intent.ACTION_SEND -> {
                    if ("text/plain" == intent.type) {
                        return it.getStringExtra(Intent.EXTRA_TEXT)
                    }
                }
            }
        }
        return null
    }
}

object SharedContentHolder {
    var sharedText by mutableStateOf<String?>(null)
}