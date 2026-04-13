package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroupResult
import dev.mizarc.waystonewarps.domain.warps.WarpGroup
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

class WarpGroupRenameMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val group: WarpGroup,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val renameWarpGroup: RenameWarpGroup by inject()
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        anvilInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_TITLE),
            initialValue = group.name,
            icon = Material.BOOKSHELF,
            onInput = { name -> rename(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun rename(name: String) {
        when (renameWarpGroup.execute(group.id, name)) {
            RenameWarpGroupResult.SUCCESS -> menuNavigator.goBack()
            RenameWarpGroupResult.NAME_BLANK -> menuNavigator.goBack()
            RenameWarpGroupResult.NOT_FOUND -> menuNavigator.goBack()
            RenameWarpGroupResult.NAME_TAKEN -> {
                player.sendMessage(Component.text(
                    localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN)
                ).color(PrimaryColourPalette.FAILED.color))
                open()
            }
        }
    }
}
