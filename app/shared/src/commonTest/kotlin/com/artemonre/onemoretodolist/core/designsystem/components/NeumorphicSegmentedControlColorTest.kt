package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class NeumorphicSegmentedControlColorTest {

    private fun Color.oklabLightness() = convert(ColorSpaces.Oklab).red

    @Test
    fun `withOklabLightnessDelta produces the same perceptual lightness swing for a near-light and a near-dark background`() {
        val nearLight = Color(0xFFDBDBDD) // Indigo light background
        val nearDark = Color(0xFF20212A) // Indigo dark background
        val delta = 0.08f

        val nearLightSwing = nearLight.withOklabLightnessDelta(delta).oklabLightness() -
            nearLight.oklabLightness()
        val nearDarkSwing = nearDark.withOklabLightnessDelta(delta).oklabLightness() -
            nearDark.oklabLightness()

        // Regression guard for the fixed bug: both swings should land within the same small
        // tolerance of the requested delta, regardless of how light or dark the starting color
        // already was. The old lerp-toward-white blend failed this by a wide margin for a dark
        // base.
        assertTrue(abs(nearLightSwing - delta) < 0.01f)
        assertTrue(abs(nearDarkSwing - delta) < 0.01f)
    }

    @Test
    fun `negative delta darkens a near-white background as much as it darkens a near-black one`() {
        val nearLight = Color(0xFFDBDBDD)
        val nearDark = Color(0xFF20212A)
        val delta = -0.08f

        val nearLightSwing = nearLight.withOklabLightnessDelta(delta).oklabLightness() -
            nearLight.oklabLightness()
        val nearDarkSwing = nearDark.withOklabLightnessDelta(delta).oklabLightness() -
            nearDark.oklabLightness()

        assertTrue(abs(nearLightSwing - delta) < 0.01f)
        assertTrue(abs(nearDarkSwing - delta) < 0.01f)
    }
}
