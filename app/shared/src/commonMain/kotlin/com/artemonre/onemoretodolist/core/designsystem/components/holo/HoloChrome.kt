package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import com.artemonre.onemoretodolist.core.designsystem.components.AppStat
import com.artemonre.onemoretodolist.core.designsystem.components.FabMenuItem
import com.artemonre.onemoretodolist.core.designsystem.components.material.FAB_TRIGGER_SIZE
import com.artemonre.onemoretodolist.core.designsystem.components.material.FabMenuLayout
import com.artemonre.onemoretodolist.core.designsystem.components.material.FabMenuTriggerIcon
import com.artemonre.onemoretodolist.core.designsystem.components.material.fabMenuTriggerClicks
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.stat_label_value
import org.jetbrains.compose.resources.stringResource

/**
 * The Holo FAB look: a rounded square with a soft gradient, an outline and a glow in
 * [MaterialTheme.colorScheme.tertiary].
 */
@Composable
private fun Modifier.holoFab(shape: Shape): Modifier {
    val colors = MaterialTheme.colorScheme
    return this
        .size(FAB_TRIGGER_SIZE)
        .shadow(HoloDefaults.FabGlowElevation, shape, ambientColor = colors.tertiary, spotColor = colors.tertiary)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(colors.tertiaryContainer, colors.surface)))
        .border(HoloDefaults.BorderWidth, colors.tertiary, shape)
}

/** A Holo floating action button. */
@Composable
fun HoloFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(HoloDefaults.FabCornerRadius)
    Box(
        modifier = modifier
            .holoFab(shape)
            .clickable(onClick = onClick, role = Role.Button),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.onTertiaryContainer)
    }
}

/** The Holo FAB menu: the shared menu layout with a [HoloFab]-styled trigger. */
@Composable
fun HoloFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    icon: ImageVector,
    items: List<FabMenuItem>,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    FabMenuLayout(expanded, onExpandedChange, items, modifier) {
        val shape = RoundedCornerShape(HoloDefaults.FabCornerRadius)
        Box(
            modifier = Modifier
                .holoFab(shape)
                .fabMenuTriggerClicks(expanded, onExpandedChange, onClick),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onTertiaryContainer) {
                FabMenuTriggerIcon(icon, contentDescription, expanded)
            }
        }
    }
}

/**
 * The Holo navigation bar: Material3's compact bar over a frosted background, with a line along
 * its top edge and spaced-out capital labels.
 */
@Composable
fun <T> HoloNavigationBar(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (T) -> ImageVector,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    ShortNavigationBar(
        modifier = modifier.drawBehind {
            val line = HoloDefaults.BorderWidth.toPx()
            drawLine(colors.primary, Offset(0f, line / 2), Offset(size.width, line / 2), strokeWidth = line)
        },
        containerColor = colors.primaryContainer.copy(alpha = HoloDefaults.FROST_ALPHA)
    ) {
        items.forEachIndexed { index, item ->
            val text = label(item)
            ShortNavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                // The label below already names the item - a description here would be read twice.
                icon = { Icon(imageVector = icon(item), contentDescription = null) },
                label = {
                    Text(
                        text = text.uppercase(),
                        letterSpacing = HoloDefaults.LabelLetterSpacing,
                        modifier = Modifier.clearAndSetSemantics { contentDescription = text }
                    )
                },
                colors = ShortNavigationBarItemDefaults.colors(selectedIndicatorColor = colors.secondaryContainer)
            )
        }
    }
}

/**
 * The Holo stats card: a frosted gradient card with a `> TITLE` heading and one tile per stat,
 * label above a big number. TalkBack reads the title and each "label value" as given, without the
 * glyph or the capitals.
 */
@Composable
fun HoloStatsCard(
    title: String,
    stats: List<AppStat>,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(HoloDefaults.HeroCardCornerRadius)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.primaryContainer.copy(alpha = HoloDefaults.FROST_ALPHA),
                        colors.tertiaryContainer.copy(alpha = HoloDefaults.FROST_ALPHA)
                    )
                )
            )
            .border(HoloDefaults.BorderWidth, colors.primary.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA), shape)
            .drawBehind {
                // The short accent bar along the top-end edge.
                val bar = HoloDefaults.HeroAccentBarWidth.toPx()
                val thickness = HoloDefaults.HeroAccentBarThickness.toPx()
                val inset = HoloDefaults.HeroCardCornerRadius.toPx()
                val startX = if (isLtr) size.width - inset - bar else inset
                drawRect(colors.primary, Offset(startX, 0f), Size(bar, thickness))
            }
            .padding(AppSpacing.l)
    ) {
        Row(
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = title
                heading()
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = HoloDefaults.PROMPT_GLYPH,
                style = MaterialTheme.typography.titleLarge,
                color = colors.tertiary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(AppSpacing.s))
            Text(text = title.uppercase(), style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(AppSpacing.m))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.s), modifier = Modifier.fillMaxWidth()) {
            // The numbers take turns through these colors, like the mockup's dark / cyan / violet.
            val valueColors = listOf(colors.onSurface, colors.primary, colors.tertiary)
            stats.forEachIndexed { index, stat ->
                val spoken = stringResource(Res.string.stat_label_value, stat.label, stat.value)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clearAndSetSemantics { contentDescription = spoken }
                        .clip(HoloTileShape)
                        .background(colors.surface.copy(alpha = HoloDefaults.TILE_ALPHA))
                        .border(HoloDefaults.ThinBorderWidth, colors.primary.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA), HoloTileShape)
                        .padding(vertical = AppSpacing.m, horizontal = AppSpacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stat.label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        maxLines = 1
                    )
                    Text(
                        text = stat.value.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = valueColors[index % valueColors.size],
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 360)
@Composable
private fun HoloChromePreview() {
    HoloChromePreviewContent(ThemeMode.Light)
}

@Preview(widthDp = 360, heightDp = 360)
@Composable
private fun HoloChromeDarkPreview() {
    HoloChromePreviewContent(ThemeMode.Dark)
}

@Composable
private fun HoloChromePreviewContent(mode: ThemeMode) {
    AppTheme(themeConfig = ThemeConfig(mode = mode, uiStyle = UiStyleOption.Holo)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.l), modifier = Modifier.padding(AppSpacing.s)) {
            HoloStatsCard(
                title = "Completed todos",
                stats = listOf(AppStat("Today", 0), AppStat("This week", 1), AppStat("This month", 27)),
                modifier = Modifier.fillMaxWidth()
            )
            HoloFab(onClick = {}, icon = Icons.Filled.Add)
            HoloNavigationBar(
                items = listOf("Todo", "Settings"),
                selectedIndex = 0,
                onItemSelected = {},
                icon = { Icons.Filled.Add },
                label = { it }
            )
        }
    }
}
