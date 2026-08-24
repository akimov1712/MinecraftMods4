package dev.mod.store.minecraft.core.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.mod.store.minecraft.core.ui.theme.Palette

/** Themed snackbar host for one-shot notices (errors, confirmations). */
@Composable
fun NoticeHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        modifier = modifier,
        hostState = hostState,
    ) { data ->
        Snackbar(
            shape = RoundedCornerShape(14.dp),
            containerColor = Palette.SurfaceHigh,
            contentColor = Palette.TextPrimary,
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Palette.TextPrimary,
            )
        }
    }
}
