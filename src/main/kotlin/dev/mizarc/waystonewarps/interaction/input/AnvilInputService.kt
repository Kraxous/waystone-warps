package dev.mizarc.waystonewarps.interaction.input

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

sealed class AnvilInputResult {
    data object Close : AnvilInputResult()
    data object KeepOpen : AnvilInputResult()
    data class Error(val item: ItemStack) : AnvilInputResult()
}

class AnvilInputService(private val plugin: JavaPlugin) : Listener {
    private data class Prompt(
        val inventory: Inventory,
        val confirmItem: ItemStack,
        val onInputChanged: (String) -> Unit,
        val onSubmit: (String) -> AnvilInputResult,
        val onCancel: () -> Unit,
        val onErrorCleared: () -> Unit,
        var handlingSubmit: Boolean = false,
        var submitted: Boolean = false
    )

    private val prompts = ConcurrentHashMap<UUID, Prompt>()

    fun prompt(
        player: Player,
        title: String,
        inputItem: ItemStack,
        confirmItem: ItemStack,
        errorItem: ItemStack? = null,
        onInputChanged: (String) -> Unit = {},
        onSubmit: (String) -> AnvilInputResult,
        onCancel: () -> Unit = {},
        onErrorCleared: () -> Unit = {}
    ) {
        val inventory = Bukkit.createInventory(player, InventoryType.ANVIL, Component.text(title))
        inventory.setItem(0, inputItem)
        inventory.setItem(1, errorItem)
        inventory.setItem(2, confirmItem)

        prompts[player.uniqueId] = Prompt(inventory, confirmItem, onInputChanged, onSubmit, onCancel, onErrorCleared)
        player.openInventory(inventory)
    }

    fun prompt(
        player: Player,
        title: String,
        initialValue: String = "",
        icon: Material = Material.PAPER,
        onInput: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        prompt(
            player = player,
            title = title,
            inputItem = namedItem(icon, initialValue),
            confirmItem = namedItem(Material.NETHER_STAR, "Confirm"),
            onSubmit = { input ->
                onInput(input)
                AnvilInputResult.Close
            },
            onCancel = onCancel
        )
    }

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val player = event.view.player as? Player ?: return
        val prompt = prompts[player.uniqueId] ?: return
        if (event.inventory != prompt.inventory) return
        val renameText = event.view.renameText.orEmpty()

        prompt.onInputChanged(renameText)
        event.result = prompt.confirmItem.clone()
        event.view.repairCost = 0
        event.view.maximumRepairCost = 0
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val prompt = prompts[player.uniqueId] ?: return
        if (event.view.type != InventoryType.ANVIL) return
        if (event.inventory != prompt.inventory) return

        event.isCancelled = true
        if (event.rawSlot == 1) {
            event.inventory.setItem(1, null)
            prompt.onErrorCleared()
            return
        }
        if (event.rawSlot != 2) return

        val input = (event.view as? AnvilView)?.renameText.orEmpty()
        prompt.handlingSubmit = true
        val result = prompt.onSubmit(input)
        prompt.handlingSubmit = false

        when (result) {
            AnvilInputResult.Close -> {
                prompt.submitted = true
                prompts.remove(player.uniqueId)
                if (player.openInventory.topInventory == prompt.inventory) {
                    player.closeInventory()
                }
            }
            AnvilInputResult.KeepOpen -> Unit
            is AnvilInputResult.Error -> {
                event.inventory.setItem(1, result.item)
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val prompt = prompts[player.uniqueId] ?: return
        if (event.inventory != prompt.inventory) return
        if (prompt.submitted || prompt.handlingSubmit) return

        prompts.remove(player.uniqueId)

        Bukkit.getScheduler().runTask(plugin, Runnable {
            prompt.onCancel()
        })
    }

    private fun namedItem(material: Material, name: String): ItemStack {
        val item = ItemStack(material)
        item.editMeta { meta -> meta.displayName(Component.text(name.ifBlank { " " })) }
        return item
    }
}
