package com.example.locket.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

val LocketYellow = Color(0xFFFFD700)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.loginState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> {
                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
            is LoginState.Error -> {
                Toast.makeText(context, (state as LoginState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 1. Nền đen
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Locket",
            style = MaterialTheme.typography.displayMedium, // Chữ to hơn
            color = LocketYellow, // Màu vàng
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Login to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            // 2. Style cho TextField trên nền đen
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = LocketYellow,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = LocketYellow,
                unfocusedLabelColor = Color.Gray,
                cursorColor = LocketYellow
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = LocketYellow,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = LocketYellow,
                unfocusedLabelColor = Color.Gray,
                cursorColor = LocketYellow
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Nút Đăng Nhập
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = state !is LoginState.Loading,
            // 3. Nút màu vàng, chữ đen
            colors = ButtonDefaults.buttonColors(
                containerColor = LocketYellow,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.Gray
            )
        ) {
            if (state is LoginState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Log In", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút chuyển sang Đăng ký
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? ", color = Color.Gray)
            Text("Sign up", color = LocketYellow, fontWeight = FontWeight.Bold)
        }
    }
}