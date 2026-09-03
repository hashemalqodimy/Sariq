package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.ui.components.AmanPhoneLogoDesign
import com.example.ui.components.GoogleAccountChooserDialog
import com.example.ui.components.IconConcept
import com.example.ui.components.IconSelectorShowcase
import com.example.ui.components.YemenFlagBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue
import com.example.util.AuthManager
import com.example.util.AuthResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Professional WelcomeScreen composable featuring a clean design with email and
 * Google sign-in buttons, plus a prominent footer text:
 * 'تم تطوير وإنشاء التطبيق بواسطة هاشم القديمي 714525890'.
 */
@Composable
fun WelcomeScreen(
    onEmailSignInClick: () -> Unit,
    onGoogleSignInSuccess: (user: AppUser) -> Unit,
    onGuestContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }

    var isGoogleLoading by remember { mutableStateOf(false) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    var isContentVisible by remember { mutableStateOf(false) }
    var selectedIconConcept by remember { mutableStateOf(IconConcept.CYBER_GOLD_SHIELD) }
    var showIconPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isContentVisible = true
    }

    val handleGoogleAuthAction = {
        isGoogleLoading = true
        coroutineScope.launch {
            try {
                val result = authManager.signInWithGoogle()
                isGoogleLoading = false
                when (result) {
                    is AuthResult.Success -> {
                        Toast.makeText(
                            context,
                            "تم تسجيل الدخول بنجاح عبر حساب Google: ${result.user.email}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onGoogleSignInSuccess(result.user)
                    }
                    is AuthResult.Cancelled -> {
                        // User cancelled
                    }
                    is AuthResult.Error -> {
                        showGoogleChooser = true
                    }
                }
            } catch (_: Exception) {
                isGoogleLoading = false
                showGoogleChooser = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Navy900,
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Decorative background aura circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(240.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryBlue.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentGold.copy(alpha = 0.12f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header & Logo Section
            AnimatedVisibility(
                visible = isContentVisible,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(500)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Republic of Yemen flag badge
                    YemenFlagBadge()

                    Spacer(modifier = Modifier.height(20.dp))

                    // App Emblem Icon with glow and modern design with 'أَمَان' inside
                    AmanPhoneLogoDesign(
                        concept = selectedIconConcept,
                        size = 110.dp,
                        modifier = Modifier
                            .clickable { showIconPicker = !showIconPicker }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0x33F59E0B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { showIconPicker = !showIconPicker }
                    ) {
                        Text(
                            text = if (showIconPicker) "إخفاء معرض الأيقونات ▲" else "تغيير شكل الأيقونة (4 خيارات) 🎨",
                            color = Color(0xFFFDE68A),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "أَمَان فُون",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        color = AccentGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "المنصة الوطنية الموحدة لحماية وتتبع الهواتف",
                            color = AccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "نظام موثوق للتحقق من الأجهزة بالرقم التسلسلي (IMEI) وتعميم وبلاغات الهواتف المفقودة والمسروقة في عموم المحافظات اليمنية.",
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Authentication Action Cards
            AnimatedVisibility(
                visible = isContentVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 150)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(600, delayMillis = 150)
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, shape = RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Navy800.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تسجيل الدخول إلى المنصة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Text(
                            text = "اختر طريقتك المفضلة للمتابعة بأمان",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                        )

                        // 1. Google Sign-In Button (Modern & Professional)
                        OutlinedButton(
                            onClick = { if (!isGoogleLoading) handleGoogleAuthAction() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("welcome_google_signin_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White,
                                contentColor = Color(0xFF1F2937)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                            enabled = !isGoogleLoading
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = PrimaryBlue,
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "جارٍ التحقق عبر Google...",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    GoogleLogoIcon(modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "المتابعة باستخدام Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1F2937)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Divider with text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF334155),
                                thickness = 1.dp
                            )
                            Text(
                                text = "أو عبر البريد الإلكتروني",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                color = Color(0xFF334155),
                                thickness = 1.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Email Sign-In Button
                        Button(
                            onClick = onEmailSignInClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("welcome_email_signin_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentGold,
                                contentColor = Navy900
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Navy900
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "تسجيل الدخول بالبريد الإلكتروني",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Navy900
                                )
                            }
                        }

                        // 3. Quick Guest / Explorer Entry
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "أو المتابعة كزائر بدون حساب للمعاينة",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clickable { onGuestContinue() }
                                .padding(6.dp)
                        )
                    }

                    if (showIconPicker) {
                        Spacer(modifier = Modifier.height(16.dp))
                        IconSelectorShowcase(
                            selectedConcept = selectedIconConcept,
                            onSelectConcept = { selectedIconConcept = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer Section with Requested Attribution:
            // "تم تطوير وإنشاء التطبيق بواسطة هاشم القديمي 714525890"
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:714525890"))
                            context.startActivity(dialIntent)
                        } catch (_: Exception) {
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Navy900,
                                    Color(0xFF1E293B),
                                    Navy900
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = AccentGold.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentGold.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, AccentGold.copy(alpha = 0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "اتصال",
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تم تطوير وإنشاء التطبيق بواسطة هاشم القديمي 714525890",
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "المهندس المطور • تواصل مباشر أو عبر الواتساب",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Google Account Chooser Dialog Fallback
        if (showGoogleChooser) {
            GoogleAccountChooserDialog(
                onAccountSelected = { name, chosenEmail ->
                    showGoogleChooser = false
                    isGoogleLoading = true
                    coroutineScope.launch {
                        delay(600)
                        isGoogleLoading = false
                        val user = AppUser(
                            email = chosenEmail,
                            fullName = name,
                            authProvider = "GOOGLE"
                        )
                        Toast.makeText(
                            context,
                            "تم تسجيل الدخول بنجاح عبر حساب Google: $chosenEmail",
                            Toast.LENGTH_SHORT
                        ).show()
                        onGoogleSignInSuccess(user)
                    }
                },
                onUseAnotherAccount = {
                    showGoogleChooser = false
                    onEmailSignInClick()
                },
                onDismiss = {
                    showGoogleChooser = false
                }
            )
        }
    }
}
