package nl.villagercraft.paper.listener;

import nl.villagercraft.paper.hunt.HeadRegistry;
import nl.villagercraft.paper.hunt.HuntService;
import nl.villagercraft.paper.model.ActiveHunt;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class HeadProtectionListener implements Listener {

  private final HuntService huntService;

  public HeadProtectionListener(HuntService huntService) {
    this.huntService = huntService;
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    if (huntService
        .getHuntForHeadLookup()
        .flatMap(hunt -> HeadRegistry.findByBlock(hunt, block))
        .isPresent()) {
      event.setCancelled(true);
    }
  }
}
