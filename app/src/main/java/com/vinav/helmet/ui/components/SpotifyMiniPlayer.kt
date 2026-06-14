package com.vinav.helmet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vinav.helmet.MainActivity
import com.vinav.helmet.media.TrackInfo
import com.vinav.helmet.ui.theme.ViNavPrimary

@Composable
fun SpotifyMiniPlayer(
    trackInfo: TrackInfo,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1DB954).copy(alpha = 0.1f)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        if (trackInfo.title.isEmpty() && !trackInfo.isPlaying) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.MusicNote, 
                        null, 
                        tint = Color(0xFF1DB954),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Spotify", 
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Button(
                    onClick = { (context as? MainActivity)?.startSpotifyLogin() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954)),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1DB954).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.MusicNote, null, tint = Color(0xFF1DB954))
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Track Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = trackInfo.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = trackInfo.artist.ifEmpty { "Spotify" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White)
                    }
                    IconButton(
                        onClick = { if (trackInfo.isPlaying) onPause() else onPlay() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            if (trackInfo.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            null,
                            tint = Color.Black
                        )
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Filled.SkipNext, "Next", tint = Color.White)
                    }
                }
            }
        }
    }
}
