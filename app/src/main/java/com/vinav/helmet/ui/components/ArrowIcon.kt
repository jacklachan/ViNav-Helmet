package com.vinav.helmet.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NorthWest
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Straight
import androidx.compose.material.icons.filled.UTurnLeft
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vinav.helmet.model.ArrowDirection
import com.vinav.helmet.ui.theme.ViNavPrimary
import com.vinav.helmet.ui.theme.ViNavSuccess

@Composable
fun ArrowIcon(
    direction: ArrowDirection,
    size: Dp = 64.dp,
    tint: Color = ViNavPrimary,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (direction) {
        ArrowDirection.STRAIGHT -> Icons.Filled.North
        ArrowDirection.LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        ArrowDirection.RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
        ArrowDirection.SLIGHT_LEFT -> Icons.Filled.NorthWest
        ArrowDirection.SLIGHT_RIGHT -> Icons.Filled.NorthEast
        ArrowDirection.UTURN -> Icons.Filled.UTurnLeft
        ArrowDirection.ARRIVED -> Icons.Filled.Flag
        ArrowDirection.ROUNDABOUT_LEFT -> Icons.Filled.Refresh // Placeholder for roundabout
        ArrowDirection.ROUNDABOUT_RIGHT -> Icons.Filled.Refresh // Placeholder for roundabout
        ArrowDirection.ROUNDABOUT_STRAIGHT -> Icons.Filled.Straight
    }
    val color = if (direction == ArrowDirection.ARRIVED) ViNavSuccess else tint
    Icon(
        imageVector = icon,
        contentDescription = direction.name,
        tint = color,
        modifier = modifier.size(size)
    )
}
