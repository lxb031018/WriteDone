package me.lxb.writedone.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import me.lxb.writedone.R
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.ui.theme.ZcoolKuaiLeFont as handwritingFont
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import me.lxb.writedone.ui.theme.LocalAmbientProgress
import me.lxb.writedone.ui.theme.LocalBreathingAlpha
import me.lxb.writedone.ui.theme.LocalDarkTheme
import me.lxb.writedone.util.FormatUtils
import java.util.Date

import kotlin.random.Random

@Composable
fun CompletedSection(
    notes: List<CompletedNote>,
    emptyText: String = stringResource(R.string.completed_empty_text),
    headerText: String = stringResource(R.string.completed_header, notes.size),
    showHeader: Boolean = true,
    breathingEnabled: Boolean = false,
    onNoteContentChange: ((Long, String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val emptyTextColor = colorScheme.onSurfaceVariant

    val listState = rememberLazyListState()

    LaunchedEffect(notes.firstOrNull()?.id) {
        if (notes.isNotEmpty() && listState.firstVisibleItemIndex <= 1) {
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
                    CompletedCard(
                        note = note,
                        breathingEnabled = breathingEnabled,
                        onContentChange = onNoteContentChange,
                    )
                    Spacer(Modifier.height(Dimens.gap))
                }
            }
        }
    }
}

@Composable
private fun EditNoteDialog(
    currentContent: String,
    bgColor: Color,
    textColor: Color,
    cursorColor: Color,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf(currentContent) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = bgColor, shape = RoundedCornerShape(12.dp))
                .padding(Dimens.cardPad),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                textStyle = TextStyle(
                    fontFamily = handwritingFont,
                    fontSize = 22.sp,
                    color = textColor,
                ),
                cursorBrush = SolidColor(cursorColor),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Dimens.gapMd))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.calendar_cancel),
                        fontFamily = handwritingFont,
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = 0.6f),
                    )
                }
                Spacer(Modifier.width(Dimens.gap))
                TextButton(onClick = { onConfirm(text) }) {
                    Text(
                        text = stringResource(R.string.confirm),
                        fontFamily = handwritingFont,
                        fontSize = 16.sp,
                        color = textColor,
                    )
                }
            }
        }
    }
}
@Composable
private fun SectionHeader(
    text: String,
    breathingEnabled: Boolean,
) {
    val colorScheme = MaterialTheme.colorScheme
    val breathingAlpha = LocalBreathingAlpha.current
    val headerTextColor = colorScheme.onSurfaceVariant
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompletedCard(
    modifier: Modifier = Modifier,
    note: CompletedNote,
    breathingEnabled: Boolean,
    onContentChange: ((Long, String) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = LocalDarkTheme.current
    val seed = remember { note.id.hashCode() + note.content.hashCode() }
    val rng = remember { Random(seed.toLong()) }
    val rotationDeg = remember { (rng.nextDouble() - 0.5) * 4.0 }
    val colorIndex = remember { rng.nextInt(AppColors.macaronPalette.size) }

    val ambientProgress = LocalAmbientProgress.current
    val breathingAlpha = LocalBreathingAlpha.current
    val bgColor = lerp(
        if (isDark) AppColors.darkMacaronPalette[colorIndex] else AppColors.macaronPalette[colorIndex],
        AppColors.ambientMacaronPalette[colorIndex],
        ambientProgress,
    )
    val headerTextColor = lerp(
        if (isDark) AppColors.darkTextMuted else AppColors.textMuted,
        AppColors.ambientText.copy(alpha = 0.15f),
        ambientProgress,
    )
    val dividerColor = colorScheme.outline
    val textColor = colorScheme.onSurface
    val cursorColor = colorScheme.primary

    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth(),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val startDate = remember(note.createdAt) { Date(note.createdAt) }
                    val endDate = remember(note.createdAt, note.durationSeconds) { Date(note.createdAt + note.durationSeconds * 1000L) }
                    Text(
                        text = FormatUtils.formatDateWithDay(startDate),
                        fontFamily = handwritingFont,
                        fontSize = 13.sp,
                        color = headerTextColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                    )
                    Text(
                        text = "${FormatUtils.time(startDate)}-${FormatUtils.time(endDate)}",
                        fontFamily = handwritingFont,
                        fontSize = 13.sp,
                        color = headerTextColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = FormatUtils.duration(note.durationSeconds),
                        fontFamily = handwritingFont,
                        fontSize = 13.sp,
                        color = headerTextColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                    )
                }
                Spacer(Modifier.height(Dimens.gap))
                HorizontalDivider(color = dividerColor, thickness = 1.dp)
                Spacer(Modifier.height(Dimens.gap))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (onContentChange != null) showEditDialog = true },
                        ),
                ) {
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

    if (showEditDialog && onContentChange != null) {
        EditNoteDialog(
            currentContent = note.content,
            bgColor = bgColor,
            textColor = textColor,
            cursorColor = cursorColor,
            onConfirm = { newContent ->
                onContentChange(note.id, newContent)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false },
        )
    }
}
