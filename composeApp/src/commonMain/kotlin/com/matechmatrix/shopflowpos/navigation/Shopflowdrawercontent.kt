package com.matechmatrix.shopflowpos.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Drawer nav items with sections ────────────────────────────────────────────────
private data class DrawerNavItem(
    val route  : String,
    val label  : String,
    val emoji  : String,
    val icon   : ImageVector,
    val badge  : String? = null,
    val section: String
)

private val allDrawerItems = listOf(
    // ── MAIN ──
    DrawerNavItem(AppRoute.Dashboard.route,    "Dashboard",    "🏠", Icons.Rounded.Dashboard,          section = "MAIN"),
    DrawerNavItem(AppRoute.POS.route,          "POS Sale",     "🛒", Icons.Rounded.PointOfSale,         section = "MAIN"),
    DrawerNavItem(AppRoute.Inventory.route,    "Products",     "📦", Icons.Rounded.Inventory2,          section = "MAIN"),

    // ── FINANCE ──
    DrawerNavItem(AppRoute.Transactions.route, "Sales",        "💰", Icons.Rounded.Receipt,             section = "FINANCE"),
    DrawerNavItem(AppRoute.Installments.route, "Installments", "📅", Icons.Rounded.CreditCard,          section = "FINANCE"),
    DrawerNavItem(AppRoute.SalesReturn.route,  "Returns",      "↩️", Icons.Rounded.AssignmentReturn,    section = "FINANCE"),
    DrawerNavItem(AppRoute.Expenses.route,     "Expenses",     "💸", Icons.Rounded.Money,               section = "FINANCE"),
    DrawerNavItem(AppRoute.Ledger.route,       "Cash & Bank",  "📒", Icons.Rounded.AccountBalance,      section = "FINANCE"),

    // ── PEOPLE ──
    DrawerNavItem(AppRoute.Customers.route,    "Customers",    "👥", Icons.Rounded.Group,               section = "PEOPLE"),
    DrawerNavItem(AppRoute.Suppliers.route,    "Suppliers",    "🏭", Icons.Rounded.LocalShipping,       section = "PEOPLE"),

    // ── SERVICE ──
    DrawerNavItem(AppRoute.Repairs.route,      "Repairs",      "🔧", Icons.Rounded.Build,               section = "SERVICE"),

    // ── BUSINESS ──
    DrawerNavItem(AppRoute.Reports.route,      "Reports",      "📊", Icons.Rounded.Analytics,           section = "BUSINESS"),
    DrawerNavItem(AppRoute.Settings.route,     "Settings",     "⚙️", Icons.Rounded.Settings,            section = "BUSINESS")
)

private val sectionOrder   = listOf("MAIN", "FINANCE", "PEOPLE", "SERVICE", "BUSINESS")
private val sectionLabels  = mapOf(
    "MAIN"     to "Main",
    "FINANCE"  to "Finance",
    "PEOPLE"   to "People",
    "SERVICE"  to "Service",
    "BUSINESS" to "Business"
)

/**
 * Full sidebar drawer content.
 * Theme-aware and status bar safe.
 */
@Composable
fun ShopFlowDrawerContent(
    currentRoute : String?,
    ownerName    : String  = "Ahmad Raza",
    ownerInitials: String  = "A",
    shopRole     : String  = "Shop Owner",
    onNavigate   : (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    ModalDrawerSheet(
        modifier           = Modifier.width(280.dp).fillMaxHeight(),
        drawerContainerColor = colors.surface,
        drawerContentColor = colors.onSurface,
        drawerShape = RoundedCornerShape(0.dp) // Flat design matching dashboard
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Logo Header ───────────────────────────────────────────────────────
            // Removed redundant background and added statusBarsPadding directly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gradient logo icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(colors.primary, colors.secondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📱", fontSize = 20.sp)
                }
                Column {
                    Row {
                        Text("ShopFlow", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.onSurface, letterSpacing = (-0.5).sp)
                        Text(" POS", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary, letterSpacing = (-0.5).sp)
                    }
                    Text("Mobile Shop Manager", fontSize = 11.sp, color = colors.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = colors.outline.copy(alpha = 0.1f))

            // ── Nav Sections (scrollable) ─────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val grouped = allDrawerItems.groupBy { it.section }
                sectionOrder.forEach { section ->
                    val items = grouped[section] ?: return@forEach

                    // Section title label
                    Text(
                        text     = sectionLabels[section] ?: section,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color    = colors.onSurfaceVariant.copy(alpha = 0.6f),
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 16.dp, bottom = 8.dp)
                    )

                    items.forEach { item ->
                        DrawerNavRow(
                            item         = item,
                            isActive     = currentRoute == item.route,
                            onClick      = { onNavigate(item.route) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Footer ────────────────────────────────────────────────────────────
            HorizontalDivider(color = colors.outline.copy(alpha = 0.1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppRoute.Settings.route) }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .navigationBarsPadding(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Gradient avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(colors.primary, colors.secondary))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ownerInitials, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                // Owner info
                Column(modifier = Modifier.weight(1f)) {
                    Text(ownerName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(shopRole,  fontSize = 12.sp, color = colors.onSurfaceVariant)
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Single nav row ─────────────────────────────────────────────────────────────────
@Composable
private fun DrawerNavRow(
    item    : DrawerNavItem,
    isActive: Boolean,
    onClick : () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) colors.primaryContainer else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon box
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isActive) colors.primary else colors.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(item.emoji, fontSize = 16.sp)
        }

        // Label
        Text(
            text       = item.label,
            fontSize   = 14.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold,
            color      = if (isActive) colors.primary else colors.onSurface,
            modifier   = Modifier.weight(1f)
        )

        // Optional badge
        if (item.badge != null) {
            Surface(
                color = if (isActive) colors.primary else colors.error,
                shape = RoundedCornerShape(100.dp)
            ) {
                Text(
                    text     = item.badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color    = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}