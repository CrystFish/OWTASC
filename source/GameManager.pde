class GameManager extends ScreenManager implements GlobalObserver
{
  // game screens
  TitleScreen titleScreen;
  DatabaseScreen databaseScreen;
  SolarSystemMapScreen solarSystemMapScreen;
  
  // game objects
  SolarSystem _solarSystem;
  Telescope _telescope;

  boolean _flashbackTriggered = false;
  
  void newGame()
  {
    setupSolarSystem();

    titleScreen = new TitleScreen();
    databaseScreen = new DatabaseScreen();
    
    pushScreen(titleScreen);
  }

  void resetTimeLoop()
  {
    _flashbackTriggered = false;
    _screenStack.clear();
    setupSolarSystem();
    loadSector(SectorName.TIMBER_HEARTH);
  }

  void setupSolarSystem()
  {
    messenger.removeAllObservers();
    messenger.addObserver(this);

    feed.init();
    timeLoop.init();
    playerData.init();

    _telescope = new Telescope();

    _solarSystem = new SolarSystem();
    _solarSystem.timberHearth.addActor(_solarSystem.player, "Village");

    solarSystemMapScreen = new SolarSystemMapScreen(_solarSystem);

    if (playerData.knowsLaunchCodes())
    {
      messenger.sendMessage("spawn ship");
    }

    locator = new Locator();
  }

  // runs after everything else updates
  void lateUpdate()
  {
    // check if the sun explodes (this check has to be last to override all other screens)
    timeLoop.lateUpdate();

    // check if the player died
    if (playerData.isPlayerDead() && !_flashbackTriggered)
    {
      _flashbackTriggered = true;

      if (timeLoop.getEnabled())
      {
        swapScreen(new FlashbackScreen());
      }
      else
      {
        swapScreen(new GameOverScreen());
      }
    }
  }

  void onReceiveGlobalMessage(Message message)
  {
    // TRIGGERED FROM SECTORSCREEN (NOT EXPLORE SCREEN)
    if (message.id.equals("death by anglerfish"))
    {
      pushScreen(new DeathByAnglerfishScreen());
    }
    else if (message.id.equals("dive attempt"))
    {
      pushScreen(new DiveAttemptScreen());
    }
    // TRIGGERED FROM EVENT SCREEN
    else if (message.id.equals("follow the vine"))
    {
      swapScreen(new FollowTheVineScreen());
    }
    // TRIGGERED FROM EXPLORE DATA
    else if (message.id.equals("explore ancient vessel"))
    {
      swapScreen(new AncientVesselScreen());
    }
    else if (message.id.equals("time loop central"))
    {
      swapScreen(new TimeLoopCentralScreen());
    }
    else if (message.id.equals("older than the universe"))
    {
      swapScreen(new EyeOfTheUniverseScreen());
    }
    else if (message.id.equals("explore bramble outskirts"))
    {
      swapScreen(new BrambleOutskirtsScreen());
    }
  }

  void loadTelescopeView()
  {
    pushScreen(new SolarSystemTelescopeScreen(_solarSystem, _telescope));

    // if (_solarSystem.player.currentSector != null)
    // {
    //   loadSectorTelescopeView(_solarSystem.player.currentSector);
    // }
  }

  void loadSectorTelescopeView(Sector sector)
  {
    pushScreen(new SectorTelescopeScreen(sector, _telescope));
  }
  
  void loadSolarSystemMap()
  {
    swapScreen(solarSystemMapScreen);
  }
  
  void loadSector(SectorName sectorName)
  {
    loadSector(_solarSystem.getSectorByName(sectorName));
  }

  void loadSector(Sector sector)
  {
    swapScreen(new SectorScreen(sector, _solarSystem.player, _solarSystem.ship));
  }
}