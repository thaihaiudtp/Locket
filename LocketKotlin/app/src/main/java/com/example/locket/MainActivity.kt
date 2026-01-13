// locket/MainActivity.kt
package com.example.locket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.locket.ui.camera.CameraScreen
import com.example.locket.ui.friend.FriendScreen
import com.example.locket.ui.history.HistoryScreen
import com.example.locket.ui.login.LoginScreen
import com.example.locket.ui.profile.ProfileScreen
import com.example.locket.ui.register.RegisterScreen
import com.example.locket.ui.theme.LocketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocketTheme {
                val isLoading by mainViewModel.isLoading.collectAsState()
                val startDestination by mainViewModel.startDestination.collectAsState()
                if(isLoading){
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                } else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginSuccess = {
                                    navController.navigate("camera") { popUpTo("login") { inclusive = true } }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = { navController.popBackStack() },
                                onNavigateToLogin = { navController.navigate("login") }
                            )
                        }
                        composable("camera") {
                            CameraScreen(
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToFriend = { navController.navigate("friend") },
                                onNavigateToProfile = { navController.navigate("profile") }
                            )
                        }
                        composable(
                            "history",
                            // 1. History trượt từ DƯỚI lên (SlideDirection.Up)
                            enterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400))
                            },
                            // 2. Khi thoát, trượt xuống (SlideDirection.Down)
                            exitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                            }
                        ) {
                            HistoryScreen(
                                onBackToCamera = { navController.popBackStack() },
                                onNavigateToProfile = { navController.navigate("profile") }
                            )
                        }
                        composable(
                            "friend",
                            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400)) },
                            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400)) }
                        ) {
                            FriendScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            "profile",
                            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400)) },
                            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400)) }
                        ) {
                            ProfileScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLogoutSuccess = {
                                    // Khi logout, điều hướng về Login và xóa sạch backstack
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}