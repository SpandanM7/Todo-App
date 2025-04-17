package com.example.onlyandroidtodo

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.example.onlyandroidtodo.R

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SplashScreen()
        }

        // for delay
        Handler(Looper.getMainLooper()).postDelayed({
            // Start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3 seconds
    }
}

@Composable
fun SplashScreen() {
    val logo = painterResource(id = R.drawable.todo_logo)
    //Log.d("SplashActivity", "Logo loaded: $logo")

    // Set a gradient background
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent // Transparent to allow gradient
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFff0000), Color(0xFF00ffff))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(align = Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = logo,
                    contentDescription = "To-Do Logo",
                    modifier = Modifier.size(400.dp) // Adjust size as needed
                )

                Spacer(modifier = Modifier.height(16.dp))

                /*
                Text(
                    text = "Made by Spandan",
                    style = MaterialTheme.typography.bodySmall
                )
                */
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen()
}
