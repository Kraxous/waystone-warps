package dev.mizarc.waystonewarps.interaction.menus.use

import dev.mizarc.waystonewarps.interaction.input.ChatInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpSearchMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val chatInputService: ChatInputService by inject()

    override fun open() {
        chatInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_SEARCH_TITLE),
            onInput = { input -> menuNavigator.goBackWithData(input) },
            onCancel = { menuNavigator.goBack() }
        )
    }
}
