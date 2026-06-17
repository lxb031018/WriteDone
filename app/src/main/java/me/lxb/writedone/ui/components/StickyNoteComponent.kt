package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.util.FormatUtils
import java.util.Calendar
import java.util.Date
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun StickyNoteInput(
    value: String,
    onValueChange: (String) -> Unit,
    createdAt: Date?,
    durationSeconds: Int?,
    breathingEnabled: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val seed = remember { abs(java.util.Objects.hash(value)) }
    val rng = remember { Random(seed.toLong()) }
    val rotationDeg = remember { (rng.nextDouble() - 0.5) * 4.0 }
    val colorIndex = remember { rng.nextInt(AppColors.macaronPalette.size) }

    val t = LocalAmbientProgress.current
    val breathingAlpha = LocalBreathingAlpha.current

    val bgColor = lerp(
        AppColors.macaronPalette[colorIndex],
        AppColors.darkMacaronPalette[colorIndex],
        t,
    )
    val headerColor = lerp(AppColors.textMuted, AppColors.darkText.copy(alpha = 0.15f), t)
    val borderColor = lerp(AppColors.border, AppColors.darkBorder, t)
    val textColor = lerp(AppColors.text, AppColors.darkText, t)
    val hintColor = lerp(AppColors.textMuted.copy(alpha = 0.4f), AppColors.darkTextMuted, t)
    val cursorColor = lerp(AppColors.accent, AppColors.darkAccent, t)

    val handwritingFont = FontFamily(
        Font(
            googleFont = GoogleFont("ZCOOL KuaiLe"),
            fontProvider = GoogleFont.Provider(
                providerAuthority = "com.google.android.gms.fonts",
                providerPackage = "com.google.android.gms",
                certificates = R.array.com_google_android_gms_fonts_certs,
            ),
        ),
    )

    val headerText = remember(createdAt, durationSeconds) {
        buildString {
            if (createdAt != null) {
                val cal = Calendar.getInstance().apply { time = createdAt }
                append("${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日    ")
                append("开始:${FormatUtils.time(createdAt)}")
            } else {
                append("--年--月--日    开始:--:--")
            }
            if (durationSeconds != null) {
                append("    ")
                append("用时:${FormatUtils.duration(durationSeconds)}")
            } else {
                append("    ")
                append("用时:--")
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        BreathingWrapper(enabled = breathingEnabled, alpha = breathingAlpha) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(rotationDeg.toFloat())
                    .stickyNoteShadow(bgColor)
                    .background(color = bgColor, shape = RoundedCornerShape(4.dp))
                    .padding(Dimens.cardPad),
            ) {
                Text(
                    text = headerText,
                    fontFamily = handwritingFont,
                    fontSize = 13.sp,
                    color = headerColor,
                )
                Spacer(Modifier.height(Dimens.gap))
                HorizontalDivider(color = borderColor, thickness = 1.dp)
                Spacer(Modifier.height(Dimens.gap))
                val readOnly = !enabled
                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= 60) onValueChange(it) },
                    readOnly = readOnly,
                    textStyle = TextStyle(
                        fontFamily = handwritingFont,
                        fontSize = 22.sp,
                        color = textColor,
                    ),
                    cursorBrush = if (readOnly) SolidColor(cursorColor.copy(alpha = 0f)) else SolidColor(cursorColor),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty() && !readOnly) {
                                Text(
                                    text = "准备好了嘛^_^",
                                    fontFamily = handwritingFont,
                                    fontSize = 22.sp,
                                    color = hintColor,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
            }
        }
    }
}
