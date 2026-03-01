package com.rosan.installer.ui.page.main.widget.setting

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.rosan.installer.ui.theme.ConnectionRadius
import com.rosan.installer.ui.theme.CornerRadius

data class SplicedItemData(
    val key: Any?,
    val visible: Boolean,
    val content: @Composable () -> Unit
)

class SplicedGroupScope {
    val items = mutableListOf<SplicedItemData>()

    /**
     * Adds an item to the spliced group.
     * @param key A unique identifier for the item. Crucial for correct animation state during list changes.
     */
    fun item(key: Any? = null, visible: Boolean = true, content: @Composable () -> Unit) {
        items.add(SplicedItemData(key ?: items.size, visible, content))
    }
}

/**
 * A container that groups items with a spliced, continuous look (similar to M3 Expressive).
 *
 * Features:
 * - **Dynamic Shapes**: Smoothly morphs on Android 13+; uses static fallback on Android 12 and below to prevent RenderNode crashes.
 * - **Blinds Animation**: Items expand/collapse vertically without scaling.
 * - **Stacking Order**: Exiting items slide over remaining items to mask any shape transitions.
 */
@Composable
fun SplicedColumnGroup(
    modifier: Modifier = Modifier,
    title: String = "",
    content: SplicedGroupScope.() -> Unit
) {
    val scope = SplicedGroupScope().apply(content)
    val allItems = scope.items

    if (allItems.isEmpty()) return

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )
        }

        Column(verticalArrangement = Arrangement.Top) {
            val firstVisibleIndex = allItems.indexOfFirst { it.visible }
            val lastVisibleIndex = allItems.indexOfLast { it.visible }

            val sharedStiffness = Spring.StiffnessMediumLow

            allItems.forEachIndexed { index, itemData ->
                key(itemData.key) {
                    // Shutter Masking Z-Index:
                    // Exiting items MUST render on top to physically cover the gaps and morphing corners.
                    val zIndex = if (itemData.visible) 0f else 1f

                    AnimatedVisibility(
                        visible = itemData.visible,
                        modifier = Modifier.zIndex(zIndex),
                        enter = expandVertically(
                            animationSpec = spring(stiffness = sharedStiffness),
                            expandFrom = Alignment.Top
                        ) + fadeIn(
                            animationSpec = spring(stiffness = sharedStiffness)
                        ),
                        exit = shrinkVertically(
                            animationSpec = spring(stiffness = sharedStiffness),
                            shrinkTowards = Alignment.Top
                        ) + fadeOut(
                            animationSpec = spring(stiffness = sharedStiffness)
                        )
                    ) {
                        // Stable Edge Retention: Keep outer corners rounded even during exit.
                        val isFirst = index == firstVisibleIndex || (index == 0 && !itemData.visible)
                        val isLast = index == lastVisibleIndex || (index == allItems.lastIndex && !itemData.visible)

                        val targetTopRadius = if (isFirst) CornerRadius else ConnectionRadius
                        val targetBottomRadius = if (isLast) CornerRadius else ConnectionRadius

                        // Conditionally apply animateDpAsState only for Android 13 (TIRAMISU) and above.
                        val isAtLeastTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                        val currentTopRadius = if (isAtLeastTiramisu) {
                            animateDpAsState(
                                targetValue = targetTopRadius,
                                animationSpec = spring(stiffness = sharedStiffness),
                                label = "TopCornerRadius"
                            ).value
                        } else {
                            targetTopRadius
                        }

                        val currentBottomRadius = if (isAtLeastTiramisu) {
                            animateDpAsState(
                                targetValue = targetBottomRadius,
                                animationSpec = spring(stiffness = sharedStiffness),
                                label = "BottomCornerRadius"
                            ).value
                        } else {
                            targetBottomRadius
                        }

                        val shape = RoundedCornerShape(
                            topStart = currentTopRadius,
                            topEnd = currentTopRadius,
                            bottomStart = currentBottomRadius,
                            bottomEnd = currentBottomRadius
                        )

                        // Padding is safe to animate on all Android versions.
                        val targetTopPadding = if (isFirst) 0.dp else 2.dp
                        val currentTopPadding = if (isAtLeastTiramisu) {
                            animateDpAsState(
                                targetValue = targetTopPadding,
                                animationSpec = spring(stiffness = sharedStiffness),
                                label = "TopPadding"
                            ).value
                        } else {
                            // On older Androids, animating layout bounds with clip can also be risky,
                            // but usually padding is fine. If it still glitches, use static padding.
                            targetTopPadding
                        }

                        Column(
                            modifier = Modifier
                                .padding(top = currentTopPadding)
                                // Using graphicsLayer is more performant for shapes during animation
                                .graphicsLayer {
                                    this.shape = shape
                                    this.clip = true
                                }
                                .background(MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            itemData.content()
                        }
                    }
                }
            }
        }
    }
}