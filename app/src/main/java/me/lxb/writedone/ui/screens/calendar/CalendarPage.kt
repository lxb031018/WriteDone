package me.lxb.writedone.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.lxb.writedone.R
import me.lxb.writedone.data.model.CompletedNote
import me.lxb.writedone.ui.components.CalendarGrid
import me.lxb.writedone.ui.components.CompletedCard
import me.lxb.writedone.ui.theme.AppColors
import me.lxb.writedone.ui.theme.Dimens
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPage(
    onBack: (Date) -> Unit,
    selectedDate: Date,
    notes: List<CompletedNote>,
    onDateSelected: (Date) -> Unit,
) {
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

    val dateLabel = remember(selectedDate) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月${cal.get(Calendar.DAY_OF_MONTH)}日   共${notes.size}条"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历") },
                navigationIcon = {
                    IconButton(onClick = { onBack(selectedDate) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            CalendarGrid(
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                hasNotes = { false },
                modifier = Modifier.padding(horizontal = Dimens.pageH),
            )

            Spacer(Modifier.height(Dimens.gapMd))
            HorizontalDivider()
            Spacer(Modifier.height(Dimens.gapMd))

            Text(
                text = dateLabel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.pageH),
                fontFamily = handwritingFont,
                fontSize = 16.sp,
                color = AppColors.text,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(Dimens.gapMd))

            if (notes.isEmpty()) {
                Text(
                    text = "这天还没有记录",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.gapLg),
                    fontFamily = handwritingFont,
                    fontSize = 14.sp,
                    color = AppColors.textMuted,
                    textAlign = TextAlign.Center,
                )
            } else {
                notes.forEach { note ->
                    CompletedCard(note = note, breathingEnabled = false)
                    Spacer(Modifier.height(Dimens.gap))
                }
            }
        }
    }
}
