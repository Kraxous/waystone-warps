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
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.view.AnvilView
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class AnvilInputService(private val plugin: JavaPlugin) : Listener {
    private data class Prompt(
        val onInput: (String) -> Unit,
        val onCancel: () -> Unit
    )

    private val prompts = ConcurrentHashMap<UUID, Prompt>()

    fun prompt(
        player: Player,
        title: String,
        initialValue: String = "",
        icon: Material = Material.PAPER,
        onInput: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        prompts[player.uniqueId] = Prompt(onInput, onCancel)

        val inventory = Bukkit.createInventory(player, InventoryType.ANVIL, Component.text(title))
        inventory.setItem(0, namedItem(icon, initialValue))
        inventory.setItem(2, namedItem(Material.NETHER_STAR, "Confirm"))
        player.openInventory(inventory)
    }

    @EventHandler
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        val player = event.view.player as? Player ?: return
        if (!prompts.containsKey(player.uniqueId)) return

        event.result = namedItem(Material.NETHER_STAR, event.view.renameText ?: "Confirm")
        event.view.repairCost = 0
        event.view.maximumRepairCost = 0
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val prompt = prompts[player.uniqueId] ?: return
        if (event.view.type != InventoryType.ANVIL) return

        event.isCancelled = true
        if (event.rawSlot != 2) return

        val input = (event.view as? AnvilView)?.renameText?.trim().orEmpty()
        prompts.remove(player.uniqueId)
        player.closeInventory()
        Bukkit.getScheduler().runTask(plugin, Runnable {
            prompt.onInput(input)
        })
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        val prompt = prompts.remove(player.uniqueId) ?: return
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
