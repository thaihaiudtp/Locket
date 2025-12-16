package com.example.locket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.locket.ui.login.LoginScreen
import com.example.locket.ui.register.RegisterScreen
import com.example.locket.ui.theme.LocketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocketTheme {
                val navController = rememberNavController()

                // Quản lý các màn hình
                NavHost(navController = navController, startDestination = "login") {

                    // Màn hình Login
                    composable("login") {
                        LoginScreen(
                            onNavigateToRegister = {
                                // Khi bấm nút "Đăng ký ngay" -> Chuyển sang màn register
                                navController.navigate("register")
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
                }
            }
        }
    }
}