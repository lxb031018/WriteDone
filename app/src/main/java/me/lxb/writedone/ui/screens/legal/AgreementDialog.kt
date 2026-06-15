package me.lxb.writedone.ui.screens.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun AgreementDialog(
    onAgree: () -> Unit,
    onDisagree: () -> Unit,
    onShowUserAgreement: () -> Unit,
    onShowPrivacyPolicy: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDisagree,
        title = { Text("欢迎使用 WriteDone") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("请您仔细阅读以下条款。")
                Spacer(Modifier.height(8.dp))
                Text(
                    buildAnnotatedString {
                        append("在您开始使用前，请阅读并同意")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("《用户协议》")
                        }
                        append("和")
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append("《隐私政策》")
                        }
                        append("。")
                    },
                )
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onShowUserAgreement) {
                    Text("查看用户协议")
                }
                TextButton(onClick = onShowPrivacyPolicy) {
                    Text("查看隐私政策")
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "本应用是纯本地应用，不联网，不收集任何个人信息，未集成任何第三方 SDK。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onAgree) {
                Text("同意并继续")
            }
        },
        dismissButton = {
            TextButton(onClick = onDisagree) {
                Text("不同意")
            }
        },
    )
}
