package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.management.UpdateWarpName
import dev.mizarc.waystonewarps.application.results.UpdateWarpNameResult
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.interaction.input.AnvilInputResult
import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import dev.mizarc.waystonewarps.interaction.utils.lore
import dev.mizarc.waystonewarps.interaction.utils.name
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpRenamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val warp: Warp,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val updateWarpName: UpdateWarpName by inject()
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        if (!PermissionHelper.canRename(player, warp.playerId)) {
            player.sendMessage("§c${localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_MANAGEMENT_COMMON_NO_PERMISSION)}")
            menuNavigator.goBack()
            return
        }

        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name(warp.name)
            .lore("${warp.position.x}, ${warp.position.y}, ${warp.position.z}")
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
                PrimaryColourPalette.SUCCESS.color!!)

        anvilInputService.prompt(
            player = player,
            title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_RENAMING_TITLE),
            inputItem = lodestoneItem,
            confirmItem = confirmItem,
            onSubmit = { name -> rename(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun rename(name: String): AnvilInputResult {
        if (name == warp.name) {
            menuNavigator.goBack()
            return AnvilInputResult.Close
        }

        return when (updateWarpName.execute(warp.id, player.uniqueId, name, player.hasPermission("waystonewarps.bypass.rename"))) {
            UpdateWarpNameResult.SUCCESS -> {
                menuNavigator.goBack()
                AnvilInputResult.Close
            }
            UpdateWarpNameResult.WARP_NOT_FOUND -> {
                player.sendMessage(Component.text(localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_NOT_FOUND))
                    .color(PrimaryColourPalette.FAILED.color))
                menuNavigator.goBack()
                AnvilInputResult.Close
            }
            UpdateWarpNameResult.NAME_ALREADY_TAKEN -> error(LocalizationKeys.CONDITION_NAMING_EXISTING, name)
            UpdateWarpNameResult.NAME_BLANK -> AnvilInputResult.Close
            UpdateWarpNameResult.NOT_AUTHORIZED -> {
                player.sendMessage(Component.text(localizationProvider.get(player.uniqueId, LocalizationKeys.CONDITION_NAMING_NO_PERMISSION))
                    .color(PrimaryColourPalette.FAILED.color))
                menuNavigator.goBack()
                AnvilInputResult.Close
            }
        }
    }

    private fun error(messageKey: String, vararg args: Any): AnvilInputResult {
        return AnvilInputResult.Error(
            ItemStack(Material.PAPER).name(
                localizationProvider.get(player.uniqueId, messageKey, *args),
                PrimaryColourPalette.FAILED.color!!
            )
        )
    }
}
