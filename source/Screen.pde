
abstract class Screen implements ButtonObserver
{
  boolean active = false;
  boolean overlay = false;

  ArrayList<Button> _buttons;
  ArrayList<Button> _toolbarButtons;
  Vector2[] _starPositions;

  Entity _toolbarRoot;
  
  Screen()
  {
    _buttons = new ArrayList<Button>();
    _toolbarButtons = new ArrayList<Button>();
    _starPositions = new Vector2[1000];
    
    for(int i = 0; i < _starPositions.length; i++)
    {
      _starPositions[i] = new Vector2(random(0, width), random(90, height - 90));
    }

    _toolbarRoot = new Entity(width/2, height - 50);
  }
  
  void addButton(Button button)
  {
    _buttons.add(button);
    button.setObserver(this);
  }
  
  void removeButton(Button button)
  {
    _buttons.remove(button);
  }

  void addButtonToToolbar(Button button)
  {
    addButton(button);
    _toolbarButtons.add(button);
    _toolbarRoot.addChild(button);
    updateToolbarPositions();
  }

  void removeButtonFromToolbar(Button button)
  {
    removeButton(button);
    _toolbarButtons.remove(button);
    _toolbarRoot.removeChild(button);
    updateToolbarPositions();
  }

  void updateToolbarPositions()
  {
    float margins = 10;
    float toolbarWidth = -margins;

    for (int i = 0; i < _toolbarButtons.size(); i++)
    {
      toolbarWidth += margins;
      toolbarWidth += _toolbarButtons.get(i).getWidth();
    }

    float xPos = -(toolbarWidth * 0.5f);

    for (int i = 0; i < _toolbarButtons.size(); i++)
    {
      float buttonHalfWidth = _toolbarButtons.get(i).getWidth() * 0.5f;
      xPos += buttonHalfWidth;
      _toolbarButtons.get(i).setPosition(xPos, 0);
      xPos += buttonHalfWidth + margins;
    }
  }
  
  abstract void update();
  abstract void render();
  
  void onEnter(){}
  void onExit(){}
  
  void onButtonUp(Button button){}
  void onButtonEnterHover(Button button){}
  void onButtonExitHover(Button button){}
  
  void updateInput()
  {
    for(int i = 0; i < _buttons.size(); i++)
    {
      _buttons.get(i).update();
    }
  }
  
  void renderBackground()
  {
    color bgColor = color(0, 0, 0);
    color starColor = color(0, 0, 100);

    // superhack to invert colors when player is at EYE_OF_THE_UNIVERSE
    if (playerData.isPlayerAtEOTU())
    {
      bgColor = color(0, 0, 100);
      starColor = color(0, 0, 0);
    }

    background(bgColor);
    noStroke();
    
    /** DRAW STARFIELD **/
    for(int j = 0; j < _starPositions.length; j++)
    {
      fill(starColor);
      rectMode(CENTER);
      rect(_starPositions[j].x, _starPositions[j].y, 2, 2);
    }
  }
  
  void renderButtons()
  {
    // only render buttons if the screen is active
    if (active)
    {
      for(int i = 0; i < _buttons.size(); i++)
      {
        _buttons.get(i).render();
      }
    }
  }
}