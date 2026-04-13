package dev.mizarc.waystonewarps.interaction.menus.use

import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpSearchMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        anvilInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_SEARCH_TITLE),
            icon = Material.LODESTONE,
            onInput = { input -> menuNavigator.goBackWithData(input) },
            onCancel = { menuNavigator.goBack() }
        )
    }
}
