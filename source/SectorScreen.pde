
class SectorScreen extends Screen implements NodeButtonObserver, NodeActionObserver, DatabaseObserver
{
  Actor _player;
  Actor _ship;
  Sector _sector;
  
  SectorEditor _editor;
  
  Node _focusNode;

  Button _databaseButton;
  Button _liftoffButton;
  Button _waitButton;
  Button _telescopeButton;

  String _actionlessPrompt = "inaccessible";
  ArrayList<NodeAction> _actions;

  SectorScreen(Sector sector, Actor player, Actor ship)
  {
    super();
    
    _actions = new ArrayList<NodeAction>();
    _editor = new SectorEditor(sector);
    
    _player = player;
    _ship = ship;
    _sector = sector;

    addButtonToToolbar(_databaseButton = new Button("Use Database", 0, 0, 150, 50));
    addButtonToToolbar(_telescopeButton = new Button("Scan for Signals", 0, 0, 150, 50));
    _telescopeButton.setDisabledPrompt("Scan for Signals\n(view obstructed)");

    addButtonToToolbar(_waitButton  = new Button("Wait [1 min]", 0, 0, 150, 50));
    addButtonToToolbar(_liftoffButton  = new Button("Leave Sector", 0, 0, 150, 50));
    _liftoffButton.setDisabledPrompt("Leave Sector\n(must be at ship)");
  }

  void onEnter()
  {
    _sector.addNodeButtonObserver(this);
    _sector.addNodeButtonObserver(_editor);
    _sector.updateNodeRanges(isPlayerInShip(), _player.currentNode);
    _focusNode = null;
  }
  
  void onExit()
  {
    _sector.removeAllNodeButtonObservers();
  }

  void onButtonUp(Button button)
  {
    if (button == _databaseButton)
    {
      gameManager.databaseScreen.setObserver(this);
      gameManager.pushScreen(gameManager.databaseScreen);
    }
    else if (button == _liftoffButton)
    {
      _sector.removeActor(_player);
      _sector.removeActor(_ship);
      gameManager.loadSolarSystemMap();
      feed.clear();
      feed.publish("you leave " + _sector.getName());
    }
    else if (button == _telescopeButton)
    {
      gameManager.loadTelescopeView();
    }
    else if (button == _waitButton)
    {
      timeLoop.waitFor(1);
    }
  }

  void onInvokeClue(Clue clue)
  {
    // check for node-specific clue effects
    if (_player.currentNode != null && _player.currentNode.isExplorable())
    {
      ExploreData exploreData = _player.currentNode.getExploreData();
      exploreData.parseJSON();

      if (exploreData.canClueBeInvoked(clue.id))
      {
        gameManager.popScreen(); // force-quit the database screen
        gameManager.pushScreen(new ExploreScreen(_player.currentNode)); // push on a new explorescreen
        exploreData.invokeClue(clue.id);
        exploreData.explore();
        return;
      }
    }
    
    // next try the whole sector
    if (_player.currentSector != null && _player.currentSector.canClueBeInvoked(clue))
    {
      _player.currentSector.invokeClue(clue);
    }
    else
    {
      feed.publish("that doesn't help you right now", true);
    }
  }

  void update()
  {
    // update action button visibility
    _liftoffButton.enabled = (_player.currentNode == _ship.currentNode);
    _telescopeButton.enabled = (_player.currentNode != null && _player.currentNode.allowTelescope && _player.currentSector.allowTelescope());

    _sector.update();
    if (EDIT_MODE) _editor.update();
  }

  void renderBackground()
  {
    super.renderBackground();
    _sector.renderBackground();
  }

  void render()
  {
    _sector.render();
    feed.render();
    timeLoop.renderTimer();

    fill(0, 0, 100);
    textSize(18);
    textFont(mediumFont);
    textAlign(RIGHT);
    text(_sector.getName(),width - 20, height - 100);

    if (!active) return;
    if (EDIT_MODE) _editor.render();

    drawNodeGUI(_focusNode, _actions);
  }

  void drawNodeGUI(Node target, ArrayList<NodeAction> actions)
  {
    if (target == null || (_actions.size() == 0 && target == _player.currentNode)) {return;}

    smallFont();

    float yOffset = 60;
    float promptWidth = textWidth(_actionlessPrompt);

    // find prompt width
    for (int i = 0; i < actions.size(); i++) 
    {
      promptWidth = max(promptWidth, textWidth(actions.get(i).getPrompt()));
    }

    // draw box
    stroke(200, 100, 100);
    fill(0, 0, 0);
    rectMode(CORNER);
    rect(target.screenPosition.x - promptWidth * 0.5 - 10, target.screenPosition.y + yOffset - 15, promptWidth + 15, max(20, 20 * actions.size()) + 10);

    // draw prompts
    fill(0, 0, 100);
    textAlign(LEFT, CENTER);

    for (int i = 0; i < actions.size(); i++) 
    {
      text(actions.get(i).getPrompt(), target.screenPosition.x - promptWidth * 0.5, target.screenPosition.y + yOffset + 20 * i);
    }

    if (actions.size() == 0)
    {
      text(_actionlessPrompt, target.screenPosition.x - promptWidth * 0.5, target.screenPosition.y + yOffset);
    }
  }

  void onTravelAttempt(boolean succeeded, Node node, NodeConnection connection){}
  void onExploreNode(Node node){}
  void onProbeNode(Node node){}

  void onNodeSelected(Node node)
  {
    /** EXCECUTE ACTION **/
    for (int i = 0; i < _actions.size(); i++) 
    {
      if (_actions.get(i).getMouseButton() == mouseButton)
      {
        _actions.get(i).execute();
        break;
      }
    }

    refreshAvailableActions();
  }
  
  void onNodeGainFocus(Node node)
  {
    _focusNode = node;
    refreshAvailableActions();
  }
  
  void onNodeLoseFocus(Node node)
  {
    if (node == _focusNode)
    {
      _focusNode = null;
      refreshAvailableActions();
    }
  }

  void refreshAvailableActions()
  {
    _actions.clear();

    if (_focusNode == null) 
    {
      return;
    }

    if (_player.currentNode != _focusNode)
    {
      if (_focusNode.inRange())
      {
        if (_focusNode.isProbeable())
        {
          _actions.add(new ProbeAction(RIGHT, _player, _focusNode, this));
        }
        
        if (isPlayerInShip() && _focusNode.shipAccess)
        {
          _actions.add(new TravelAction(LEFT, _player, _ship, _focusNode, this));
        }
        else
        {
          _actions.add(new TravelAction(LEFT, _player, _focusNode, this));
        }
      }
    }
    else
    {
      if (_focusNode.isExplorable())
      {
        _actions.add(new ExploreAction(LEFT, _focusNode, this));
      }
    }
  }

  boolean isPlayerInShip()
  {
    return(_player.currentNode == _ship.currentNode);
  }
}