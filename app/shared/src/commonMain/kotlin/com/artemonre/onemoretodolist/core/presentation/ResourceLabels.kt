package com.artemonre.onemoretodolist.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// Chip groups, segmented controls and nav bars take a plain `(T) -> String` label lambda, which
// can't call stringResource itself - this resolves every option's label up front, in composition,
// and hands back a lookup that can be passed straight in as that lambda.
@Composable
fun <T> resourceLabels(options: List<T>, resource: (T) -> StringResource): (T) -> String {
    val labels = options.associateWith { stringResource(resource(it)) }
    return { labels.getValue(it) }
}
