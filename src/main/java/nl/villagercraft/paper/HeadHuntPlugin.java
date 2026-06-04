package nl.villagercraft.paper;

import nl.villagercraft.paper.command.HeadHuntCommands;
import nl.villagercraft.paper.config.PluginFiles;
import nl.villagercraft.paper.hunt.HuntService;
import nl.villagercraft.paper.hunt.RewardService;
import nl.villagercraft.paper.listener.HeadFindListener;
import nl.villagercraft.paper.listener.HeadProtectionListener;
import nl.villagercraft.paper.message.MessagesService;
import nl.villagercraft.paper.storage.ActiveHuntRepository;
import nl.villagercraft.paper.storage.FindRepository;
import nl.villagercraft.paper.storage.HuntCompletionRepository;
import nl.villagercraft.paper.storage.HuntArchiveService;
import nl.villagercraft.paper.storage.yaml.YamlActiveHuntRepository;
import nl.villagercraft.paper.storage.yaml.YamlFindRepository;
import nl.villagercraft.paper.storage.yaml.YamlHuntCompletionRepository;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadHuntPlugin extends JavaPlugin {

  private MessagesService messagesService;
  private ActiveHuntRepository activeHuntRepository;
  private FindRepository findRepository;
  private HuntCompletionRepository huntCompletionRepository;
  private HuntService huntService;

  @Override
  public void onEnable() {
    PluginFiles.ensureDefaults(this);

    messagesService = new MessagesService(this);
    messagesService.load();

    activeHuntRepository = new YamlActiveHuntRepository(this);
    findRepository = new YamlFindRepository(this);
    huntCompletionRepository = new YamlHuntCompletionRepository(this);
    activeHuntRepository.load();
    findRepository.load();
    huntCompletionRepository.load();

    HuntArchiveService archiveService = new HuntArchiveService(this);
    RewardService rewardService =
        new RewardService(this, findRepository, huntCompletionRepository);
    huntService =
        new HuntService(
            this,
            activeHuntRepository,
            findRepository,
            huntCompletionRepository,
            rewardService,
            archiveService);

    getServer()
        .getPluginManager()
        .registerEvents(new HeadProtectionListener(huntService), this);
    getServer()
        .getPluginManager()
        .registerEvents(new HeadFindListener(huntService, messagesService), this);
    HeadHuntCommands.register(this, messagesService, huntService);
  }

  @Override
  public void onDisable() {
    if (activeHuntRepository != null) {
      activeHuntRepository.save();
    }
    if (findRepository != null) {
      findRepository.save();
    }
    if (huntCompletionRepository != null) {
      huntCompletionRepository.save();
    }
  }

  public MessagesService messages() {
    return messagesService;
  }

  public ActiveHuntRepository activeHunts() {
    return activeHuntRepository;
  }

  public FindRepository finds() {
    return findRepository;
  }

  public HuntCompletionRepository huntCompletions() {
    return huntCompletionRepository;
  }

  public HuntService hunts() {
    return huntService;
  }

  public void reloadPluginMessages() {
    messagesService.load();
  }
}
