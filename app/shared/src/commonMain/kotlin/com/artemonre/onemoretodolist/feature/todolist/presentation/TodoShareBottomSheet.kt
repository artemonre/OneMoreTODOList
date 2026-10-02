package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppBottomSheet
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.share_coming_soon
import onemoretodolist.app.shared.generated.resources.share_make_note
import onemoretodolist.app.shared.generated.resources.share_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TodoShareBottomSheet(
    itemText: String,
    onShareClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        showDragHandle = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.l)
        ) {
            Text(
                text = itemText,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.xl, top = 20.dp, end = AppSpacing.xl, bottom = AppSpacing.m)
            )
            HorizontalDivider()
            ShareSheetRow(
                icon = Icons.Filled.Share,
                label = stringResource(Res.string.share_title),
                onClick = {
                    onShareClick()
                    onDismiss()
                }
            )
            ShareSheetRow(
                icon = Icons.Filled.Create,
                label = stringResource(Res.string.share_make_note),
                caption = stringResource(Res.string.share_coming_soon),
                enabled = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun ShareSheetRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
    enabled: Boolean = true
) {
    val contentColor = if (enabled) {
        LocalContentColor.current
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = AppSpacing.l, vertical = AppSpacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Spacer(Modifier.width(AppSpacing.l))
        Column {
            Text(text = label, color = contentColor, style = MaterialTheme.typography.bodyLarge)
            if (caption != null) {
                Text(text = caption, color = contentColor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview
@Composable
private fun TodoShareBottomSheetPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        TodoShareBottomSheet(
            itemText = "Write project architecture document covering module boundaries, data flow, and testing strategy for the new feature",
            onShareClick = {},
            onDismiss = {}
        )
    }
}
