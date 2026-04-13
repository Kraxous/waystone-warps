package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.RenameWarpGroupResult
import dev.mizarc.waystonewarps.domain.warps.WarpGroup
import dev.mizarc.waystonewarps.interaction.input.AnvilInputResult
import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.name
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
        val bookItem = ItemStack(Material.BOOKSHELF).name(group.name)
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
                PrimaryColourPalette.SUCCESS.color!!)

        anvilInputService.prompt(
            player = player,
            title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_TITLE),
            inputItem = bookItem,
            confirmItem = confirmItem,
            onSubmit = { name -> rename(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun rename(name: String): AnvilInputResult {
        return when (renameWarpGroup.execute(group.id, name)) {
            RenameWarpGroupResult.SUCCESS -> {
                menuNavigator.goBack()
                AnvilInputResult.Close
            }
            RenameWarpGroupResult.NAME_BLANK -> AnvilInputResult.Close
            RenameWarpGroupResult.NOT_FOUND -> AnvilInputResult.Close
            RenameWarpGroupResult.NAME_TAKEN -> error()
        }
    }

    private fun error(): AnvilInputResult {
        return AnvilInputResult.Error(
            ItemStack(Material.PAPER).name(
                localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_RENAME_NAME_TAKEN),
                PrimaryColourPalette.FAILED.color!!
            )
        )
    }
}
