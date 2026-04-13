package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroupResult
import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpGroupCreateMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val createWarpGroup: CreateWarpGroup by inject()
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        anvilInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_CREATE_TITLE),
            icon = Material.BOOKSHELF,
            onInput = { name -> create(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun create(name: String) {
        when (createWarpGroup.execute(player.uniqueId, name)) {
            CreateWarpGroupResult.SUCCESS -> menuNavigator.goBack()
            CreateWarpGroupResult.NAME_BLANK -> menuNavigator.goBack()
            CreateWarpGroupResult.NAME_TAKEN -> {
                player.sendMessage(Component.text(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN)
                ).color(PrimaryColourPalette.FAILED.color))
                open()
            }
        }
    }
}
