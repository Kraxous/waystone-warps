package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
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
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WarpNamingMenu(
    private val player: Player,
    private val menuNavigator: MenuNavigator,
    private val location: Location
) : Menu, KoinComponent {
    private val createWarp: CreateWarp by inject()
    private val localizationProvider: LocalizationProvider by inject()
    private val anvilInputService: AnvilInputService by inject()
    private var name = ""
    private var isConfirming = false

    override fun open() {
        val lodestoneItem = ItemStack(Material.LODESTONE)
            .name("", PrimaryColourPalette.INFO.color!!)
            .lore(localizationProvider.get(
                player.uniqueId,
                LocalizationKeys.MENU_WARP_NAMING_ITEM_WARP_LORE,
                location.blockX.toString(),
                location.blockY.toString(),
                location.blockZ.toString()
            ))
        val confirmItem = ItemStack(Material.NETHER_STAR)
            .name(localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_COMMON_ITEM_CONFIRM_NAME),
                PrimaryColourPalette.SUCCESS.color!!)

        anvilInputService.prompt(
            player = player,
            title = localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_NAMING_TITLE),
            inputItem = lodestoneItem,
            confirmItem = confirmItem,
            onInputChanged = { newName ->
                if (!isConfirming) {
                    name = newName
                } else {
                    isConfirming = false
                }
            },
            onSubmit = { create(lodestoneItem) },
            onCancel = { player.sendActionBar(Component.text("Waystone creation cancelled.")) },
            onErrorCleared = { isConfirming = true }
        )
    }

    private fun create(lodestoneItem: ItemStack): AnvilInputResult {
        val belowLocation = location.clone().subtract(0.0, 1.0, 0.0)
        return when (val result = createWarp.execute(
            player.uniqueId,
            name,
            location.toPosition3D(),
            location.world.uid,
            location.world.getBlockAt(belowLocation).type.name,
            PermissionHelper.canBypassLimit(player)
        )) {
            is CreateWarpResult.Success -> {
                location.world.playSound(player.location, Sound.BLOCK_VAULT_OPEN_SHUTTER, SoundCategory.BLOCKS, 1.0f, 1.0f)
                menuNavigator.openMenu(WarpManagementMenu(player, menuNavigator, result.warp))
                AnvilInputResult.Close
            }
            is CreateWarpResult.LimitExceeded -> error(LocalizationKeys.CONDITION_NAMING_LIMIT)
            is CreateWarpResult.NameAlreadyExists -> error(LocalizationKeys.CONDITION_NAMING_EXISTING)
            is CreateWarpResult.NameCannotBeBlank -> {
                lodestoneItem.name("")
                error(LocalizationKeys.CONDITION_NAMING_BLANK)
            }
        }
    }

    private fun error(messageKey: String): AnvilInputResult {
        isConfirming = true
        return AnvilInputResult.Error(
            ItemStack(Material.PAPER).name(
                localizationProvider.get(player.uniqueId, messageKey),
                PrimaryColourPalette.FAILED.color!!
            )
        )
    }
}
