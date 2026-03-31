package com.matechmatrix.shopflowpos.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize

/** Maps a route string to the human-readable page title shown in the TopBar. */
fun routeToTitle(route: String?): String = when (route) {
    AppRoute.Dashboard.route    -> "Dashboard"
    AppRoute.POS.route          -> "POS Sale"
    AppRoute.Inventory.route    -> "Products"
    AppRoute.Transactions.route -> "Sales"
    AppRoute.SalesReturn.route  -> "Returns"
    AppRoute.Customers.route    -> "Customers"
    AppRoute.Suppliers.route    -> "Suppliers"
    AppRoute.Expenses.route     -> "Expenses"
    AppRoute.Ledger.route       -> "Cash & Bank"
    AppRoute.Reports.route      -> "Reports"
    AppRoute.Installments.route -> "Installments"
    AppRoute.Repairs.route      -> "Repairs"
    AppRoute.Settings.route     -> "Settings"
    AppRoute.Reviews.route      -> "Reviews"
    else                        -> "ShopFlowPOS"
}

/**
 * Adaptive top bar for all non-auth screens.
 */
@Composable
fun ShopFlowTopBar(
    currentRoute    : String?,
    windowSize      : AppWindowSize,
    canNavigateBack : Boolean  = false,
    shopName        : String   = "Al-Madina Mobile",
    ownerInitials   : String   = "A",
    onMenuClick     : () -> Unit,
    onBackClick     : () -> Unit = {},
    onAvatarClick   : () -> Unit = {},
    onReviewClick   : () -> Unit = {}
) {
    val isCompact = windowSize == AppWindowSize.COMPACT
    val pageTitle = routeToTitle(currentRoute)

    val showBack = canNavigateBack
    val showMenu = !canNavigateBack && isCompact

    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation  = 0.dp
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        drawLine(
                            color = colorScheme.outlineVariant.copy(alpha = 0.3f),
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ── Left button ───────────────────────────────────────────────
                    when {
                        showBack -> {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.surfaceVariant)
                                    .clickable(onClick = onBackClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.ArrowBackIosNew,
                                    contentDescription = "Go Back",
                                    tint               = colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(16.dp)
                                )
                            }
                        }
                        showMenu -> {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(colorScheme.surfaceVariant)
                                    .clickable(onClick = onMenuClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector        = Icons.Rounded.Menu,
                                    contentDescription = "Open Menu",
                                    tint               = colorScheme.onSurfaceVariant,
                                    modifier           = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // ── Brand ──────────────────
                    Row(
                        modifier          = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
//                        Box(
//                            modifier = Modifier
//                                .size(32.dp)
//                                .clip(RoundedCornerShape(10.dp))
//                                .background(Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary))),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("📱", fontSize = 14.sp)
//                        }
                        Column {
                            Text(
                                text       = pageTitle,
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = colorScheme.onSurface,
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Text(text = shopName, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                        }
                    }

                    // ── Right: actions ─────────────────────────────────
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Review button
//                        Box(
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(colorScheme.surfaceVariant)
//                                .clickable { onReviewClick() },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector = Icons.Rounded.Star,
//                                contentDescription = "Reviews",
//                                tint = Color(0xFFFFB830),
//                                modifier = Modifier.size(18.dp)
//                            )
//                        }

                        // Dark mode toggle
//                        Box(
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(colorScheme.surfaceVariant)
//                                .clickable { /* TODO */ },
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Text("🌙", fontSize = 14.sp)
//                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(colorScheme.primary, colorScheme.secondary)))
                                .clickable(onClick = onAvatarClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text       = ownerInitials,
                                fontSize   = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}