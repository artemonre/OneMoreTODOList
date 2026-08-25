package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons

@Composable
fun AppCheckToggle(
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
