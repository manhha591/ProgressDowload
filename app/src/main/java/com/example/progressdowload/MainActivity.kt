package com.example.progressdowload

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.progressdownload.DownloadViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: DownloadViewModel = viewModel()
            val progress = viewModel.progress
            var url by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.startDownload("https://thetestdata.com/assets/video/mp4/highquality/5k_Thetestdata.mp4") },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Download")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Progress: $progress%")
                LinearProgressIndicator(progress = progress / 100f)
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    color = Color.Red
                )
            }
        }
    }
}