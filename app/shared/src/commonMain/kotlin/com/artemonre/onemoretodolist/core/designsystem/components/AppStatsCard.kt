package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloStatsCard
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.stat_label_value
import org.jetbrains.compose.resources.stringResource

/** One number in an [AppStatsCard], e.g. "Today" and 3. */
data class AppStat(val label: String, val value: Int)

/**
 * A card with a [title] heading and a row of [stats]. Material shows each stat as one
 * line ("Today 3"); Holo shows tiles with the label above a big number.
 */
@Composable
fun AppStatsCard(
    title: String,
    stats: List<AppStat>,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> InlineStatsCard(title, stats, modifier)
        UiStyleOption.Holo -> HoloStatsCard(title, stats, modifier)
    }
}

@Composable
private fun InlineStatsCard(
    title: String,
    stats: List<AppStat>,
    modifier: Modifier
) {
    // Same color as the bottom navigation bar, so the card reads as part of the app chrome
    // rather than as another list row.
    AppCard(modifier = modifier, containerColor = ShortNavigationBarDefaults.containerColor) {
        // Extra padding on top of the card's own 8dp - 12dp total, same as the settings sections.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xs)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(AppSpacing.s))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stats.forEach { stat ->
                    Text(
                        text = stringResource(Res.string.stat_label_value, stat.label, stat.value),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
