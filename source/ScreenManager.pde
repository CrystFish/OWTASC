abstract class ScreenManager
{
  ArrayList<Screen> _screenStack;
  boolean _skipRender = false;

  ScreenManager()
  {
    _screenStack = new ArrayList<Screen>();
  }

  void lateUpdate()
  {
    // just in case gamemanager needs to do anything
  }

  void runGameLoop()
  {
    if (_screenStack.size() > 0)
    {
      _skipRender = false;
      
      // update active screen
      Screen activeScreen = _screenStack.get(_screenStack.size() - 1);
      activeScreen.updateInput();
      activeScreen.update();
      lateUpdate();

      if (_skipRender)
      {
        return;
      }

      // find which screens should get rendered
      int lowestRenderIndex = 0;

      for (int i = _screenStack.size() - 1; i >= 0; i--)
      {
        if (!_screenStack.get(i).overlay)
        {
          lowestRenderIndex = i;
          break;
        }
      }

      // render screens lowest-first
      for (int i = lowestRenderIndex; i < _screenStack.size(); i++)
      {
        _screenStack.get(i).renderBackground();
        _screenStack.get(i).renderButtons();
        _screenStack.get(i).render();
      }
    }
    else
    {
      println("No screens on the stack!!!");
    }
  }
  
  void swapScreen(Screen newScreen)
  {
    if (_screenStack.size() > 0)
    {
      _screenStack.get(_screenStack.size() - 1).onExit();
      _screenStack.get(_screenStack.size() - 1).active = false;
      _screenStack.remove(_screenStack.size() - 1);
    }
    
    _screenStack.add(newScreen);
    _screenStack.get(_screenStack.size() - 1).active = true;
    _screenStack.get(_screenStack.size() - 1).onEnter();

    _skipRender = true;

    println("SWAP: " + newScreen);
  }
  
  void pushScreen(Screen nextScreen)
  {
    if (_screenStack.size() > 0)
    {
      _screenStack.get(_screenStack.size() - 1).onExit();
      _screenStack.get(_screenStack.size() - 1).active = false;
    }
    
    _screenStack.add(nextScreen);
    nextScreen.active = true;
    nextScreen.onEnter();

    _skipRender = true;

    println("PUSH: " + nextScreen);
  }
  
  void popScreen()
  {
    _screenStack.get(_screenStack.size() - 1).onExit();
    _screenStack.get(_screenStack.size() - 1).active = false;
    _screenStack.remove(_screenStack.size() - 1);
    
    if (_screenStack.size() > 0)
    {
      _screenStack.get(_screenStack.size() - 1).active = true;
      _screenStack.get(_screenStack.size() - 1).onEnter();
    }

    _skipRender = true;

    println("POP");
  }
}