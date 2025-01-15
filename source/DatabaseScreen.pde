interface DatabaseObserver
{
  void onInvokeClue(Clue clue);
}

class DatabaseScreen extends Screen implements ClueButtonObserver
{
  Entity _clueRoot;
  Clue _activeClue;
  DatabaseObserver _observer;

  DatabaseScreen()
  {
    super();
    addButtonToToolbar(new Button("Close Database",  0, 0, 150, 50));
    _clueRoot = new Entity(100, 140);

    // create clue buttons using PlayerData's list of clues
    for (int i = 0; i < playerData.getClueCount(); i++)
    {
      ClueButton clueButton = new ClueButton(playerData.getClueAt(i), i * 40, this);
      addButton(clueButton);
      _clueRoot.addChild(clueButton);
    }
  }

  void setObserver(DatabaseObserver observer)
  {
    _observer = observer;
  }

  void onEnter()
  {
  }

  void onExit()
  {
    _observer = null;
  }

  void onClueMouseOver(Clue clue)
  {
    _activeClue = clue;
  }

  void onClueSelected(Clue clue)
  {
    if (_observer != null)
    {
      _observer.onInvokeClue(clue);
    }
    else
    {
      feed.publish("that doesn't help you right now", true);
    }
  }

  void onButtonUp(Button button)
  {
    if (button.id == "Close Database")
    {
      gameManager.popScreen();
    }
  }

  void update() {}

  void render()
  {
    fill(0, 0, 0);
    stroke(0, 0, 100);

    rectMode(CORNER);

    float x = width/2 - 100;
    float y = 200;
    float w = 500;
    float h = 300;

    rect(x, y, w, h);

    String _displayText = "Select An Entry";

    if (_activeClue != null)
    {
      _displayText = _activeClue.description;
    }
    else if (playerData.getKnownClueCount() == 0)
    {
      _displayText = "No Entries Yet";
    }

    textFont(mediumFont);
    textSize(18);
    textAlign(LEFT, TOP);
    fill(0, 0, 100);
    text(_displayText, x + 10, y + 10, w - 20, h - 20);

    feed.render();
  }
}

interface ClueButtonObserver
{
  void onClueSelected(Clue clue);
  void onClueMouseOver(Clue clue);
}

class ClueButton extends Button
{
  Clue _clue;
  ClueButtonObserver _observer;

  ClueButton(Clue clue, float y, ClueButtonObserver observer)
  {
    super(clue.name, (textWidth(clue.name) + 10) * 0.5f, y, textWidth(clue.name) + 10, 20);
    _clue = clue;
    _observer = observer;
  }

  Clue getClue()
  {
    return _clue;
  }

  void update()
  {
    enabled = _clue.discovered;
    visible = _clue.discovered;
    super.update();
  }

  void draw()
  {
    if (!visible) {return;}
    
    super.draw();

    color symbolColor;

    if (_clue.curiosity == Curiosity.VESSEL)
    {
        symbolColor = color(100, 100, 100);
    }
    else if (_clue.curiosity == Curiosity.ANCIENT_PROBE_LAUNCHER)
    {
      symbolColor = color(200, 100, 100);
    }
    else if (_clue.curiosity == Curiosity.TIME_LOOP_DEVICE)
    {
      symbolColor = color(20, 100, 100);
    }
    else
    {
      symbolColor = color(300, 100, 100);
    }

    fill(symbolColor);
    noStroke();
    ellipse(screenPosition.x - _bounds.x * 0.5f - 20, screenPosition.y, 10, 10);

    noFill();
    stroke(symbolColor);
    ellipse(screenPosition.x - _bounds.x * 0.5f - 20, screenPosition.y, 15, 15);
  }

  void onButtonEnterHover()
  {
    _observer.onClueMouseOver(_clue);
  }

  void onButtonUp()
  {
    _observer.onClueSelected(_clue);
  }
}