package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 4 High-End App Icon Visual Prototypes for 'Aman Phone' (أَمَان فُون)
 * Renders modern, non-primitive icons with Arabic typography 'أَمَان' embedded directly in the design.
 */

enum class IconConcept(
    val id: String,
    val title: String,
    val badge: String,
    val description: String
) {
    CYBER_GOLD_SHIELD(
        id = "cyber_gold",
        title = "الدرع السيبراني والتاج الذهبي",
        badge = "الأكثر طلباً وفخامة ⭐",
        description = "درع كحلي ياقوتي محاط بإطار ذهبي مصقول 24K مع قفل أمان ذكي ونقش أَمَان الذهبي في الأسفل."
    ),
    RADAR_NEON_TRACKER(
        id = "radar_neon",
        title = "رادار التتبع الذكي النيون",
        badge = "تقني مستقبلي 📡",
        description = "هاتف معاصر تخرج من شاشته موجات رادارية فيروزية للتتبع والتعميم الفوري مع كلمة أَمَان المضيئة."
    ),
    NATIONAL_FALCON_CREST(
        id = "national_crest",
        title = "الختم الوطني والهوية الرسمية",
        badge = "هيبة حكومية رسمية 🇾🇪",
        description = "ختم ملكي رسمي مستوحى من نسر الجمهورية اليمنية مع حماية رقمية وشعار أَمَان فُون."
    ),
    MINIMAL_MODERN_A(
        id = "minimal_apple",
        title = "البساطة الفاخرة (Modern Minimalist)",
        badge = "نمط أبل وجوجل 💎",
        description = "شعار هندسي ناعم يدمج حرف A مع الدرع والهاتف بانسيابية عالية ووضوح فائق."
    )
}

@Composable
fun AmanPhoneLogoDesign(
    concept: IconConcept = IconConcept.CYBER_GOLD_SHIELD,
    size: Dp = 88.dp,
    modifier: Modifier = Modifier
) {
    when (concept) {
        IconConcept.CYBER_GOLD_SHIELD -> CyberGoldShieldDesign(size, modifier)
        IconConcept.RADAR_NEON_TRACKER -> RadarNeonDesign(size, modifier)
        IconConcept.NATIONAL_FALCON_CREST -> NationalCrestDesign(size, modifier)
        IconConcept.MINIMAL_MODERN_A -> MinimalModernDesign(size, modifier)
    }
}

/**
 * Concept 1: Cyber Shield & Gold Trim with 'أَمَان' text badge
 */
@Composable
private fun CyberGoldShieldDesign(size: Dp, modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0xFFF59E0B), ambientColor = Color(0xFF1E3A8A))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF070C18),
                        Color(0xFF020617)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                BorderStroke(
                    1.8.dp,
                    Brush.sweepGradient(
                        listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFFDE68A),
                            Color(0xFFD97706),
                            Color(0xFFF59E0B)
                        )
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle cyber glow inside
        Box(
            modifier = Modifier
                .size(size * 0.75f)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Color(0xFF38BDF8).copy(alpha = glowAlpha * 0.4f), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp)
        ) {
            // Shield + Lock + Phone Composition
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(size * 0.52f)
                )
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(size * 0.36f)
                )
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFDE68A),
                    modifier = Modifier.size(size * 0.20f)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Typographic Brand Badge inside the icon
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.8.dp, Color(0xFFF59E0B))
            ) {
                Text(
                    text = "أَمَان",
                    color = Color(0xFFFDE68A),
                    fontSize = (size.value * 0.13f).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Concept 2: Radar Pulse & Cyan Neon Tracker
 */
@Composable
private fun RadarNeonDesign(size: Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0xFF06B6D4), ambientColor = Color(0xFF0284C7))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF030712),
                        Color(0xFF082F49),
                        Color(0xFF020617)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF38BDF8), Color(0xFF06B6D4), Color(0xFF0284C7))
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(vertical = 6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Radar rings
                Box(
                    modifier = Modifier
                        .size(size * 0.54f)
                        .border(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(size * 0.38f)
                        .border(1.4.dp, Color(0xFF06B6D4).copy(alpha = 0.7f), CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(size * 0.42f)
                )
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.24f)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Surface(
                color = Color(0x330284C7),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(0.8.dp, Color(0xFF38BDF8))
            ) {
                Text(
                    text = "أَمَان فُون",
                    color = Color(0xFFE0F2FE),
                    fontSize = (size.value * 0.11f).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * Concept 3: Official National Crest (Republic of Yemen Seal Style)
 */
@Composable
private fun NationalCrestDesign(size: Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0xFFD97706), ambientColor = Color(0xFF1E3A8A))
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A),
                        Color(0xFF030712)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                BorderStroke(2.dp, Color(0xFFD97706)),
                shape = RoundedCornerShape(26.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(vertical = 5.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(size * 0.48f)
                )
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Yemen flag bar indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .width(size * 0.45f)
                    .height(2.5.dp)
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color(0xFFCE1126)))
                Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color.White))
                Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color.Black))
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "أَمَان فُون",
                color = Color(0xFFFDE68A),
                fontSize = (size.value * 0.12f).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Concept 4: Modern Minimalist Geometry
 */
@Composable
private fun MinimalModernDesign(size: Dp, modifier: Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(16.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0xFF2563EB))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color(0xFF1D4ED8),
                        Color(0xFF1E3A8A),
                        Color(0xFF0F172A)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .border(
                BorderStroke(1.dp, Color(0x6660A5FA)),
                shape = RoundedCornerShape(26.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.46f)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.32f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AMAN",
                color = Color.White,
                fontSize = (size.value * 0.12f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "أَمَان",
                color = Color(0xFF93C5FD),
                fontSize = (size.value * 0.09f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Interactive Showcase Card for user to preview and pick an icon
 */
@Composable
fun IconSelectorShowcase(
    selectedConcept: IconConcept,
    onSelectConcept: (IconConcept) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, Color(0xFF1E3A8A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "اختر التصميم المفضل لأيقونة التطبيق",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = Color(0x33F59E0B),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "4 تصاميم",
                        color = Color(0xFFFDE68A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Display grid / list of concepts
            IconConcept.entries.forEach { concept ->
                val isSelected = concept == selectedConcept
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onSelectConcept(concept) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D31),
                    border = BorderStroke(
                        if (isSelected) 1.5.dp else 0.8.dp,
                        if (isSelected) Color(0xFFF59E0B) else Color(0xFF1E3A8A).copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Live Icon Preview
                        AmanPhoneLogoDesign(
                            concept = concept,
                            size = 62.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = concept.title,
                                    color = if (isSelected) Color(0xFFFDE68A) else Color.White,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = concept.badge,
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = concept.description,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }

                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "محدد",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
