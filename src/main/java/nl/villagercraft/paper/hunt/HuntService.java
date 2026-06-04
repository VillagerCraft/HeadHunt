package nl.villagercraft.paper.hunt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import nl.villagercraft.paper.config.HuntTemplateLoader;
import nl.villagercraft.paper.config.InvalidHuntConfigException;
import nl.villagercraft.paper.model.ActiveHunt;
import nl.villagercraft.paper.model.ClickMode;
import nl.villagercraft.paper.model.Find;
import nl.villagercraft.paper.model.Head;
import nl.villagercraft.paper.model.HuntCompletion;
import nl.villagercraft.paper.model.HuntSet;
import nl.villagercraft.paper.storage.ActiveHuntRepository;
import nl.villagercraft.paper.storage.FindRepository;
import nl.villagercraft.paper.storage.HuntArchiveService;
import nl.villagercraft.paper.storage.HuntCompletionRepository;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuntService {

  private static final String PERM_PLAY_FIND = "headhunt.play.find";

  private final JavaPlugin plugin;
  private final ActiveHuntRepository activeHuntRepository;
  private final FindRepository findRepository;
  private final HuntCompletionRepository completionRepository;
  private final RewardService rewardService;
  private final HuntArchiveService archiveService;

  public HuntService(
      JavaPlugin plugin,
      ActiveHuntRepository activeHuntRepository,
      FindRepository findRepository,
      HuntCompletionRepository completionRepository,
      RewardService rewardService,
      HuntArchiveService archiveService) {
    this.plugin = plugin;
    this.activeHuntRepository = activeHuntRepository;
    this.findRepository = findRepository;
    this.completionRepository = completionRepository;
    this.rewardService = rewardService;
    this.archiveService = archiveService;
  }

  public static boolean isBanned(ActiveHunt hunt, UUID playerId) {
    return hunt.getBanlist().contains(playerId);
  }

  public Optional<ActiveHunt> getActiveHunt() {
    return activeHuntRepository.getActiveHunt();
  }

  /** Current hunt for head lookup and break protection (active or inactive). */
  public Optional<ActiveHunt> getHuntForHeadLookup() {
    return activeHuntRepository.getActiveHunt();
  }

  public boolean isFindRegistrationAllowed() {
    Optional<ActiveHunt> hunt = activeHuntRepository.getActiveHunt();
    if (hunt.isEmpty()) {
      return false;
    }
    ActiveHunt current = hunt.get();
    if (!current.isActivated()) {
      return false;
    }
    if (current.isPaused()) {
      return false;
    }
    Instant now = Instant.now();
    Instant scheduleStart = current.getScheduleStart();
    if (scheduleStart != null && now.isBefore(scheduleStart)) {
      return false;
    }
    Instant scheduleEnd = current.getScheduleEnd();
    if (scheduleEnd != null && now.isAfter(scheduleEnd)) {
      return false;
    }
    return true;
  }

  public Optional<RegisterFindOutcome> registerFind(Player player, Block block, Action action) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return Optional.empty();
    }

    ActiveHunt hunt = huntOptional.get();
    Optional<Head> headOptional = HeadRegistry.findByBlock(hunt, block);
    if (headOptional.isEmpty()) {
      return Optional.empty();
    }
    if (!matchesClickMode(hunt.getClickMode(), action)) {
      return Optional.empty();
    }
    if (!player.hasPermission(PERM_PLAY_FIND)) {
      return Optional.empty();
    }

    Head head = headOptional.get();
    if (isBanned(hunt, player.getUniqueId())) {
      return Optional.empty();
    }
    if (!isFindRegistrationAllowed()) {
      RegisterFindOutcome outcome = new RegisterFindOutcome();
      outcome.addPlayerMessage("hunt-inactive");
      return Optional.of(outcome);
    }
    if (findRepository.hasFind(hunt.getName(), player.getUniqueId(), head.getName())) {
      RegisterFindOutcome outcome = new RegisterFindOutcome();
      outcome.addPlayerMessage("already-found");
      return Optional.of(outcome);
    }

    Instant timestamp = Instant.now();
    findRepository.addFind(
        new Find(hunt.getName(), player.getUniqueId(), head.getName(), timestamp));
    return Optional.of(rewardService.handleFindRewards(hunt, player, head, timestamp));
  }

  public CommandOutcome create(String requestedName) {
    if (activeHuntRepository.getActiveHunt().isPresent()) {
      return CommandOutcome.message("hunt-already-exists");
    }

    plugin.reloadConfig();
    FileConfiguration config = plugin.getConfig();
    String configName = config.getString("hunt.name", "").trim();
    if (!requestedName.equals(configName)) {
      return CommandOutcome.message(
          "hunt-name-mismatch", Map.of("name", requestedName, "expected", configName));
    }

    ActiveHunt snapshot;
    try {
      snapshot = HuntTemplateLoader.loadSnapshot(config);
    } catch (InvalidHuntConfigException ex) {
      plugin.getLogger().log(Level.WARNING, "Invalid hunt config: " + ex.getReason(), ex);
      return CommandOutcome.message("hunt-config-invalid", Map.of("reason", ex.getReason()));
    }

    snapshot.setActivated(false);
    snapshot.setScheduleStart(null);
    snapshot.setScheduleEnd(null);

    activeHuntRepository.setActiveHunt(snapshot);
    return CommandOutcome.message("hunt-created", Map.of("hunt", snapshot.getName()));
  }

  public CommandOutcome activate() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (hunt.isActivated()) {
      return CommandOutcome.message("hunt-already-activated");
    }

    hunt.setActivated(true);
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("hunt-activated", Map.of("hunt", hunt.getName()));
  }

  public CommandOutcome pause() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (hunt.isPaused()) {
      return CommandOutcome.message("hunt-already-paused");
    }

    hunt.setPaused(true);
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("hunt-paused", Map.of("hunt", hunt.getName()));
  }

  public CommandOutcome resume() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (!hunt.isPaused()) {
      return CommandOutcome.message("hunt-not-paused");
    }

    hunt.setPaused(false);
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("hunt-resumed", Map.of("hunt", hunt.getName()));
  }

  public CommandOutcome createSet(String setName) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (hunt.getSets().containsKey(setName)) {
      return CommandOutcome.message("set-already-exists", Map.of("set", setName));
    }

    hunt.getSets().put(setName, new HuntSet(setName));
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("set-created", Map.of("set", setName));
  }

  public CommandOutcome listSets() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    Map<String, HuntSet> sets = huntOptional.get().getSets();
    if (sets.isEmpty()) {
      return CommandOutcome.message("set-list-empty");
    }

    String setNames = String.join(", ", sets.keySet());
    return CommandOutcome.message("set-list", Map.of("sets", setNames));
  }

  public CommandOutcome addHead(
      String setName,
      String headName,
      String world,
      int x,
      int y,
      int z,
      String identity) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (HeadRegistry.findSet(hunt, setName).isEmpty()) {
      return CommandOutcome.message("set-not-found", Map.of("set", setName));
    }
    if (HeadRegistry.isHeadNameTaken(hunt, headName)) {
      return CommandOutcome.message("head-name-taken", Map.of("head", headName));
    }
    if (HeadRegistry.findByLocation(hunt, world, x, y, z).isPresent()) {
      return CommandOutcome.message("head-location-taken");
    }

    Head head = new Head(headName, setName, world, x, y, z, identity);
    hunt.getSets().get(setName).getHeads().put(headName, head);
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message(
        "head-added", Map.of("head", headName, "set", setName));
  }

  public CommandOutcome removeHead(String world, int x, int y, int z) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("head-not-registered");
    }

    ActiveHunt hunt = huntOptional.get();
    Optional<Head> headOptional = HeadRegistry.findByLocation(hunt, world, x, y, z);
    if (headOptional.isEmpty()) {
      return CommandOutcome.message("head-not-registered");
    }

    Head head = headOptional.get();
    HeadRegistry.removeHeadAtLocation(hunt, world, x, y, z);
    findRepository.removeFindsForHead(head.getName());
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("head-removed", Map.of("head", head.getName()));
  }

  public CommandOutcome delete(boolean deleteHeads) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    String huntName = hunt.getName();
    List<Find> finds = findRepository.getFindsForHunt(huntName);
    List<HuntCompletion> completions = completionRepository.getCompletionsForHunt(huntName);
    archiveService.archive(huntName, finds, completions);

    if (deleteHeads) {
      removeRegisteredHeadBlocks(hunt);
    }

    activeHuntRepository.clearActiveHunt();
    findRepository.removeFindsForHunt(huntName);
    completionRepository.removeCompletionsForHunt(huntName);
    return CommandOutcome.message("hunt-deleted", Map.of("hunt", huntName));
  }

  private void removeRegisteredHeadBlocks(ActiveHunt hunt) {
    for (Head head : HuntHeads.allHeads(hunt)) {
      World world = Bukkit.getWorld(head.getWorld());
      if (world == null) {
        plugin
            .getLogger()
            .warning(
                "Delete heads: world not loaded for head "
                    + head.getName()
                    + " at "
                    + head.getWorld()
                    + " "
                    + head.getX()
                    + ","
                    + head.getY()
                    + ","
                    + head.getZ());
        continue;
      }
      Block block = world.getBlockAt(head.getX(), head.getY(), head.getZ());
      if (HeadBlockSupport.isPlayerSkull(block)) {
        block.setType(Material.AIR, false);
      } else {
        plugin
            .getLogger()
            .warning(
                "Delete heads: block missing or not a player skull for head "
                    + head.getName()
                    + " at "
                    + head.getWorld()
                    + " "
                    + head.getX()
                    + ","
                    + head.getY()
                    + ","
                    + head.getZ());
      }
    }
  }

  public CommandOutcome reset(String playerName, String scope, String identifier) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    Optional<Player> targetOptional = PlayerLookup.onlinePlayer(playerName);
    if (targetOptional.isEmpty()) {
      return CommandOutcome.message("player-not-found", Map.of("player", playerName));
    }

    ActiveHunt hunt = huntOptional.get();
    UUID playerId = targetOptional.get().getUniqueId();
    String huntName = hunt.getName();

    return switch (scope.toLowerCase()) {
      case "hunt" -> resetHunt(hunt, huntName, playerId, playerName, identifier);
      case "set" -> resetSet(hunt, huntName, playerId, playerName, identifier);
      case "head" -> resetHead(hunt, huntName, playerId, playerName, identifier);
      default -> CommandOutcome.message("reset-invalid-scope");
    };
  }

  private CommandOutcome resetHunt(
      ActiveHunt hunt, String huntName, UUID playerId, String playerName, String identifier) {
    if (!hunt.getName().equals(identifier)) {
      return CommandOutcome.message(
          "reset-hunt-name-mismatch",
          Map.of("identifier", identifier, "hunt", hunt.getName()));
    }
    findRepository.removeFindsForPlayer(huntName, playerId);
    completionRepository.removeCompletion(huntName, playerId);
    return CommandOutcome.message(
        "progress-reset",
        Map.of("player", playerName, "scope", "hunt", "identifier", identifier));
  }

  private CommandOutcome resetSet(
      ActiveHunt hunt, String huntName, UUID playerId, String playerName, String setName) {
    if (!hunt.getSets().containsKey(setName)) {
      return CommandOutcome.message("set-not-found", Map.of("set", setName));
    }
    findRepository.removeFindsForPlayerInSet(
        huntName, playerId, HuntHeads.headNamesInSet(hunt, setName));
    completionRepository.removeCompletion(huntName, playerId);
    return CommandOutcome.message(
        "progress-reset",
        Map.of("player", playerName, "scope", "set", "identifier", setName));
  }

  private CommandOutcome resetHead(
      ActiveHunt hunt, String huntName, UUID playerId, String playerName, String headName) {
    if (!HeadRegistry.isHeadNameTaken(hunt, headName)) {
      return CommandOutcome.message("head-not-found", Map.of("head", headName));
    }
    findRepository.removeFindForPlayer(huntName, playerId, headName);
    completionRepository.removeCompletion(huntName, playerId);
    return CommandOutcome.message(
        "progress-reset",
        Map.of("player", playerName, "scope", "head", "identifier", headName));
  }

  public CommandOutcome ban(String playerName) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    Optional<Player> targetOptional = PlayerLookup.onlinePlayer(playerName);
    if (targetOptional.isEmpty()) {
      return CommandOutcome.message("player-not-found", Map.of("player", playerName));
    }

    ActiveHunt hunt = huntOptional.get();
    UUID playerId = targetOptional.get().getUniqueId();
    if (hunt.getBanlist().contains(playerId)) {
      return CommandOutcome.message("player-already-banned", Map.of("player", playerName));
    }

    hunt.getBanlist().add(playerId);
    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("player-banned", Map.of("player", playerName));
  }

  public CommandOutcome unban(String playerName) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    Optional<Player> targetOptional = PlayerLookup.onlinePlayer(playerName);
    if (targetOptional.isEmpty()) {
      return CommandOutcome.message("player-not-found", Map.of("player", playerName));
    }

    ActiveHunt hunt = huntOptional.get();
    UUID playerId = targetOptional.get().getUniqueId();
    if (!hunt.getBanlist().remove(playerId)) {
      return CommandOutcome.message("player-not-banned", Map.of("player", playerName));
    }

    activeHuntRepository.setActiveHunt(hunt);
    return CommandOutcome.message("player-unbanned", Map.of("player", playerName));
  }

  public CommandOutcome leaderboard() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    if (!hunt.isActivated()) {
      return CommandOutcome.message("leaderboard-not-activated");
    }

    String huntName = hunt.getName();
    Map<UUID, Integer> findCounts = new HashMap<>();
    for (Find find : findRepository.getFindsForHunt(huntName)) {
      if (find.getPlayer() == null || isBanned(hunt, find.getPlayer())) {
        continue;
      }
      findCounts.merge(find.getPlayer(), 1, Integer::sum);
    }

    if (findCounts.isEmpty()) {
      return CommandOutcome.message("leaderboard-empty");
    }

    List<LeaderboardRow> rows = new ArrayList<>();
    for (Map.Entry<UUID, Integer> entry : findCounts.entrySet()) {
      UUID playerId = entry.getKey();
      boolean finisher = completionRepository.hasCompletion(huntName, playerId);
      Instant completionTime =
          completionRepository
              .getCompletion(huntName, playerId)
              .map(HuntCompletion::getTimestamp)
              .orElse(Instant.MAX);
      String name = Bukkit.getOfflinePlayer(playerId).getName();
      if (name == null) {
        name = playerId.toString();
      }
      rows.add(new LeaderboardRow(name, entry.getValue(), finisher, completionTime));
    }

    rows.sort(
        Comparator.comparingInt(LeaderboardRow::findCount)
            .reversed()
            .thenComparing(row -> !row.finisher())
            .thenComparing(LeaderboardRow::completionTime));

    List<CommandOutcome> lines = new ArrayList<>();
    lines.add(CommandOutcome.message("leaderboard-header", Map.of("hunt", huntName)));
    for (int rank = 0; rank < rows.size(); rank++) {
      LeaderboardRow row = rows.get(rank);
      lines.add(
          CommandOutcome.message(
              "leaderboard-entry",
              Map.of(
                  "rank",
                  String.valueOf(rank + 1),
                  "player",
                  row.playerName(),
                  "finds",
                  String.valueOf(row.findCount()),
                  "finished",
                  row.finisher() ? "yes" : "no")));
    }
    return CommandOutcome.batch(lines);
  }

  public CommandOutcome progress(String viewerName, Optional<String> targetPlayerName) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    String huntName = hunt.getName();
    String subjectName = targetPlayerName.orElse(viewerName);

    Optional<Player> subjectOptional = PlayerLookup.onlinePlayer(subjectName);
    if (subjectOptional.isEmpty()) {
      return CommandOutcome.message("player-not-found", Map.of("player", subjectName));
    }

    UUID playerId = subjectOptional.get().getUniqueId();
    int total = HuntHeads.totalHeadCount(hunt);
    int found = findRepository.countFindsForPlayer(huntName, playerId);

    Set<String> foundHeads = new HashSet<>();
    for (Find find : findRepository.getFindsForHunt(huntName)) {
      if (playerId.equals(find.getPlayer())) {
        foundHeads.add(find.getHead());
      }
    }

    String headList =
        foundHeads.isEmpty()
            ? "none"
            : String.join(", ", foundHeads.stream().sorted().toList());

    return CommandOutcome.message(
        "progress",
        Map.of(
            "player",
            subjectName,
            "found",
            String.valueOf(found),
            "total",
            String.valueOf(total),
            "heads",
            headList));
  }

  public CommandOutcome inspectHead(String world, int x, int y, int z) {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    Optional<Head> headOptional = HeadRegistry.findByLocation(hunt, world, x, y, z);
    if (headOptional.isEmpty()) {
      return CommandOutcome.message("head-not-registered");
    }

    Head head = headOptional.get();
    List<Find> finds = findRepository.getFindsForHead(head.getName()).stream()
        .filter(find -> hunt.getName().equals(find.getHunt()))
        .toList();

    if (finds.isEmpty()) {
      return CommandOutcome.message("inspect-none", Map.of("head", head.getName()));
    }

    List<CommandOutcome> lines = new ArrayList<>();
    lines.add(CommandOutcome.message("inspect-header", Map.of("head", head.getName())));
    for (Find find : finds) {
      String name =
          find.getPlayer() == null
              ? "unknown"
              : Optional.ofNullable(Bukkit.getOfflinePlayer(find.getPlayer()).getName())
                  .orElse(find.getPlayer().toString());
      lines.add(
          CommandOutcome.message(
              "inspect-entry",
              Map.of(
                  "player",
                  name,
                  "time",
                  find.getTimestamp() == null ? "?" : find.getTimestamp().toString())));
    }
    return CommandOutcome.batch(lines);
  }

  public CommandOutcome popular() {
    Optional<ActiveHunt> huntOptional = activeHuntRepository.getActiveHunt();
    if (huntOptional.isEmpty()) {
      return CommandOutcome.message("no-hunt");
    }

    ActiveHunt hunt = huntOptional.get();
    String huntName = hunt.getName();
    List<Head> heads = HuntHeads.allHeads(hunt);
    if (heads.isEmpty()) {
      return CommandOutcome.message("popular-empty");
    }

    List<PopularRow> rows = new ArrayList<>();
    for (Head head : heads) {
      int count = findRepository.countDistinctPlayersForHead(huntName, head.getName());
      rows.add(new PopularRow(head.getName(), count));
    }

    rows.sort(
        Comparator.comparingInt(PopularRow::count)
            .reversed()
            .thenComparing(PopularRow::headName));

    List<CommandOutcome> lines = new ArrayList<>();
    lines.add(CommandOutcome.message("popular-header", Map.of("hunt", huntName)));
    for (int rank = 0; rank < rows.size(); rank++) {
      PopularRow row = rows.get(rank);
      lines.add(
          CommandOutcome.message(
              "popular-entry",
              Map.of(
                  "rank",
                  String.valueOf(rank + 1),
                  "head",
                  row.headName(),
                  "count",
                  String.valueOf(row.count()))));
    }
    return CommandOutcome.batch(lines);
  }

  private record LeaderboardRow(
      String playerName, int findCount, boolean finisher, Instant completionTime) {}

  private record PopularRow(String headName, int count) {}

  private static boolean matchesClickMode(ClickMode clickMode, Action action) {
    return switch (clickMode) {
      case LEFT -> action == Action.LEFT_CLICK_BLOCK;
      case RIGHT -> action == Action.RIGHT_CLICK_BLOCK;
      case BOTH ->
          action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK;
    };
  }

  public record CommandOutcome(
      String messageKey, Map<String, String> placeholders, List<CommandOutcome> batch) {

    public CommandOutcome {
      batch = batch == null ? List.of() : List.copyOf(batch);
    }

    public static CommandOutcome message(String messageKey) {
      return new CommandOutcome(messageKey, Map.of(), List.of());
    }

    public static CommandOutcome message(String messageKey, Map<String, String> placeholders) {
      return new CommandOutcome(messageKey, placeholders, List.of());
    }

    public static CommandOutcome batch(List<CommandOutcome> lines) {
      if (lines.isEmpty()) {
        return message("leaderboard-empty");
      }
      return new CommandOutcome(lines.getFirst().messageKey(), lines.getFirst().placeholders(), lines);
    }

    public boolean isBatch() {
      return !batch.isEmpty();
    }

    public List<CommandOutcome> messagesToSend() {
      return isBatch() ? batch : List.of(this);
    }
  }
}
