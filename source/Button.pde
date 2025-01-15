
interface ButtonObserver
{
  void onButtonUp(Button button);
  void onButtonEnterHover(Button button);
  void onButtonExitHover(Button button);
}

class Button extends Entity
{
  String id;
  boolean hoverState = false;
  boolean visible = true;
  boolean enabled = true;
  
  Vector2 _bounds;  
  ButtonObserver _observer;
    
  boolean _buttonDown = false;
  boolean _wasMousePressed = false;

  color _buttonColor = color(0, 0, 100);
  boolean _hasDisabledPrompt = false;
  String _disabledPrompt;

  Button(String buttonID)
  {
    this(buttonID, 0, 0, 150, 50);
  }
  
  Button(String buttonID, float x, float y, float w, float h)
  {
    super(new Vector2(x, y));
    id = _disabledPrompt = buttonID;
    _bounds = new Vector2(w, h);
  }

  void setColor(color newColor) {_buttonColor = newColor;}
  void setDisabledPrompt(String prompt) 
  {
    _disabledPrompt = prompt;
    _hasDisabledPrompt = true;
  }

  float getWidth()
  {
    return _bounds.x;
  }
  
  void setObserver(ButtonObserver observer)
  {
    _observer = observer;
  }
  
  void update()
  {
    if (!enabled) 
    {
      _buttonDown = false;
      hoverState = false;
      return;
    }
    
    if (isPointInBounds(mouseX, mouseY))
    {
      if (!hoverState)
      {
        hoverState = true;
        onButtonEnterHover();
        _observer.onButtonEnterHover(this);
      }
      
      if (!_wasMousePressed && mousePressed())
      {
        _buttonDown = true;
      }
      // fire event on release
      if (_buttonDown && !mousePressed())
      {
        _buttonDown = false;
        onButtonUp();
        _observer.onButtonUp(this);
      }
    }
    else
    {
      _buttonDown = false;
      
      if (hoverState)
      {
        hoverState = false;
        onButtonExitHover();
        _observer.onButtonExitHover(this);
      }
    }
    
    _wasMousePressed = mousePressed();
  }

  void onButtonExitHover(){}
  void onButtonEnterHover(){}
  void onButtonUp(){}

  boolean mousePressed()
  {
    return mousePressed;// && mouseButton == LEFT;
  }
  
  void draw()
  {
    if (!visible) {return;}

    float alpha = 100;

    if (!enabled) alpha = 25;
    
    stroke(hue(_buttonColor), saturation(_buttonColor), brightness(_buttonColor), alpha);
    fill(0, 0, 0);
    
    if (hoverState)
    {
      if (_buttonDown)
      {
        stroke(0, 100, 100);
      }
      else
      {
        stroke(200, 100, 100);
      }
    }

    drawShape();
    drawText(alpha);
  }

  void drawShape()
  {
    rectMode(CENTER);
    rect(screenPosition.x, screenPosition.y, _bounds.x, _bounds.y);
  }

  void drawText(float alpha)
  {
    fill(0, 0, 100, alpha);
    textSize(14);
    textFont(smallFont);
    textAlign(CENTER, CENTER);

    if (enabled) 
    {
      text(id, screenPosition.x, screenPosition.y);
    }
    else
    {
      text(_disabledPrompt, screenPosition.x, screenPosition.y);
    }
  }
  
  boolean isPointInBounds(float x, float y)
  {
    if (x > screenPosition.x - _bounds.x * 0.5f && x < screenPosition.x + _bounds.x * 0.5f)
    {
      if (y > screenPosition.y - _bounds.y * 0.5f && y < screenPosition.y + _bounds.y * 0.5f)
      {
        return true;
      }
    }
    return false;
  }
}