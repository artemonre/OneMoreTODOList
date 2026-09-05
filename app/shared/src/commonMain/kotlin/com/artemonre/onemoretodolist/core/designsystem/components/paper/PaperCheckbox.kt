package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A Paper-styled checkbox: a hand-drawn filled/outlined circle rather than a stock Material3
 * [androidx.compose.material3.Checkbox].
 */
@Composable
fun PaperCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(
                        value = checked,
                        onValueChange = onCheckedChange,
                        role = Role.Checkbox
                    )
                } else {
                    Modifier
                }
            )
            .then(
                if (checked) {
                    Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = LocalAppIcons.current.checkIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(widthDp = 60, heightDp = 24)
@Composable
private fun PaperCheckboxPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        Row {
            PaperCheckbox(checked = false, onCheckedChange = {})
            Spacer(modifier = Modifier.width(12.dp))
            PaperCheckbox(checked = true, onCheckedChange = {})
        }
    }
}
