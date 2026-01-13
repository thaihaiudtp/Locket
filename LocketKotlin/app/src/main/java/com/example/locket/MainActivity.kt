package com.example.locket

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.example.locket.ui.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Nạp layout XML (chứa FragmentContainerView)
        setContentView(R.layout.activity_main)

        // 2. Thiết lập NavController
        // Tìm NavHostFragment từ ID trong activity_main.xml
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 3. Xử lý logic Start Destination (Login vs Camera)
        // Mặc định nav_graph.xml đang set startDestination là LoginFragment.
        // Ta lắng nghe ViewModel, nếu token đã có (startDestination == "camera") thì chuyển ngay sang Camera.

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.startDestination.collect { destination ->
                    if (destination == "camera") {
                        // Kiểm tra nếu đang ở Login (mặc định) thì mới navigate sang Camera
                        val currentDest = navController.currentDestination?.id
                        if (currentDest == R.id.loginFragment) {
                            // Gọi action chuyển từ Login -> Camera (đã định nghĩa trong nav_graph)
                            navController.navigate(R.id.action_loginFragment_to_cameraFragment)
                        }
                    }
                }
            }
        }
    }
}