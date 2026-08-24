package dev.mod.store.minecraft.feature.outreach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.component.NoticeHost
import dev.mod.store.minecraft.core.ui.component.OutlineField
import dev.mod.store.minecraft.core.ui.component.PillButton
import dev.mod.store.minecraft.core.ui.component.SegmentedTabs
import dev.mod.store.minecraft.core.ui.theme.Palette
import dev.mod.store.minecraft.core.ui.util.ObserveSignals
import dev.mod.store.minecraft.feature.outreach.OutreachStore.Intent

@Composable
fun OutreachPane(
    component: OutreachComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val sentText = stringResource(R.string.outreach_sent)

    ObserveSignals(component.labels) { label ->
        when (label) {
            is OutreachStore.Label.Notify -> snackbar.showSnackbar(label.message)
            OutreachStore.Label.Sent -> snackbar.showSnackbar(sentText)
        }
    }

    val modes = listOf(OutreachMode.Recommendation, OutreachMode.Report)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.outreach_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Palette.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            SegmentedTabs(
                labels = modes.map { stringResource(it.labelRes()) },
                selectedIndex = modes.indexOf(state.mode),
                onSelect = { component.onIntent(Intent.SelectMode(modes[it])) },
            )

            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlineField(
                    value = state.email,
                    onValueChange = { component.onIntent(Intent.ChangeEmail(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = stringResource(R.string.outreach_email_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlineField(
                    value = state.message,
                    onValueChange = { component.onIntent(Intent.ChangeMessage(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = stringResource(messageHintFor(state.mode)),
                    singleLine = false,
                    minLines = 4,
                )
                PillButton(
                    text = stringResource(R.string.outreach_submit),
                    onClick = { component.onIntent(Intent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.canSubmit,
                    busy = state.sending,
                )
            }
        }

        NoticeHost(
            hostState = snackbar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}

private fun OutreachMode.labelRes(): Int = when (this) {
    OutreachMode.Recommendation -> R.string.outreach_mode_recommend
    OutreachMode.Report -> R.string.outreach_mode_report
}

private fun messageHintFor(mode: OutreachMode): Int = when (mode) {
    OutreachMode.Recommendation -> R.string.outreach_message_recommend_hint
    OutreachMode.Report -> R.string.outreach_message_report_hint
}
