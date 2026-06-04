package nl.villagercraft.paper.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import nl.villagercraft.paper.HeadHuntPlugin;
import nl.villagercraft.paper.hunt.HeadBlockSupport;
import nl.villagercraft.paper.hunt.HuntService;
import nl.villagercraft.paper.message.MessagesService;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class HeadHuntCommands {

  private static final String PERM_HUNT_CREATE = "headhunt.admin.hunt.create";
  private static final String PERM_HUNT_ACTIVATE = "headhunt.admin.hunt.activate";
  private static final String PERM_HUNT_DELETE = "headhunt.admin.hunt.delete";
  private static final String PERM_HUNT_SCHEDULE = "headhunt.admin.hunt.schedule";
  private static final String PERM_SET_CREATE = "headhunt.admin.set.create";
  private static final String PERM_SET_LIST = "headhunt.admin.set.list";
  private static final String PERM_HEAD_ADD = "headhunt.admin.head.add";
  private static final String PERM_HEAD_REMOVE = "headhunt.admin.head.remove";
  private static final String PERM_HEAD_INSPECT = "headhunt.admin.head.inspect";
  private static final String PERM_PLAYER_RESET = "headhunt.admin.player.reset";
  private static final String PERM_PLAYER_BAN = "headhunt.admin.player.ban";
  private static final String PERM_PROGRESS_SELF = "headhunt.play.progress.self";
  private static final String PERM_PROGRESS_OTHER = "headhunt.play.progress.other";
  private static final String PERM_LEADERBOARD = "headhunt.play.leaderboard";
  private static final String PERM_POPULAR = "headhunt.play.stats.popular";

  private HeadHuntCommands() {}

  public static void register(
      HeadHuntPlugin plugin, MessagesService messagesService, HuntService huntService) {
    plugin
        .getLifecycleManager()
        .registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> {
              LiteralCommandNode<CommandSourceStack> root =
                  Commands.literal("headhunt")
                      .executes(context -> sendUsage(messagesService, context.getSource()))
                      .then(
                          Commands.literal("create")
                              .then(
                                  Commands.argument("name", StringArgumentType.word())
                                      .executes(
                                          context ->
                                              runCreate(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource(),
                                                  StringArgumentType.getString(context, "name")))))
                      .then(
                          Commands.literal("activate")
                              .executes(
                                  context ->
                                      runActivate(
                                          messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("pause")
                              .executes(
                                  context ->
                                      runPause(messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("resume")
                              .executes(
                                  context ->
                                      runResume(
                                          messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("delete")
                              .executes(
                                  context ->
                                      runDelete(
                                          messagesService, huntService, context.getSource(), false))
                              .then(
                                  Commands.literal("--delete-heads")
                                      .executes(
                                          context ->
                                              runDelete(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource(),
                                                  true))))
                      .then(
                          Commands.literal("reset")
                              .then(
                                  Commands.argument("player", StringArgumentType.word())
                                      .then(
                                          Commands.literal("hunt")
                                              .then(
                                                  Commands.argument(
                                                          "identifier", StringArgumentType.word())
                                                      .executes(
                                                          context ->
                                                              runReset(
                                                                  messagesService,
                                                                  huntService,
                                                                  context.getSource(),
                                                                  StringArgumentType.getString(
                                                                      context, "player"),
                                                                  "hunt",
                                                                  StringArgumentType.getString(
                                                                      context, "identifier")))))
                                      .then(
                                          Commands.literal("set")
                                              .then(
                                                  Commands.argument(
                                                          "identifier", StringArgumentType.word())
                                                      .executes(
                                                          context ->
                                                              runReset(
                                                                  messagesService,
                                                                  huntService,
                                                                  context.getSource(),
                                                                  StringArgumentType.getString(
                                                                      context, "player"),
                                                                  "set",
                                                                  StringArgumentType.getString(
                                                                      context, "identifier")))))
                                      .then(
                                          Commands.literal("head")
                                              .then(
                                                  Commands.argument(
                                                          "identifier", StringArgumentType.word())
                                                      .executes(
                                                          context ->
                                                              runReset(
                                                                  messagesService,
                                                                  huntService,
                                                                  context.getSource(),
                                                                  StringArgumentType.getString(
                                                                      context, "player"),
                                                                  "head",
                                                                  StringArgumentType.getString(
                                                                      context, "identifier")))))))
                      .then(
                          Commands.literal("ban")
                              .then(
                                  Commands.argument("player", StringArgumentType.word())
                                      .executes(
                                          context ->
                                              runBan(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource(),
                                                  StringArgumentType.getString(
                                                      context, "player")))))
                      .then(
                          Commands.literal("unban")
                              .then(
                                  Commands.argument("player", StringArgumentType.word())
                                      .executes(
                                          context ->
                                              runUnban(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource(),
                                                  StringArgumentType.getString(
                                                      context, "player")))))
                      .then(
                          Commands.literal("leaderboard")
                              .executes(
                                  context ->
                                      runLeaderboard(
                                          messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("progress")
                              .executes(
                                  context ->
                                      runProgress(
                                          messagesService, huntService, context.getSource(), null))
                              .then(
                                  Commands.argument("player", StringArgumentType.word())
                                      .executes(
                                          context ->
                                              runProgress(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource(),
                                                  StringArgumentType.getString(
                                                      context, "player")))))
                      .then(
                          Commands.literal("inspect")
                              .executes(
                                  context ->
                                      runInspect(
                                          messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("popular")
                              .executes(
                                  context ->
                                      runPopular(
                                          messagesService, huntService, context.getSource())))
                      .then(
                          Commands.literal("set")
                              .then(
                                  Commands.literal("create")
                                      .then(
                                          Commands.argument("name", StringArgumentType.word())
                                              .executes(
                                                  context ->
                                                      runSetCreate(
                                                          messagesService,
                                                          huntService,
                                                          context.getSource(),
                                                          StringArgumentType.getString(
                                                              context, "name")))))
                              .then(
                                  Commands.literal("list")
                                      .executes(
                                          context ->
                                              runSetList(
                                                  messagesService,
                                                  huntService,
                                                  context.getSource()))))
                      .then(
                          Commands.literal("add")
                              .then(
                                  Commands.argument("set", StringArgumentType.word())
                                      .then(
                                          Commands.argument("headName", StringArgumentType.word())
                                              .executes(
                                                  context ->
                                                      runAdd(
                                                          messagesService,
                                                          huntService,
                                                          context.getSource(),
                                                          StringArgumentType.getString(
                                                              context, "set"),
                                                          StringArgumentType.getString(
                                                              context, "headName"))))))
                      .then(
                          Commands.literal("remove")
                              .executes(
                                  context ->
                                      runRemove(
                                          messagesService, huntService, context.getSource())))
                      .build();

              event.registrar()
                  .register(root, "Head hunt administration and gameplay", List.of());
            });
  }

  private static void sendOutcome(
      CommandSender sender, MessagesService messagesService, HuntService.CommandOutcome outcome) {
    for (HuntService.CommandOutcome line : outcome.messagesToSend()) {
      sender.sendMessage(messagesService.resolve(line.messageKey(), line.placeholders()));
    }
  }

  private static int runCreate(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String name) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HUNT_CREATE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.create(name));
    return Command.SINGLE_SUCCESS;
  }

  private static int runActivate(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HUNT_ACTIVATE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.activate());
    return Command.SINGLE_SUCCESS;
  }

  private static int runPause(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HUNT_SCHEDULE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.pause());
    return Command.SINGLE_SUCCESS;
  }

  private static int runResume(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HUNT_SCHEDULE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.resume());
    return Command.SINGLE_SUCCESS;
  }

  private static int runDelete(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      boolean deleteHeads) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HUNT_DELETE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.delete(deleteHeads));
    return Command.SINGLE_SUCCESS;
  }

  private static int runReset(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String playerName,
      String scope,
      String identifier) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_PLAYER_RESET)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.reset(playerName, scope, identifier));
    return Command.SINGLE_SUCCESS;
  }

  private static int runBan(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String playerName) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_PLAYER_BAN)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.ban(playerName));
    return Command.SINGLE_SUCCESS;
  }

  private static int runUnban(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String playerName) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_PLAYER_BAN)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.unban(playerName));
    return Command.SINGLE_SUCCESS;
  }

  private static int runLeaderboard(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_LEADERBOARD)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.leaderboard());
    return Command.SINGLE_SUCCESS;
  }

  private static int runProgress(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String targetPlayerName) {
    CommandSender sender = source.getSender();
    if (targetPlayerName == null) {
      if (!sender.hasPermission(PERM_PROGRESS_SELF)) {
        sender.sendMessage(messagesService.resolve("no-permission"));
        return Command.SINGLE_SUCCESS;
      }
      if (!(sender instanceof Player player)) {
        sender.sendMessage(messagesService.resolve("player-only-command"));
        return Command.SINGLE_SUCCESS;
      }
      sendOutcome(
          sender,
          messagesService,
          huntService.progress(player.getName(), Optional.empty()));
    } else {
      if (!sender.hasPermission(PERM_PROGRESS_OTHER)) {
        sender.sendMessage(messagesService.resolve("no-permission"));
        return Command.SINGLE_SUCCESS;
      }
      sendOutcome(
          sender,
          messagesService,
          huntService.progress(sender.getName(), Optional.of(targetPlayerName)));
    }
    return Command.SINGLE_SUCCESS;
  }

  private static int runInspect(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HEAD_INSPECT)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }
    if (!(sender instanceof Player player)) {
      sender.sendMessage(messagesService.resolve("player-only-command"));
      return Command.SINGLE_SUCCESS;
    }

    Optional<Block> blockOptional = HeadBlockSupport.targetBlock(player);
    if (blockOptional.isEmpty()) {
      sender.sendMessage(messagesService.resolve("no-target-block"));
      return Command.SINGLE_SUCCESS;
    }

    Block block = blockOptional.get();
    sendOutcome(
        sender,
        messagesService,
        huntService.inspectHead(
            block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    return Command.SINGLE_SUCCESS;
  }

  private static int runPopular(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_POPULAR)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.popular());
    return Command.SINGLE_SUCCESS;
  }

  private static int runSetCreate(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String setName) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_SET_CREATE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.createSet(setName));
    return Command.SINGLE_SUCCESS;
  }

  private static int runSetList(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_SET_LIST)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(sender, messagesService, huntService.listSets());
    return Command.SINGLE_SUCCESS;
  }

  private static int runAdd(
      MessagesService messagesService,
      HuntService huntService,
      CommandSourceStack source,
      String setName,
      String headName) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HEAD_ADD)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }
    if (!(sender instanceof Player player)) {
      sender.sendMessage(messagesService.resolve("player-only-command"));
      return Command.SINGLE_SUCCESS;
    }

    Optional<Block> blockOptional = HeadBlockSupport.targetBlock(player);
    if (blockOptional.isEmpty()) {
      sender.sendMessage(messagesService.resolve("no-target-block"));
      return Command.SINGLE_SUCCESS;
    }

    Block block = blockOptional.get();
    if (!HeadBlockSupport.isPlayerSkull(block)) {
      sender.sendMessage(messagesService.resolve("head-not-player-skull"));
      return Command.SINGLE_SUCCESS;
    }

    Optional<UUID> profileUuid = HeadBlockSupport.profileUuid(block);
    if (profileUuid.isEmpty()) {
      sender.sendMessage(messagesService.resolve("head-no-profile"));
      return Command.SINGLE_SUCCESS;
    }

    sendOutcome(
        sender,
        messagesService,
        huntService.addHead(
            setName,
            headName,
            block.getWorld().getName(),
            block.getX(),
            block.getY(),
            block.getZ(),
            profileUuid.get().toString()));
    return Command.SINGLE_SUCCESS;
  }

  private static int runRemove(
      MessagesService messagesService, HuntService huntService, CommandSourceStack source) {
    CommandSender sender = source.getSender();
    if (!sender.hasPermission(PERM_HEAD_REMOVE)) {
      sender.sendMessage(messagesService.resolve("no-permission"));
      return Command.SINGLE_SUCCESS;
    }
    if (!(sender instanceof Player player)) {
      sender.sendMessage(messagesService.resolve("player-only-command"));
      return Command.SINGLE_SUCCESS;
    }

    Optional<Block> blockOptional = HeadBlockSupport.targetBlock(player);
    if (blockOptional.isEmpty()) {
      sender.sendMessage(messagesService.resolve("no-target-block"));
      return Command.SINGLE_SUCCESS;
    }

    Block block = blockOptional.get();
    sendOutcome(
        sender,
        messagesService,
        huntService.removeHead(
            block.getWorld().getName(), block.getX(), block.getY(), block.getZ()));
    return Command.SINGLE_SUCCESS;
  }

  private static int sendUsage(MessagesService messagesService, CommandSourceStack source) {
    Component message = messagesService.resolve("command-usage");
    CommandSender sender = source.getSender();
    sender.sendMessage(message);
    return Command.SINGLE_SUCCESS;
  }
}
