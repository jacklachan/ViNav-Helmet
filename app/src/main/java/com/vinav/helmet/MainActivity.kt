package com.vinav.helmet

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.vinav.helmet.media.MediaController
import com.vinav.helmet.navigation.ViNavNavGraph
import com.vinav.helmet.ui.theme.ViNavTheme
import com.vinav.helmet.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mediaController: MediaController

    private val REQUEST_CODE = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            ViNavTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ViNavNavGraph()
                }
            }
        }
    }

    fun startSpotifyLogin() {
        val builder = AuthorizationRequest.Builder(
            Constants.SPOTIFY_CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            Constants.SPOTIFY_REDIRECT_URI
        )

        builder.setScopes(arrayOf("streaming", "user-read-playback-state", "user-modify-playback-state"))
        // Force the use of the web view to avoid "SERVICE_UNAVAILABLE" errors
        builder.setShowDialog(true) 
        val request = builder.build()

        AuthorizationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    Log.d("ViNav", "Spotify Auth Success")
                    // In a real app, you'd save the token here
                    mediaController.connect()
                }
                AuthorizationResponse.Type.ERROR -> {
                    Log.e("ViNav", "Spotify Auth Error: ${response.error}")
                }
                else -> {}
            }
        }
    }
}
