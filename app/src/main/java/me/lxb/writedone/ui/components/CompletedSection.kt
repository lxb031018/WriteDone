package me.lxb.writedone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
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
fun CompletedSection(
    notes: List<CompletedNote>,
    emptyText: String = "轻触上方计时器，开始第一件事",
    headerText: String = "已完成 — ${notes.size}",
    showHeader: Boolean = true,
    breathingEnabled: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val t = LocalAmbientProgress.current
    val emptyTextColor = lerp(AppColors.textMuted, AppColors.darkTextMuted, t)

    val listState = rememberLazyListState()

    LaunchedEffect(notes.firstOrNull()?.id) {
        if (notes.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (showHeader) {
            SectionHeader(text = headerText, breathingEnabled = breathingEnabled)
            Spacer(Modifier.height(Dimens.gapMd))
        }

        if (notes.isEmpty()) {
            Text(
                text = emptyText,
                fontFamily = handwritingFont,
                fontSize = 14.sp,
                color = emptyTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(vertical = Dimens.gapLg)
                    .align(Alignment.CenterHorizontally),
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.padding(bottom = Dimens.pageBottom),
            ) {
                itemsIndexed(
                    items = notes,
                    key = { _, note -> note.id },
                ) { _, note ->
                    CompletedCard(note = note, breathingEnabled = breathingEnabled)
                    Spacer(Modifier.height(Dimens.gap))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    text: String,
    breathingEnabled: Boolean,
) {
    val t = LocalAmbientProgress.current
    val breathingAlpha = LocalBreathingAlpha.current
    val headerTextColor = lerp(AppColors.textMuted, AppColors.darkTextMuted, t)
    BreathingWrapper(enabled = breathingEnabled, alpha = breathingAlpha) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = Dimens.gap),
                fontFamily = handwritingFont,
                fontSize = 14.sp,
                color = headerTextColor,
            )
        }
    }
}

@Composable
fun CompletedCard(
    note: CompletedNote,
    breathingEnabled: Boolean,
) {
    val seed = remember { abs(note.id.toInt()) + note.content.hashCode() }
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
    val headerTextColor = lerp(AppColors.textMuted, AppColors.darkText.copy(alpha = 0.15f), t)
    val dividerColor = lerp(AppColors.border, AppColors.darkBorder, t)
    val textColor = lerp(AppColors.text, AppColors.darkText, t)

    val headerText = remember(note) {
        val cal = Calendar.getInstance().apply { time = Date(note.createdAt) }
        buildString {
            append("${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日    ")
            append("开始:${FormatUtils.time(Date(note.createdAt))}")
            append("    ")
            append("用时:${FormatUtils.duration(note.durationSeconds)}")
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
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
                    color = headerTextColor,
                )
                Spacer(Modifier.height(Dimens.gap))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(Modifier.height(Dimens.gap))
                Text(
                    text = note.content,
                    fontFamily = handwritingFont,
                    fontSize = 22.sp,
                    color = textColor,
                )
            }
        }
    }
}
