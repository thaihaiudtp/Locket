package com.example.locket.ui.register

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
fun RegisterScreen(
    viewModel: RegisterViewModel = hiltViewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.registerState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        when (state) {
            is RegisterState.Success -> {
                Toast.makeText(context, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show()
                onRegisterSuccess()
            }
            is RegisterState.Error -> {
                Toast.makeText(context, (state as RegisterState.Error).message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 1. Nền đen
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
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

        // Input Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
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

        // Nút Đăng Ký
        Button(
            onClick = { viewModel.register(email, username, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = state !is RegisterState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = LocketYellow,
                contentColor = Color.Black,
                disabledContainerColor = Color.DarkGray,
                disabledContentColor = Color.Gray
            )
        ) {
            if (state is RegisterState.Loading) {
                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút chuyển sang Đăng nhập
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? ", color = Color.Gray)
            Text("Log in", color = LocketYellow, fontWeight = FontWeight.Bold)
        }
    }
}