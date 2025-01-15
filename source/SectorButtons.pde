
abstract class SectorButton extends Button
{
  Sector _sector;
  Node node;

  SectorButton(String id, float x, float y, float w, float h, Sector sector)
  {
    super(id, x, y, w, h);
    node = new Node(x, y);
    _sector = sector;
  }

  float getRadius()
  {
    return _bounds.y * 0.5f;
  }

  Sector getSector()
  {
    return _sector;
  }
  
  void render()
  {
    drawNonHighlighted();

    if (hoverState)
    {
      strokeWeight(2);
    }
    
    drawPlanet();
    drawYouAreHere();
    
    strokeWeight(1);
  }
  
  abstract void drawPlanet();

  void drawNonHighlighted(){}
  
  void drawName(String name, float xPos)
  {
    fill(0, 0, 100);
    textAlign(CENTER, CENTER);
    textSize(18);
    text(name, xPos, screenPosition.y - getRadius() - 40);
  }

  void drawName()
  {
    drawName(id, screenPosition.x);
  }

  void drawYouAreHere()
  {
    if (locator.ship.currentSector == _sector)
    {
      ((Ship)locator.ship).drawAt(screenPosition.x, screenPosition.y, 0.5f, true);
    }

    if (locator.player.currentSector == _sector)
    {
      ((Player)locator.player).drawAt(screenPosition.x, screenPosition.y, 0.5f);
    }
  }

  void drawZoomPrompt()
  {
    textSize(14);
    textFont(smallFont);

    fill(0, 0, 0);
    stroke(0, 0, 100);
    rectMode(CENTER);
    rect(screenPosition.x, screenPosition.y + getRadius() + 40, textWidth("L - Zoom In") + 10, 20);

    fill(0, 0, 100);
    textAlign(CENTER, CENTER);
    text("L - Zoom In", screenPosition.x, screenPosition.y + getRadius() + 40);
  }
}

class CometButton extends SectorButton
{
  CometButton(float x, float y, Sector sector)
  {
    super("The Comet", x, y, 40, 40, sector);
  }
  
  void drawPlanet()
  {
    noStroke();
    fill(0, 0, 0);

    pushMatrix();
      translate(screenPosition.x, screenPosition.y);
      triangle(0, getRadius(), 0, -getRadius(), 130, 0);
      stroke(200, 30, 100);
      arc(0, 0, _bounds.y, _bounds.y, PI * 0.5f, PI * 1.5f);
      line(0, _bounds.y * 0.5f, 130, 0);
      line(0, -_bounds.y * 0.5f, 130, 0);
    popMatrix();
  }
}

class HourglassTwinsButton extends SectorButton
{
  float _centerX;
  boolean _isRightTwin = false;
  String _twinName;

  HourglassTwinsButton(float centerX, float y, boolean isRightTwin, Sector sector)
  {
    super("Hourglass Twin", centerX, y, 50, 50, sector);
    _isRightTwin = isRightTwin;
    _centerX = centerX;
    
    if (isRightTwin) 
    {
      _twinName = "Sandy ";
      setPosition(centerX + 35, position.y);
    }
    else
    {
      _twinName = "Rocky ";
      setPosition(centerX - 35, position.y);
    }
  }

  void drawName()
  {
    super.drawName(_twinName + id, _centerX);
  }

  void drawNonHighlighted()
  {
    if (!_isRightTwin)
    {
      stroke(60, 100, 100);
      fill(0, 0, 0);
      rectMode(CENTER);
      rect(_centerX, position.y, _bounds.y * 1.5f, _bounds.y * 0.2f);
    }
  }
  
  void drawPlanet()
  {
    stroke(60, 100, 100);
    fill(0, 0, 0);    
    ellipse(position.x, position.y, _bounds.y, _bounds.y);
  }
}

class TimberHearthButton extends SectorButton
{
  TimberHearthButton(float x, float y, Sector sector)
  {
    super("Timber Hearth", x, y, 80, 80, sector);
  }
  
  void drawPlanet()
  {
    stroke(200, 100, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}

class BrittleHollowButton extends SectorButton
{
  BrittleHollowButton(float x, float y, Sector sector)
  {
    super("Brittle Hollow", x, y, 80, 80, sector);
  }
  
  void drawPlanet()
  {
    stroke(0, 100, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}

class GiantsDeepButton extends SectorButton
{
  GiantsDeepButton(float x, float y, Sector sector)
  {
    super("Giant's Deep", x, y, 150, 150, sector);
  }
  
  void drawPlanet()
  {
    stroke(180, 100, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}

class DarkBrambleButton extends SectorButton
{
  DarkBrambleButton(float x, float y, Sector sector)
  {
    super("Dark Bramble", x, y, 230, 230, sector);
  }
  
  void drawPlanet()
  {
    stroke(120, 100, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}

class QuantumMoonButton extends SectorButton
{
  SectorButton _currentTarget;
  SectorButton[] _targets;

  QuantumMoonButton(SectorButton[] targets, Sector sector)
  {
    super("Quantum Moon", 0, 0, 30, 30, sector);
    _targets = targets;
  }

  void collapse()
  {
    ((QuantumMoon)_sector).collapse();
    updatePosition();
  }

  void updatePosition()
  {
    int i = ((QuantumMoon)_sector).getQuantumLocation();

    println(i);

    if (i == 4)
    {
      _currentTarget = null;
    }
    else
    {
      _currentTarget = _targets[i];
    }

    if (_currentTarget != null)
    {
      setPosition(_currentTarget.position.x - 25, _currentTarget.position.y - _currentTarget.getRadius() - 30);
    }
    else
    {
      setPosition(-1000, -1000);
    }
  }
  
  void drawPlanet()
  {
    stroke(300, 50, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}