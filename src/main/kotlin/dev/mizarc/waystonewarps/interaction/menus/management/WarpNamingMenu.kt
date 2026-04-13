package dev.mizarc.waystonewarps.interaction.menus.management

import dev.mizarc.waystonewarps.application.actions.world.CreateWarp
import dev.mizarc.waystonewarps.application.results.CreateWarpResult
import dev.mizarc.waystonewarps.infrastructure.mappers.toPosition3D
import dev.mizarc.waystonewarps.interaction.input.AnvilInputService
import dev.mizarc.waystonewarps.interaction.localization.LocalizationKeys
import dev.mizarc.waystonewarps.interaction.localization.LocalizationProvider
import dev.mizarc.waystonewarps.interaction.menus.Menu
import dev.mizarc.waystonewarps.interaction.menus.MenuNavigator
import dev.mizarc.waystonewarps.interaction.messaging.PrimaryColourPalette
import dev.mizarc.waystonewarps.interaction.utils.PermissionHelper
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
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

    override fun open() {
        anvilInputService.prompt(
            player,
            localizationProvider.get(player.uniqueId, LocalizationKeys.MENU_WARP_NAMING_TITLE),
            icon = Material.LODESTONE,
            onInput = { name -> create(name) },
            onCancel = { player.sendActionBar(Component.text("Waystone creation cancelled.")) }
        )
    }

    private fun create(name: String) {
        val belowLocation = location.clone().subtract(0.0, 1.0, 0.0)
        when (val result = createWarp.execute(
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
            }
            is CreateWarpResult.LimitExceeded -> retry(LocalizationKeys.CONDITION_NAMING_LIMIT)
            is CreateWarpResult.NameAlreadyExists -> retry(LocalizationKeys.CONDITION_NAMING_EXISTING)
            is CreateWarpResult.NameCannotBeBlank -> retry(LocalizationKeys.CONDITION_NAMING_BLANK)
        }
    }

    private fun retry(messageKey: String) {
        player.sendMessage(Component.text(localizationProvider.get(player.uniqueId, messageKey)).color(PrimaryColourPalette.FAILED.color))
        open()
    }
}
