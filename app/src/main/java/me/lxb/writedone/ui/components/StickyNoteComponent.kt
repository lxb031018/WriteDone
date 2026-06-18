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
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import me.lxb.writedone.R
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.ui.theme.LocalDarkTheme
import me.lxb.writedone.util.FormatUtils
import java.util.Calendar
import java.util.Date

import kotlin.random.Random

private const val MAX_INPUT_LENGTH = 60

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
    val seed = remember { java.util.Objects.hash(value) }
    val rng = remember { Random(seed.toLong()) }
    val rotationDeg = remember { (rng.nextDouble() - 0.5) * 4.0 }
    val colorIndex = remember { rng.nextInt(AppColors.macaronPalette.size) }

    val ambientProgress = LocalAmbientProgress.current
    val breathingAlpha = LocalBreathingAlpha.current

    val colorScheme = MaterialTheme.colorScheme
    val dateFormatStr = stringResource(R.string.sticky_header_date)
    val headerStart = stringResource(R.string.sticky_header_start)
    val emptyDateStr = stringResource(R.string.sticky_header_empty_date)
    val headerDuration = stringResource(R.string.sticky_header_duration)
    val durationEmptyStr = stringResource(R.string.sticky_duration_empty)

    val isDark = LocalDarkTheme.current
    val bgColor = lerp(
        if (isDark) AppColors.darkMacaronPalette[colorIndex] else AppColors.macaronPalette[colorIndex],
        AppColors.ambientMacaronPalette[colorIndex],
        ambientProgress,
    )
    val headerColor = lerp(
        if (isDark) AppColors.darkTextMuted else AppColors.textMuted,
        AppColors.ambientText.copy(alpha = 0.15f),
        ambientProgress,
    )
    val borderColor = colorScheme.outline
    val textColor = colorScheme.onSurface
    val hintColor = lerp(
        if (isDark) AppColors.darkTextMuted else AppColors.textMuted.copy(alpha = 0.4f),
        AppColors.ambientTextMuted,
        ambientProgress,
    )
    val cursorColor = colorScheme.primary

    val headerText = remember(createdAt, durationSeconds, dateFormatStr, headerStart, emptyDateStr, headerDuration, durationEmptyStr) {
        buildString {
            if (createdAt != null) {
                val cal = Calendar.getInstance().apply { time = createdAt }
                append(String.format(dateFormatStr, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)))
                append("$headerStart${FormatUtils.time(createdAt)}")
            } else {
                append(emptyDateStr)
            }
            if (durationSeconds != null) {
                append("    ")
                append("$headerDuration${FormatUtils.duration(durationSeconds)}")
            } else {
                append("    ")
                append(durationEmptyStr)
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
                    onValueChange = { if (it.length <= MAX_INPUT_LENGTH) onValueChange(it) },
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
                                    text = stringResource(R.string.sticky_placeholder),
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
