package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.Translator

@Composable
fun NoInternetBanner(visible: Boolean, language: AppLanguage) {
    AnimatedVisibility(visible = visible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFD32F2F))
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = Translator.translate("no_internet_banner", language),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun GpsStatusPill(gpsMode: String, language: AppLanguage) {
    val isBoth = gpsMode == "both"
    val bgColor = if (isBoth) UpiSuccess.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f)
    val strokeColor = if (isBoth) UpiSuccess else WarningAmber
    val textColor = if (isBoth) UpiSuccess else WarningAmber
    val label = if (isBoth) {
        Translator.translate("gps_both_mode", language)
    } else {
        Translator.translate("gps_driver_mode", language)
    }

    Box(
        modifier = Modifier
            .testTag("gps_status_pill")
            .background(bgColor, shape = RoundedCornerShape(100.dp))
            .border(1.dp, strokeColor, shape = RoundedCornerShape(100.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun LiveMapCanvas(
    driverLoc: Pair<Double, Double>?,
    passengerLoc: Pair<Double, Double>?,
    driverPath: List<Pair<Double, Double>>,
    passengerPath: List<Pair<Double, Double>>,
    gpsMode: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "map_animations")
    
    // Rotating radar glow animation
    val radarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteTransitionSpec(),
        label = "radar_angle"
    )

    // Breathing pulse for locations
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteTransitionSpecBreathing(),
        label = "pulse_radius"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(NearBlackBg)
            .border(1.dp, SurfaceElevated, shape = RoundedCornerShape(16.dp))
            .testTag("live_map_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2, height / 2)

            // Draw radial dark grid background
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(SurfaceCard, NearBlackBg),
                    center = center,
                    radius = width / 1.5f
                )
            )

            // Draw subtle coordinate grid lines
            val gridSpacing = 60.dp.toPx()
            val gridStroke = Stroke(width = 1.dp.toPx())
            val gridColor = Color(0x1FADADAD)

            // Vertical Grid lines
            var x = 0f
            while (x < width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = gridStroke.width)
                x += gridSpacing
            }

            // Horizontal Grid lines
            var y = 0f
            while (y < height) {
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = gridStroke.width)
                y += gridSpacing
            }

            // Draw concentric radar indicator rings
            drawCircle(Color(0x0AFFFFFF), radius = 100.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
            drawCircle(Color(0x14FFFFFF), radius = 160.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))

            // Draw spinning green radar line for premium technological depth
            rotate(radarAngle, center) {
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x8000C853), Color.Transparent),
                        start = center,
                        end = Offset(center.x, center.y - 180.dp.toPx())
                    ),
                    start = center,
                    end = Offset(center.x, center.y - 180.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // Helper to translate GPS offset to local canvas pixels relative to Bhubaneswar center coordinate
            val centerLat = 20.2961
            val centerLng = 85.8245
            
            fun gpsToPixel(coord: Pair<Double, Double>?): Offset {
                if (coord == null) return center
                val latDiff = coord.first - centerLat
                val lngDiff = coord.second - centerLng
                // Scale factor to fit movement nicely in the 260dp box
                val pxX = center.x + (lngDiff * 45000).toFloat()
                val pxY = center.y - (latDiff * 45000).toFloat() // Invert Y for screen space
                return Offset(
                    pxX.coerceIn(20f, width - 20f),
                    pxY.coerceIn(20f, height - 20f)
                )
            }

            // Draw Bhubaneswar simulated street grid path lines
            val streetStroke = Stroke(width = 3.dp.toPx())
            val streetColor = Color(0x1AFFFFFF)
            
            // Decal mock streets
            drawLine(streetColor, Offset(0f, height * 0.2f), Offset(width, height * 0.4f), strokeWidth = streetStroke.width)
            drawLine(streetColor, Offset(width * 0.3f, 0f), Offset(width * 0.4f, height), strokeWidth = streetStroke.width)
            drawLine(streetColor, Offset(0f, height * 0.8f), Offset(width, height * 0.6f), strokeWidth = streetStroke.width)

            // Draw Driver Path (Saffron glowing polyline)
            if (driverPath.size > 1) {
                val path = Path()
                val firstPoint = gpsToPixel(driverPath.first())
                path.moveTo(firstPoint.x, firstPoint.y)
                for (i in 1 until driverPath.size) {
                    val pt = gpsToPixel(driverPath[i])
                    path.lineTo(pt.x, pt.y)
                }
                drawPath(
                    path = path,
                    color = SaffronPrimary,
                    style = Stroke(width = 4.dp.toPx(), miter = 1f)
                )
            }

            // Draw Passenger Path (Blue glowing polyline) if active
            if (gpsMode == "both" && passengerPath.size > 1) {
                val path = Path()
                val firstPoint = gpsToPixel(passengerPath.first())
                path.moveTo(firstPoint.x, firstPoint.y)
                for (i in 1 until passengerPath.size) {
                    val pt = gpsToPixel(passengerPath[i])
                    path.lineTo(pt.x, pt.y)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF00B0FF),
                    style = Stroke(width = 3.dp.toPx(), miter = 1f)
                )
            }

            // Draw Passenger Point (Animated Blue Ring + Core)
            if (gpsMode == "both" && passengerLoc != null) {
                val passOffset = gpsToPixel(passengerLoc)
                // Glowing outer breathing circle
                drawCircle(
                    color = Color(0xFF00B0FF).copy(alpha = 0.25f),
                    radius = pulseRadius,
                    center = passOffset
                )
                // Outer ring
                drawCircle(
                    color = Color(0xFF00B0FF),
                    radius = 8.dp.toPx(),
                    center = passOffset,
                    style = Stroke(width = 2.dp.toPx())
                )
                // Core
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = passOffset
                )
            }

            // Draw Driver Point (Animated Saffron Auto-Rickshaw Pin)
            if (driverLoc != null) {
                val driverOffset = gpsToPixel(driverLoc)
                // Glowing outer breathing circle
                drawCircle(
                    color = SaffronPrimary.copy(alpha = 0.3f),
                    radius = pulseRadius + 2.dp.toPx(),
                    center = driverOffset
                )
                // Draw Saffron rickshaw dot
                drawCircle(
                    color = SaffronPrimary,
                    radius = 10.dp.toPx(),
                    center = driverOffset
                )
                // White accent inner point
                drawCircle(
                    color = Color.White,
                    radius = 5.dp.toPx(),
                    center = driverOffset
                )
            }
        }

        // Floating Street Name Decals
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Text(
                text = "Janpath Road",
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.TopStart)
            )
            Text(
                text = "Vani Vihar Square",
                color = Color.White.copy(alpha = 0.25f),
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

// Helpers for animations
private fun infiniteTransitionSpec() = infiniteRepeatable<Float>(
    animation = tween(durationMillis = 4000, easing = LinearEasing),
    repeatMode = RepeatMode.Restart
)

private fun infiniteTransitionSpecBreathing() = infiniteRepeatable<Float>(
    animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
    repeatMode = RepeatMode.Reverse
)
