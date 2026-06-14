package com.vinav.helmet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vinav.helmet.ui.theme.ViNavError
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess
import com.vinav.helmet.ui.theme.ViNavWarning

@Composable
fun StatusChip(
    label: String,
    color: Color,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
            }
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

@Composable
fun ConnectedChip(modifier: Modifier = Modifier) =
    StatusChip("Connected", ViNavSuccess, modifier = modifier)

@Composable
fun DisconnectedChip(modifier: Modifier = Modifier) =
    StatusChip("Disconnected", ViNavError, modifier = modifier)

@Composable
fun ConnectingChip(modifier: Modifier = Modifier) =
    StatusChip("Connecting…", ViNavWarning, modifier = modifier)

@Composable
fun ScanningChip(modifier: Modifier = Modifier) =
    StatusChip("Scanning…", ViNavPrimary, modifier = modifier)
