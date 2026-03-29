//package com.matechmatrix.shopflowpos.navigation
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.rounded.ArrowBackIosNew
//import androidx.compose.material.icons.rounded.Menu
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
//
//private val Primary     = Color(0xFF6C47FF)
//private val Accent      = Color(0xFFFF6B35)
//private val TopBarBg    = Color(0xFFFFFFFF)
//private val InputBg     = Color(0xFFEDEAFF)
//private val TextPrimary = Color(0xFF1A1240)
//private val TextMuted   = Color(0xFF8A80B4)
//private val BorderColor = Color(0x1A6C47FF)
//
///** Maps a route string to the human-readable page title shown in the TopBar. */
//fun routeToTitle(route: String?): String = when (route) {
//    AppRoute.Dashboard.route    -> "Dashboard"
//    AppRoute.POS.route          -> "POS Sale"
//    AppRoute.Inventory.route    -> "Products"
//    AppRoute.Transactions.route -> "Sales"
//    AppRoute.SalesReturn.route  -> "Returns"
//    AppRoute.Customers.route    -> "Customers"
//    AppRoute.Suppliers.route    -> "Suppliers"
//    AppRoute.Expenses.route     -> "Expenses"
//    AppRoute.Ledger.route       -> "Cash & Bank"
//    AppRoute.Reports.route      -> "Reports"
//    AppRoute.Installments.route -> "Installments"
//    AppRoute.Repairs.route      -> "Repairs"
//    AppRoute.Settings.route     -> "Settings"
//    else                        -> "ShopFlowPOS"
//}
//
///**
// * Adaptive top bar for all non-auth screens.
// *
// * Left button behaviour:
// *  • [canNavigateBack] = true  → shows ← Back arrow (child/detail screen)
// *  • [canNavigateBack] = false, COMPACT → shows ☰ hamburger (opens drawer)
// *  • [canNavigateBack] = false, MEDIUM/EXPANDED → no left button (sidebar always visible)
// *
// * Center: gradient logo icon + current page title + shop name subtitle
// * Right:  🌙 dark mode toggle + owner avatar (→ Settings)
// */
//@Composable
//fun ShopFlowTopBar(
//    currentRoute    : String?,
//    windowSize      : AppWindowSize,
//    canNavigateBack : Boolean  = false,
//    shopName        : String   = "Al-Madina Mobile",
//    ownerInitials   : String   = "A",
//    onMenuClick     : () -> Unit,
//    onBackClick     : () -> Unit = {},
//    onAvatarClick   : () -> Unit = {}
//) {
//    val isCompact = windowSize == AppWindowSize.COMPACT
//    val pageTitle = routeToTitle(currentRoute)
//
//    // Show left button:
//    //  ← back  → whenever there is a parent to go back to
//    //  ☰ menu  → COMPACT only, when on a top-level screen
//    val showBack = canNavigateBack
//    val showMenu = !canNavigateBack && isCompact
//
//    Surface(
//        modifier        = Modifier.fillMaxWidth().height(56.dp),
//        color           = TopBarBg,
//        shadowElevation = 0.dp,
//        tonalElevation  = 0.dp
//    ) {
//        // Bottom border matching HTML design
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(0.dp))
//        ) {
//            Row(
//                modifier              = Modifier
//                    .fillMaxSize()
//                    .padding(horizontal = 16.dp),
//                verticalAlignment     = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//
//                // ── Left button ───────────────────────────────────────────────
//                when {
//                    showBack -> {
//                        // ← Back arrow (child screen)
//                        Box(
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(InputBg)
//                                .clickable(onClick = onBackClick),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector        = Icons.Rounded.ArrowBackIosNew,
//                                contentDescription = "Go Back",
//                                tint               = TextMuted,
//                                modifier           = Modifier.size(16.dp)
//                            )
//                        }
//                    }
//                    showMenu -> {
//                        // ☰ Hamburger (top-level, COMPACT)
//                        Box(
//                            modifier = Modifier
//                                .size(36.dp)
//                                .clip(RoundedCornerShape(12.dp))
//                                .background(InputBg)
//                                .clickable(onClick = onMenuClick),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Icon(
//                                imageVector        = Icons.Rounded.Menu,
//                                contentDescription = "Open Menu",
//                                tint               = TextMuted,
//                                modifier           = Modifier.size(18.dp)
//                            )
//                        }
//                    }
//                    // MEDIUM/EXPANDED top-level → no left button
//                }
//
//                // ── Brand: logo + page title + shop subtitle ──────────────────
//                Row(
//                    modifier          = Modifier.weight(1f),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .clip(RoundedCornerShape(10.dp))
//                            .background(Brush.linearGradient(listOf(Primary, Accent))),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("📱", fontSize = 14.sp)
//                    }
//                    Column {
//                        Text(
//                            text       = pageTitle,
//                            fontSize   = 15.sp,
//                            fontWeight = FontWeight.Bold,
//                            color      = TextPrimary,
//                            maxLines   = 1,
//                            overflow   = TextOverflow.Ellipsis
//                        )
//                        Text(text = shopName, fontSize = 11.sp, color = TextMuted)
//                    }
//                }
//
//                // ── Right: dark mode + avatar ─────────────────────────────────
//                Row(
//                    horizontalArrangement = Arrangement.spacedBy(8.dp),
//                    verticalAlignment     = Alignment.CenterVertically
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(InputBg)
//                            .clickable { /* TODO: hook to dark-mode toggle */ },
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text("🌙", fontSize = 14.sp)
//                    }
//                    Box(
//                        modifier = Modifier
//                            .size(36.dp)
//                            .clip(RoundedCornerShape(12.dp))
//                            .background(Brush.linearGradient(listOf(Primary, Accent)))
//                            .clickable(onClick = onAvatarClick),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text       = ownerInitials,
//                            fontSize   = 13.sp,
//                            fontWeight = FontWeight.ExtraBold,
//                            color      = Color.White
//                        )
//                    }
//                }
//            }
//        }
//    }
//}