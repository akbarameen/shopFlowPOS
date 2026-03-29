package com.matechmatrix.shopflows.feature.auth.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.ui.components.ShopFlowTextField
import com.matechmatrix.shopflowpos.core.ui.theme.Accent
import com.matechmatrix.shopflowpos.core.ui.theme.Danger
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onActivated: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var licenseVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(AuthIntent.CheckSession)
        viewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToDashboard -> onActivated()
            }
        }
    }

    // ── Full-screen gradient background ──────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D0527),
                        Color(0xFF1A0A3E),
                        Color(0xFF2D1480),
                        Primary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        // Decorative blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .background(
                    Brush.radialGradient(listOf(Accent.copy(alpha = 0.15f), Color.Transparent))
                )
        )
        Box(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(listOf(Primary.copy(alpha = 0.3f), Color.Transparent))
                )
        )

        // ── Login card ────────────────────────────────────────────────────────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .padding(horizontal = 20.dp)
                .imePadding(),
            shape     = RoundedCornerShape(28.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Logo ──────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(listOf(Primary, Accent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Rounded.PointOfSale,
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(38.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // ── Title ─────────────────────────────────────────────────────
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                            append("ShopFlow")
                        }
                        withStyle(SpanStyle(color = Primary)) {
                            append(" POS")
                        }
                    },
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text      = "Enter your license details to activate",
                    style     = MaterialTheme.typography.bodySmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(32.dp))

                // ── Shop Name field ───────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text     = "Shop Name",
                        style    = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ShopFlowTextField(
                        value         = state.shopName,
                        onValueChange = { viewModel.onIntent(AuthIntent.OnShopNameChanged(it)) },
                        placeholder   = "e.g. Ahmed Mobile Center",
                        keyboardType  = KeyboardType.Text,
                        modifier      = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── License Key field ─────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text     = "License Key",
                        style    = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ShopFlowTextField(
                        value               = state.licenseKey,
                        onValueChange       = { viewModel.onIntent(AuthIntent.OnLicenseKeyChanged(it)) },
                        placeholder         = "XXXX-XXXX-XXXX-XXXX",
                        keyboardType        = KeyboardType.Ascii,
                        visualTransformation = if (licenseVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        isError             = state.errorMessage != null,
                        modifier            = Modifier.fillMaxWidth(),
                        trailingContent     = {
                            IconButton(onClick = { licenseVisible = !licenseVisible }) {
                                Icon(
                                    imageVector = if (licenseVisible) Icons.Rounded.Visibility
                                    else Icons.Rounded.VisibilityOff,
                                    contentDescription = "Toggle visibility",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }

                // ── Error message ─────────────────────────────────────────────
                AnimatedVisibility(
                    visible = state.errorMessage != null,
                    enter   = fadeIn(),
                    exit    = fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Danger.copy(alpha = 0.08f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Lock, null,
                            tint     = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text  = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Danger
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Activate button ───────────────────────────────────────────
                Button(
                    onClick  = { viewModel.onIntent(AuthIntent.OnActivateClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled  = !state.isLoading &&
                            state.shopName.isNotBlank() &&
                            state.licenseKey.isNotBlank(),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Primary,
                        contentColor           = Color.White,
                        disabledContainerColor = Primary.copy(alpha = 0.4f),
                        disabledContentColor   = Color.White.copy(alpha = 0.6f)
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(22.dp),
                            color       = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.Store, null, modifier = Modifier.size(18.dp))
                            Text(
                                text       = "Activate & Continue",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Footer ────────────────────────────────────────────────────
                Text(
                    text      = "Powered by Matech Matrix",
                    style     = MaterialTheme.typography.labelSmall,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
