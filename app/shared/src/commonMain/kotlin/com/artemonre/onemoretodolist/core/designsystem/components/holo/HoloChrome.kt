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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
 * The Holo FAB look: a rounded square with a soft gradient from Material's FAB color into
 * [MaterialTheme.colorScheme.surface], and an outline and glow in the bright tone of the accent
 * (see [brightPrimary]) so the edge reads as lit rather than drawn.
 */
@Composable
private fun Modifier.holoFab(shape: Shape): Modifier {
    val colors = MaterialTheme.colorScheme
    val glow = colors.brightPrimary()
    return this
        .size(FAB_TRIGGER_SIZE)
        .shadow(HoloDefaults.FabGlowElevation, shape, ambientColor = glow, spotColor = glow)
        .clip(shape)
        .background(Brush.verticalGradient(listOf(FloatingActionButtonDefaults.containerColor, colors.surface)))
        .border(HoloDefaults.BorderWidth, glow, shape)
}

/**
 * The bright tone of the accent in either mode: [ColorScheme.primary] in dark mode,
 * [ColorScheme.inversePrimary] (the dark scheme's primary) in light mode - whichever is lighter,
 * so it holds for every palette and for dynamic color without checking the mode.
 */
private fun ColorScheme.brightPrimary(): Color =
    if (inversePrimary.luminance() > primary.luminance()) inversePrimary else primary

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
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColorFor(FloatingActionButtonDefaults.containerColor)
        )
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
            CompositionLocalProvider(LocalContentColor provides contentColorFor(FloatingActionButtonDefaults.containerColor)) {
                FabMenuTriggerIcon(icon, contentDescription, expanded)
            }
        }
    }
}

/**
 * The Holo navigation bar: Material3's compact bar over a frosted version of its own background,
 * with a line along its top edge and spaced-out capital labels.
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
        containerColor = ShortNavigationBarDefaults.containerColor.copy(alpha = HoloDefaults.FROST_ALPHA)
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
                }
            )
        }
    }
}

/**
 * The Holo stats card: a frosted gradient from the Material stats card's color into
 * [MaterialTheme.colorScheme.surface], softly outlined in its accent's
 * [MaterialTheme.colorScheme.primary], with a `> TITLE` heading and one frosted tile per stat,
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
    // The same color as the Material stats card (the navigation bar's).
    val containerColor = ShortNavigationBarDefaults.containerColor
    CompositionLocalProvider(LocalContentColor provides contentColorFor(containerColor)) {
        Column(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            containerColor.copy(alpha = HoloDefaults.FROST_ALPHA),
                            colors.surface.copy(alpha = HoloDefaults.FROST_ALPHA)
                        )
                    )
                )
                .border(HoloDefaults.BorderWidth, colors.primary.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA), shape)
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
                stats.forEach { stat ->
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
                            maxLines = 1
                        )
                        Text(
                            text = stat.value.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
