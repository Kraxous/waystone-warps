package dev.mizarc.waystonewarps.interaction.input

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ChatInputService(private val plugin: JavaPlugin) : Listener {
    private data class Prompt(
        val onInput: (String) -> Unit,
        val onCancel: () -> Unit
    )

    private val prompts = ConcurrentHashMap<UUID, Prompt>()

    fun prompt(player: Player, message: String, onInput: (String) -> Unit, onCancel: () -> Unit = {}) {
        prompts[player.uniqueId] = Prompt(onInput, onCancel)
        player.closeInventory()
        player.sendMessage(Component.text(message))
        player.sendMessage(Component.text("Type cancel to cancel."))
    }

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val prompt = prompts.remove(event.player.uniqueId) ?: return
        event.isCancelled = true
        val input = PlainTextComponentSerializer.plainText().serialize(event.message()).trim()

        Bukkit.getScheduler().runTask(plugin, Runnable {
            if (input.equals("cancel", ignoreCase = true)) {
                prompt.onCancel()
            } else {
                prompt.onInput(input)
            }
        })
    }
}
