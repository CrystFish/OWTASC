
class SolarSystemScreen extends Screen
{
  Actor _player;
  Actor _ship;
  SolarSystem _solarSystem;
    
  SectorButton _cometButton;
  SectorButton _hourglassButton_left;
  SectorButton _hourglassButton_right;
  SectorButton _timberButton;
  SectorButton _brittleButton;
  SectorButton _giantsButton;
  SectorButton _darkButton;
  QuantumMoonButton _quantumButton;

  SectorButton _focusSectorButton;

  ArrayList<SectorButton> _sectorButtons;
  
  SolarSystemScreen(SolarSystem solarSystem)
  {
    super();
    
    _solarSystem = solarSystem;
    _player = solarSystem.player;
    _ship = solarSystem.ship;

    _sectorButtons = new ArrayList<SectorButton>();
    float buttonHeight = height / 2;

    _sectorButtons.add(_hourglassButton_left = new HourglassTwinsButton(170, buttonHeight, false, _solarSystem.rockyTwin));
    _sectorButtons.add(_hourglassButton_right = new HourglassTwinsButton(170, buttonHeight, true, _solarSystem.sandyTwin));
    _sectorButtons.add(_timberButton = new TimberHearthButton(300, buttonHeight, _solarSystem.timberHearth));
    _sectorButtons.add(_brittleButton = new BrittleHollowButton(420, buttonHeight, _solarSystem.brittleHollow));
    _sectorButtons.add(_giantsButton = new GiantsDeepButton(580, buttonHeight, _solarSystem.giantsDeep));
    _sectorButtons.add(_darkButton = new DarkBrambleButton(810, buttonHeight, _solarSystem.darkBramble));
    _sectorButtons.add(_cometButton = new CometButton(600, buttonHeight + 200, _solarSystem.comet));

    SectorButton[] targets = {_hourglassButton_right, _timberButton, _brittleButton, _giantsButton};
    _sectorButtons.add(_quantumButton = new QuantumMoonButton(targets, _solarSystem.quantumMoon));

    for (int i = 0; i < _sectorButtons.size(); i++)
    {
      addButton(_sectorButtons.get(i));
    }
  }

  void onEnter()
  {
    _quantumButton.collapse();
  }

  void update() {}
  
  void render()
  {
    // draw sun
    stroke(40, 100, 100);
    fill(0, 0, 0);
    //fill(20, 100, 100);
    ellipse(-430, height/2, 1000, 1000);

    if (_ship.currentSector == null)
    {
      _ship.render();
    }
    if (_player.currentSector == null)
    {
      _player.render();
    }
  }

  void onButtonEnterHover(Button button)
  {
    if (_sectorButtons.contains(button))
    {
      _focusSectorButton = (SectorButton)button;
    }
  }

  void onButtonExitHover(Button button)
  {
    if (button == _focusSectorButton)
    {
      _focusSectorButton = null;
    }
  }
  
  void onButtonUp(Button button)
  { 
    /** SECTOR SELECTION **/
    if (_sectorButtons.contains(button))
    {
      selectSector(((SectorButton)button).getSector());
    }
  }
  
  void selectSector(Sector selectedSector) {}

  Vector2 getSectorScreenPosition(Sector sector)
  {
    for (int i = 0; i < _sectorButtons.size(); i++)
    {
      if (_sectorButtons.get(i).getSector() == sector)
      {
        return _sectorButtons.get(i).screenPosition;
      }
    }

    return null;
  }
}

class SolarSystemTelescopeScreen extends SolarSystemScreen
{
  Telescope _telescope;
  Button _exitButton;
  Button _nextFrequency;
  Button _previousFrequency;

  ArrayList<SignalSource> _signalSources;

  SolarSystemTelescopeScreen(SolarSystem solarSystem, Telescope telescope)
  {
    super(solarSystem);
    
    addButton(_nextFrequency = new FrequencyButton(true));
    addButton(_previousFrequency = new FrequencyButton(false));

    addButtonToToolbar(_exitButton = new Button("Exit", 0, 0, 150, 50));

    _telescope = telescope;
  }

  void onEnter()
  {
    super.onEnter();
    noCursor();

    // must do this after quantum moon collapses
    _signalSources = new ArrayList<SignalSource>();
    _signalSources.add(new SignalSource(_cometButton.screenPosition, _cometButton.getSector()));
    _signalSources.add(new SignalSource(_hourglassButton_left.screenPosition, _hourglassButton_left.getSector()));
    _signalSources.add(new SignalSource(_timberButton.screenPosition, _timberButton.getSector()));
    _signalSources.add(new SignalSource(_brittleButton.screenPosition, _brittleButton.getSector()));
    _signalSources.add(new SignalSource(_giantsButton.screenPosition, _giantsButton.getSector()));
    _signalSources.add(new SignalSource(_darkButton.screenPosition, _darkButton.getSector()));
    _signalSources.add(new SignalSource(_quantumButton.screenPosition, _quantumButton.getSector()));
  }

  void onExit()
  {
    cursor();
  }

  void update()
  {
    super.update();
    _telescope.update(_signalSources);
  }

  void render()
  {
    super.render();
    _telescope.render();

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawName();
    }

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawZoomPrompt();
    }
  }

  void selectSector(Sector selectedSector)
  {
    gameManager.loadSectorTelescopeView(selectedSector);
  }

  void onButtonUp(Button button)
  {
    super.onButtonUp(button);

    if (button == _exitButton)
    {
      gameManager.popScreen();
    }
    else if (button == _nextFrequency)
    {
      _telescope.nextFrequency();
    }
    else if (button == _previousFrequency)
    {
      _telescope.previousFrequency();
    }
  }
}

class SolarSystemMapScreen extends SolarSystemScreen
{
  Button _databaseButton;
  Button _telescopeButton;

  SolarSystemMapScreen(SolarSystem solarSystem)
  {
    super(solarSystem);
    addButtonToToolbar(_databaseButton = new Button("View Database", 0, 0, 150, 50));
    addButtonToToolbar(_telescopeButton = new Button("Scan For Signals", 0, 0, 150, 50));
  }

  void render()
  {
    super.render();

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawName();
    }

    feed.render();
    timeLoop.renderTimer();
  }

  void onEnter()
  {
    super.onEnter();
    setActorPosition(_player);
    setActorPosition(_ship);
  }

  void setActorPosition(Actor actor)
  {
    Vector2 idlePos = new Vector2(200, height - 200);

    // set player position
    if (actor.lastSector != null)
    {
      actor.setScreenPosition(getSectorScreenPosition(actor.lastSector));
    }
    else
    {
      actor.setScreenPosition(idlePos);
    }

    actor.moveToScreenPosition(idlePos);
    actor.lastSector = null;
  }

  void update()
  {
    _player.update();
    _ship.update();
  }

  void onButtonUp(Button button)
  {
    /** OPEN DATABASE **/
    if (button == _databaseButton)
    {
      gameManager.pushScreen(gameManager.databaseScreen);
    }
    else if (button == _telescopeButton)
    {
      gameManager.loadTelescopeView();
    }

    super.onButtonUp(button);
  }

  void selectSector(Sector selectedSector)
  {
    Sector nextSector = selectedSector;
    
    // check if the player is already in a Sector...if not, fly to that Sector
    if (_solarSystem.ship.currentSector == null)
    {
      nextSector.addActor(_solarSystem.ship);
    }
    
    // check if the ship is already in a Sector...if not fly to that Sector
    if (_solarSystem.player.currentSector == null)
    {
      nextSector.addActor(_solarSystem.player);
    }

    gameManager.loadSector(nextSector);
    nextSector.onArrival();
  }
}