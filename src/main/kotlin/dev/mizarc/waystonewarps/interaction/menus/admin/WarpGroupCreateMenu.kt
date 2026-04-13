package dev.mizarc.waystonewarps.interaction.menus.admin

import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroup
import dev.mizarc.waystonewarps.application.actions.groups.CreateWarpGroupResult
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

class WarpGroupCreateMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val localizationProvider: LocalizationProvider
) : Menu, KoinComponent {
    private val createWarpGroup: CreateWarpGroup by inject()
    private val anvilInputService: AnvilInputService by inject()

    override fun open() {
        val bookItem = ItemStack(Material.BOOKSHELF)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_MANAGEMENT_CREATE_NAME))
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
                PrimaryColourPalette.SUCCESS.color!!)

        anvilInputService.prompt(
            player = player,
            title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_GROUP_CREATE_TITLE),
            inputItem = bookItem,
            confirmItem = confirmItem,
            onSubmit = { name -> create(name) },
            onCancel = { menuNavigator.goBack() }
        )
    }

    private fun create(name: String): AnvilInputResult {
        return when (createWarpGroup.execute(player.uniqueId, name)) {
            CreateWarpGroupResult.SUCCESS -> {
                menuNavigator.goBack()
                AnvilInputResult.Close
            }
            CreateWarpGroupResult.NAME_BLANK -> AnvilInputResult.Close
            CreateWarpGroupResult.NAME_TAKEN -> error()
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
