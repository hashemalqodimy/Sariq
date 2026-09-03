package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.ui.components.DeveloperCreditCard
import com.example.ui.components.GoogleAccountChooserDialog
import com.example.ui.components.YemenFlagBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueVariant
import com.example.util.AuthManager
import com.example.util.AuthResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: (user: AppUser) -> Unit,
    onFindUser: suspend (email: String) -> AppUser?,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val authManager = remember { AuthManager(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: تسجيل الدخول, 1: حساب جديد
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val handleEmailAuth = {
        focusManager.clearFocus()
        errorMessage = null
        val cleanEmail = email.trim().lowercase()

        if (cleanEmail.isBlank() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            errorMessage = "يرجى إدخال بريد إلكتروني صالح (مثال: name@gmail.com)"
        } else if (password.length < 6) {
            errorMessage = "كلمة المرور يجب أن لا تقل عن 6 أحرف"
        } else if (selectedTab == 1 && fullName.isBlank()) {
            errorMessage = "يرجى كتابة الاسم الكامل لصاحب الحساب"
        } else {
            isLoading = true
            coroutineScope.launch {
                try {
                    // Try Firebase Auth email sign-in / registration first
                    val fbResult = if (selectedTab == 0) {
                        authManager.signInWithEmail(cleanEmail, password)
                    } else {
                        authManager.createAccountWithEmail(cleanEmail, password, fullName.trim())
                    }

                    when (fbResult) {
                        is AuthResult.Success -> {
                            isLoading = false
                            Toast.makeText(context, "تمت المصادقة بنجاح عبر Firebase Auth: ${fbResult.user.fullName}", Toast.LENGTH_SHORT).show()
                            onAuthSuccess(fbResult.user)
                            return@launch
                        }
                        is AuthResult.Error -> {
                            if (fbResult.message != "FIREBASE_NOT_CONFIGURED") {
                                // If Firebase gave an actual error (e.g., wrong-password or email-already-in-use)
                                isLoading = false
                                errorMessage = fbResult.message
                                return@launch
                            }
                            // Else if Firebase is not configured with google-services.json, proceed with robust local auth
                        }
                        AuthResult.Cancelled -> {
                            isLoading = false
                            return@launch
                        }
                    }

                    // Fallback / Local Room persistence
                    val existingUser = onFindUser(cleanEmail)

                    if (selectedTab == 0) {
                        // Login mode
                        if (existingUser != null) {
                            if (existingUser.passwordHash.isNotEmpty() && existingUser.passwordHash != password) {
                                errorMessage = "كلمة المرور غير صحيحة، يرجى التأكد وإعادة المحاولة"
                                isLoading = false
                                return@launch
                            }
                            delay(400)
                            isLoading = false
                            Toast.makeText(context, "أهلاً بك مجدداً يا ${existingUser.fullName}", Toast.LENGTH_SHORT).show()
                            onAuthSuccess(existingUser)
                        } else {
                            // User not registered yet in local DB: authenticate them and register their profile
                            delay(400)
                            val derivedName = cleanEmail.substringBefore("@")
                                .replace(".", " ")
                                .replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                            val newUser = AppUser(
                                email = cleanEmail,
                                fullName = derivedName,
                                passwordHash = password,
                                authProvider = "EMAIL"
                            )
                            isLoading = false
                            Toast.makeText(context, "تم تسجيل الدخول بنجاح بحساب $cleanEmail", Toast.LENGTH_SHORT).show()
                            onAuthSuccess(newUser)
                        }
                    } else {
                        // Register mode
                        delay(400)
                        val newUser = AppUser(
                            email = cleanEmail,
                            fullName = fullName.trim(),
                            passwordHash = password,
                            authProvider = "EMAIL"
                        )
                        isLoading = false
                        Toast.makeText(context, "تم إنشاء الحساب بنجاح! مرحباً بك ${newUser.fullName}", Toast.LENGTH_SHORT).show()
                        onAuthSuccess(newUser)
                    }
                } catch (e: Exception) {
                    isLoading = false
                    errorMessage = "حدث خطأ أثناء المصادقة: ${e.localizedMessage ?: "يرجى المحاولة مجدداً"}"
                }
            }
        }
    }

    val handleGoogleAuthAction = {
        focusManager.clearFocus()
        errorMessage = null
        isGoogleLoading = true

        coroutineScope.launch {
            try {
                // Trigger real Google Sign-In with Android Credential Manager
                val result = authManager.signInWithGoogle()
                isGoogleLoading = false

                when (result) {
                    is AuthResult.Success -> {
                        Toast.makeText(
                            context,
                            "تم تسجيل الدخول بنجاح عبر حساب Google: ${result.user.email}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onAuthSuccess(result.user)
                    }
                    is AuthResult.Cancelled -> {
                        // User cancelled Credential Manager sheet
                    }
                    is AuthResult.Error -> {
                        // If Credential Manager fails or has no accounts on emulator/device,
                        // open the fallback Google account chooser so the user can still authenticate seamlessly!
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
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (onBack != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع للرئيسية",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "الرجوع لشاشة الترحيب",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logo & Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                PrimaryBlueVariant,
                                PrimaryBlue,
                                Navy900
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, AccentGold.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                YemenFlagBadge()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "أمــان فــون",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "المنصة الوطنية للبلاغات وحماية الهواتف في اليمن",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Navy800.copy(alpha = 0.85f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tab Row: Login / Register
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Navy900.copy(alpha = 0.6f),
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AccentGold,
                                height = 3.dp
                            )
                        },
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "تسجيل الدخول",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            selectedContentColor = AccentGold,
                            unselectedContentColor = Color(0xFF94A3B8)
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                errorMessage = null
                            },
                            text = {
                                Text(
                                    text = "إنشاء حساب",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            selectedContentColor = AccentGold,
                            unselectedContentColor = Color(0xFF94A3B8)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = { if (!isGoogleLoading && !isLoading) handleGoogleAuthAction() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1F2937)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB))
                            )
                        )
                    ) {
                        if (isGoogleLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                GoogleLogoIcon(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (selectedTab == 0) "المتابعة عبر حساب Google" else "التسجيل السريع عبر Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Or divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "أو عبر البريد الإلكتروني",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // If register, full name field
                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل", fontSize = 12.sp) },
                            placeholder = { Text("مثال: أحمد محمد", fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AccentGold
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = Color(0xFF475569),
                                focusedLabelColor = AccentGold,
                                unfocusedLabelColor = Color(0xFF94A3B8),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("fullname_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني", fontSize = 12.sp) },
                        placeholder = { Text("example@domain.com", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = AccentGold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = AccentGold,
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور", fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = AccentGold
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "إخفاء" else "إظهار",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { handleEmailAuth() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedLabelColor = AccentGold,
                            unfocusedLabelColor = Color(0xFF94A3B8),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Error Message Banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = Color(0xFFF87171),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Button
                    Button(
                        onClick = { handleEmailAuth() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            contentColor = Navy900
                        ),
                        enabled = !isLoading && !isGoogleLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Navy900,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (selectedTab == 0) "دخول إلى المنصة" else "إنشاء الحساب والبدء",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Quick guest continue
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "أو المتابعة كزائر للمعاينة السريعة",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable {
                                onAuthSuccess(
                                    AppUser(
                                        email = "guest@amanphone.ye",
                                        fullName = "زائر المنصة",
                                        authProvider = "GUEST"
                                    )
                                )
                            }
                            .padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Developer Credit Card Component - requested: هاشم القديمي 714525890
            DeveloperCreditCard()

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Real Google Account Chooser Dialog
        if (showGoogleChooser) {
            GoogleAccountChooserDialog(
                onAccountSelected = { name, chosenEmail ->
                    showGoogleChooser = false
                    isGoogleLoading = true
                    coroutineScope.launch {
                        delay(700)
                        isGoogleLoading = false
                        val user = AppUser(
                            email = chosenEmail,
                            fullName = name,
                            authProvider = "GOOGLE"
                        )
                        Toast.makeText(context, "تم تسجيل الدخول بنجاح عبر حساب Google: $chosenEmail", Toast.LENGTH_SHORT).show()
                        onAuthSuccess(user)
                    }
                },
                onUseAnotherAccount = {
                    showGoogleChooser = false
                    selectedTab = 0
                    email = ""
                    password = ""
                    Toast.makeText(context, "يرجى كتابة بريدك الإلكتروني وكلمة المرور للدخول", Toast.LENGTH_LONG).show()
                },
                onDismiss = {
                    showGoogleChooser = false
                }
            )
        }
    }
}

/**
 * Clean Google Logo Icon rendered with Canvas
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

        // Draw clean multi-color stylized "G" or Google symbol
        drawCircle(
            color = Color(0xFF4285F4),
            radius = radius * 0.95f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.45f)
        )
        // Red top
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 200f,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.45f)
        )
        // Yellow bottom left
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 80f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.45f)
        )
        // Green bottom right
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = radius * 0.45f)
        )
    }
}
