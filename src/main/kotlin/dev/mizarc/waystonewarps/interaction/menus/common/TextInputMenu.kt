package dev.mizarc.waystonewarps.interaction.menus.common

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TextInputMenu(
    private val player: Player,
    private val title: String,
    private val localizationProvider: LocalizationProvider,
    private val initialValue: String = "",
    private val onSubmit: (String) -> Unit,
    private val onCancel: () -> Unit
) : Menu {
    private var value = initialValue

    override fun open() {
        val gui = ChestGui(6, title)
        gui.setOnTopClick { it.isCancelled = true }

        fun render() {
            gui.panes.clear()
            val pane = StaticPane(0, 0, 9, 6)

            val display = ItemStack(Material.PAPER)
                .name(if (value.isBlank()) " " else value, PrimaryColourPalette.INFO.color!!)
                .lore("Click letters below to edit.")
            pane.addItem(GuiItem(display) { it.isCancelled = true }, 0, 0)

            val confirm = ItemStack(Material.LIME_CONCRETE)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
                    PrimaryColourPalette.SUCCESS.color!!)
            pane.addItem(GuiItem(confirm) {
                it.isCancelled = true
                onSubmit(value)
            }, 8, 0)

            val backspace = ItemStack(Material.YELLOW_CONCRETE).name("Backspace")
            pane.addItem(GuiItem(backspace) {
                it.isCancelled = true
                if (value.isNotEmpty()) value = value.dropLast(1)
                render()
            }, 6, 0)

            val cancel = ItemStack(Material.RED_CONCRETE)
                .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_BACK_NAME),
                    PrimaryColourPalette.CANCELLED.color!!)
            pane.addItem(GuiItem(cancel) {
                it.isCancelled = true
                onCancel()
            }, 7, 0)

            val rows = listOf(
                "ABCDEFGHI",
                "JKLMNOPQR",
                "STUVWXYZ",
                "012345678",
                "9 -_"
            )

            rows.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { column, character ->
                    val material = if (character == ' ') Material.WHITE_CONCRETE else Material.LIGHT_BLUE_CONCRETE
                    val label = if (character == ' ') "Space" else character.toString()
                    val item = ItemStack(material).name(label)
                    pane.addItem(GuiItem(item) {
                        it.isCancelled = true
                        value += character
                        render()
                    }, column, rowIndex + 1)
                }
            }

            val clear = ItemStack(Material.GRAY_CONCRETE).name("Clear")
            pane.addItem(GuiItem(clear) {
                it.isCancelled = true
                value = ""
                render()
            }, 8, 5)

            gui.addPane(pane)
            gui.update()
        }

        render()
        gui.show(player)
    }
}
