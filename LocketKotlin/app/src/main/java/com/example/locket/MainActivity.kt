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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.locket.ui.camera.CameraScreen
import com.example.locket.ui.detail.DetailPictureScreen
import com.example.locket.ui.friend.FriendScreen
import com.example.locket.ui.history.HistoryScreen
import com.example.locket.ui.login.LoginScreen
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
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // Có thể để trống hoặc thêm Logo Locket ở đây
                    }
                } else {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = startDestination) {

                        // Màn hình Login
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = {
                                    // Khi bấm nút "Đăng ký ngay" -> Chuyển sang màn register
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    navController.navigate("camera") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Màn hình Register
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    // Khi đăng ký xong -> Quay về màn login
                                    navController.popBackStack()
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                }
                            )
                        }
                        composable("camera") {
                            CameraScreen(
                                onNavigateToHistory = {
                                    navController.navigate("history")
                                },
                                onNavigateToFriend = { navController.navigate("friend") }
                            )
                        }
                        composable(
                            "history",
                            enterTransition = {
                                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up, tween(400))
                            },
                            exitTransition = {
                                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down, tween(400))
                            }
                        ) {
                            HistoryScreen(
                                onBackToCamera = { navController.popBackStack() },
                                onPictureClick = { pictureId ->
                                    // Điều hướng sang màn hình chi tiết tại đây
                                    navController.navigate("detail/$pictureId")
                                }
                            )
                        }
                        composable(
                            route = "detail/{pictureId}",
                            arguments = listOf(navArgument("pictureId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val pictureId = backStackEntry.arguments?.getString("pictureId") ?: return@composable

                            DetailPictureScreen(
                                pictureId = pictureId,
                                onNavigateBack = { navController.popBackStack() }, // Quay lại History
                                onNavigateToCamera = {
                                    // Quay về Camera (xóa hết backstack history và detail)
                                    navController.navigate("camera") {
                                        popUpTo("camera") { inclusive = true }
                                    }
                                }
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
                    }
                }
            }
        }
    }
}