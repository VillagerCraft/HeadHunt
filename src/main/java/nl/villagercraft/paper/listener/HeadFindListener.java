package nl.villagercraft.paper.listener;

import net.kyori.adventure.text.Component;
import nl.villagercraft.paper.hunt.RegisterFindOutcome;
import nl.villagercraft.paper.hunt.RegisterFindOutcome.ChatMessage;
import nl.villagercraft.paper.hunt.HuntService;
import nl.villagercraft.paper.message.MessagesService;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class HeadFindListener implements Listener {

  private final HuntService huntService;
  private final MessagesService messagesService;

  public HeadFindListener(HuntService huntService, MessagesService messagesService) {
    this.huntService = huntService;
    this.messagesService = messagesService;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }

    Action action = event.getAction();
    if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    Player player = event.getPlayer();
    huntService
        .registerFind(player, block, action)
        .ifPresent(outcome -> deliverOutcome(player, outcome));
  }

  private void deliverOutcome(Player player, RegisterFindOutcome outcome) {
    for (ChatMessage message : outcome.playerMessages()) {
      player.sendMessage(messagesService.resolve(message.messageKey(), message.placeholders()));
    }
    for (ChatMessage message : outcome.broadcasts()) {
      Component component =
          messagesService.resolve(message.messageKey(), message.placeholders());
      for (Player online : Bukkit.getOnlinePlayers()) {
        if (!online.equals(player)) {
          online.sendMessage(component);
        }
      }
    }
  }
}
