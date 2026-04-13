package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.input.ChatInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpRenamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()
    private val chatInputService: ChatInputService by inject()

    override fun open() {
        if (!PermissionHelper.canRename(player, warp.playerId)) {
            player.sendMessage("§c${localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)}")
            menuNavigator.goBack()
            return
        }

        chatInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_RENAMING_TITLE),
            onInput = { name -> rename(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun rename(name: String) {
        if (name == warp.name) {
            menuNavigator.goBack()
            return
        }

        when (updateWarpName.execute(warp.id, player.uniqueId, name, player.hasPermission("waystonewarps.bypass.rename"))) {
            UpdateWarpNameResult.SUCCESS -> menuNavigator.goBack()
            UpdateWarpNameResult.WARP_NOT_FOUND -> failAndReturn(LocalizationKeys.CONDITION_NAMING_NOT_FOUND)
            UpdateWarpNameResult.NAME_ALREADY_TAKEN -> retry(LocalizationKeys.CONDITION_NAMING_EXISTING)
            UpdateWarpNameResult.NAME_BLANK -> retry(LocalizationKeys.CONDITION_NAMING_BLANK)
            UpdateWarpNameResult.NOT_AUTHORIZED -> failAndReturn(LocalizationKeys.CONDITION_NAMING_NO_PERMISSION)
        }
    }

    private fun retry(messageKey: String) {
        player.sendMessage(Component.text(localizationProvider.get(player.uniqueId, messageKey)).color(PrimaryColourPalette.FAILED.color))
        open()
    }

    private fun failAndReturn(messageKey: String) {
        player.sendMessage(Component.text(localizationProvider.get(player.uniqueId, messageKey)).color(PrimaryColourPalette.FAILED.color))
        menuNavigator.goBack()
    }
}
