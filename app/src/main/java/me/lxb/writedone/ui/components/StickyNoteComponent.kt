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
import me.lxb.writedone.util.FormatUtils
import java.util.Calendar
import java.util.Date
import kotlin.math.PI
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun StickyNoteInput(
    value: String,
    onValueChange: (String) -> Unit,
    createdAt: Date?,
    durationSeconds: Int?,
    breathingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val seed = remember { abs(java.util.Objects.hash(value)) }
    val rng = remember { Random(seed.toLong()) }
    val rotation = remember { (rng.nextDouble() - 0.5) * (4.0 * PI / 180.0) }
    val colorIndex = remember { rng.nextInt(AppColors.macaronPalette.size) }
    val bgColor = AppColors.macaronPalette[colorIndex]

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
            }
            if (durationSeconds != null) {
                append("    ")
                append("用时:${FormatUtils.duration(durationSeconds)}")
            }
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        BreathingWrapper(enabled = breathingEnabled) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .rotate(rotation.toFloat())
                    .background(color = bgColor, shape = RoundedCornerShape(4.dp))
                    .padding(Dimens.cardPad),
            ) {
                if (headerText.isNotEmpty()) {
                    Text(
                        text = headerText,
                        fontFamily = handwritingFont,
                        fontSize = 13.sp,
                        color = AppColors.textMuted,
                    )
                    Spacer(Modifier.height(Dimens.gap))
                    HorizontalDivider(color = AppColors.border, thickness = 1.dp)
                    Spacer(Modifier.height(Dimens.gap))
                }
                BasicTextField(
                    value = value,
                    onValueChange = { if (it.length <= 60) onValueChange(it) },
                    textStyle = TextStyle(
                        fontFamily = handwritingFont,
                        fontSize = 22.sp,
                        color = AppColors.text,
                    ),
                    cursorBrush = SolidColor(AppColors.accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (value.isEmpty()) {
                                Text(
                                    text = "准备好了嘛^_^",
                                    fontFamily = handwritingFont,
                                    fontSize = 22.sp,
                                    color = AppColors.textMuted.copy(alpha = 0.4f),
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
