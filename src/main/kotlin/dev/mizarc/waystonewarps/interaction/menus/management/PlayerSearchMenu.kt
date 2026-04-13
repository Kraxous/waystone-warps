package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PlayerSearchMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator
) : Menu, KoinComponent {
    private val localizationProvider: LocalizationProvider by inject()
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        anvilInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_PLAYER_SEARCH_TITLE),
            icon = Material.PLAYER_HEAD,
            onInput = { input -> menuNavigator.goBackWithData(input) },
            onCancel = { menuNavigator.goBack() }
        )
    }
}
