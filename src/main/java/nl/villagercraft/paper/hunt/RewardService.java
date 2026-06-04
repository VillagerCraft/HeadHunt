package nl.villagercraft.paper.hunt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import nl.villagercraft.paper.hunt.RewardPlaceholderResolver.RewardContext;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.Head;
import nl.villagercraft.paper.model.HuntCompletion;
import nl.villagercraft.paper.model.HuntSet;
import nl.villagercraft.paper.model.RewardConfig;
import nl.villagercraft.paper.storage.FindRepository;
import nl.villagercraft.paper.storage.HuntCompletionRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class RewardService {

  private final JavaPlugin plugin;
  private final FindRepository findRepository;
  private final HuntCompletionRepository completionRepository;

  public RewardService(
      JavaPlugin plugin,
      FindRepository findRepository,
      HuntCompletionRepository completionRepository) {
    this.plugin = plugin;
    this.findRepository = findRepository;
    this.completionRepository = completionRepository;
  }

  /**
   * Runs head, set, and hunt reward commands after a find is persisted. Order: head → set → hunt.
   */
  public RegisterFindOutcome handleFindRewards(
      ActiveHunt hunt, Player player, Head head, Instant timestamp) {
    RegisterFindOutcome outcome = new RegisterFindOutcome();
    outcome.addPlayerMessage("head-found", Map.of("head", head.getName()));

    HuntSet set = hunt.getSets().get(head.getSetName());
    int setTotal = set == null ? 0 : set.getHeads().size();
    int totalFinds = findRepository.countFindsForPlayer(hunt.getName(), player.getUniqueId());
    int setFinds =
        set == null
            ? 0
            : findRepository.countFindsForPlayerInSet(
                hunt.getName(), player.getUniqueId(), set.getHeads().keySet());
    RewardContext context =
        RewardContext.forFind(hunt, player, head, totalFinds, setFinds, setTotal);

    runHeadRewards(hunt.getRewards(), head, context);

    if (HuntProgress.setJustCompleted(hunt, player.getUniqueId(), head.getSetName(), findRepository)) {
      runSetRewards(hunt.getRewards(), head.getSetName(), context);
      outcome.addPlayerMessage("set-complete", Map.of("set", head.getSetName()));
      if (hunt.getBroadcast().isSetComplete()) {
        outcome.addBroadcast("set-complete", Map.of("set", head.getSetName()));
      }
    }

    if (HuntProgress.huntJustCompleted(
            hunt, player.getUniqueId(), head.getSetName(), findRepository)
        && !completionRepository.hasCompletion(hunt.getName(), player.getUniqueId())) {
      runHuntRewards(hunt.getRewards(), context);
      completionRepository.recordCompletion(
          new HuntCompletion(hunt.getName(), player.getUniqueId(), timestamp));
      outcome.addPlayerMessage("hunt-complete", Map.of("hunt", hunt.getName()));
      if (hunt.getBroadcast().isHuntComplete()) {
        outcome.addBroadcast("hunt-complete", Map.of("hunt", hunt.getName()));
      }
    }

    return outcome;
  }

  private void runHeadRewards(RewardConfig rewards, Head head, RewardContext context) {
    List<String> commands = new ArrayList<>(rewards.getHeadDefault());
    commands.addAll(rewards.getByHead().getOrDefault(head.getName(), List.of()));
    runCommands(commands, context);
  }

  private void runSetRewards(RewardConfig rewards, String setName, RewardContext context) {
    List<String> commands = new ArrayList<>(rewards.getSetDefault());
    commands.addAll(rewards.getBySet().getOrDefault(setName, List.of()));
    runCommands(commands, context);
  }

  private void runHuntRewards(RewardConfig rewards, RewardContext context) {
    runCommands(rewards.getHuntCommands(), context);
  }

  private void runCommands(List<String> templates, RewardContext context) {
    for (String template : templates) {
      if (template == null || template.isBlank()) {
        continue;
      }
      String command = RewardPlaceholderResolver.resolve(template, context);
      try {
        boolean success =
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
        if (!success) {
          plugin.getLogger().warning("Reward command failed (returned false): " + command);
        }
      } catch (Exception ex) {
        plugin.getLogger().log(Level.WARNING, "Reward command threw: " + command, ex);
      }
    }
  }
}
