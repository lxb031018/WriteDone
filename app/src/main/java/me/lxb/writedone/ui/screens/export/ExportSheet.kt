package me.lxb.writedone.ui.screens.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.data.repository.NoteRepository
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.util.ExportFormatter
import java.util.Calendar
import java.util.Date

enum class ExportRange { Today, ThisWeek }

@Composable
fun ExportSheet(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val t = LocalAmbientProgress.current

    var selectedRange by remember { mutableStateOf(ExportRange.Today) }
    var exportText by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRange) {
        val repo = NoteRepository(context)
        val cal = Calendar.getInstance()
        val (startMillis, endMillis) = when (selectedRange) {
            ExportRange.Today -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 1)
                start to cal.timeInMillis
            }
            ExportRange.ThisWeek -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 7)
                start to cal.timeInMillis
            }
        }
        val notes = repo.getByDateRange(startMillis, endMillis)
        exportText = when (selectedRange) {
            ExportRange.Today -> ExportFormatter.formatDaily(notes, Date())
            ExportRange.ThisWeek -> {
                val weekStart = Date(startMillis)
                val weekEnd = Date(endMillis - 1)
                ExportFormatter.formatWeekly(notes, weekStart, weekEnd)
            }
        }
        copied = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(lerp(AppColors.bg, AppColors.darkBg, t))
            .padding(horizontal = Dimens.cardPad, vertical = Dimens.pageH),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "导出时间记录",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = lerp(AppColors.text, AppColors.darkText, t),
            )
            Text(
                text = "关闭",
                fontSize = 16.sp,
                color = lerp(AppColors.textMuted, AppColors.darkTextMuted, t),
                modifier = Modifier.clickable(onClick = onClose),
            )
        }

        Spacer(Modifier.height(Dimens.gapMd))
        HorizontalDivider(color = lerp(AppColors.border, AppColors.darkBorder, t))
        Spacer(Modifier.height(Dimens.gapMd))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RangeButton(
                text = "今天",
                selected = selectedRange == ExportRange.Today,
                onClick = { selectedRange = ExportRange.Today },
                t = t,
            )
            RangeButton(
                text = "本周",
                selected = selectedRange == ExportRange.ThisWeek,
                onClick = { selectedRange = ExportRange.ThisWeek },
                t = t,
            )
        }

        Spacer(Modifier.height(Dimens.gapMd))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    lerp(AppColors.restBg, AppColors.darkBg, t),
                    RoundedCornerShape(8.dp),
                )
                .padding(Dimens.cardPad),
        ) {
            Text(
                text = exportText.ifEmpty { "Loading..." },
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = lerp(AppColors.text, AppColors.darkText, t),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            )
        }

        Spacer(Modifier.height(Dimens.gapMd))

        val bgColor = if (copied) AppColors.green else lerp(AppColors.accent, AppColors.darkAccent, t)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.btnPadV + 20.dp)
                .background(bgColor, RoundedCornerShape(Dimens.gap))
                .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Time Records", exportText))
                    copied = true
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (copied) "已复制" else "复制到剪贴板",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun RangeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    t: Float,
) {
    val bgColor = if (selected) {
        lerp(AppColors.accent, AppColors.darkAccent, t)
    } else {
        lerp(AppColors.restBg, AppColors.darkBg, t)
    }
    val textColor = if (selected) Color.White else lerp(AppColors.text, AppColors.darkText, t)

    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(bgColor, RoundedCornerShape(Dimens.gap))
            .padding(horizontal = Dimens.btnPadH, vertical = Dimens.btnPadV),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
        )
    }
}
