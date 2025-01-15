class Comet extends Sector
{  
  void load()
  {
    _name = "the Comet";
    loadFromJSON("sectors/comet.json");
    //setAnchorOffset(100, 30);
  }
  
  void drawSectorBackdrop()
  {
    stroke(200, 30, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 300, 300);
  }
}

class RockyTwin extends Sector
{  
  void load()
  {
    _name = "the rocky Hourglass Twin";
    loadFromJSON("sectors/rocky_twin.json");
    //setAnchorOffset(100, 30);
  }
  
  void drawSectorBackdrop()
  {
    stroke(60, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class SandyTwin extends Sector
{  
  void load()
  {
    _name = "the sandy Hourglass Twin";
    loadFromJSON("sectors/sandy_twin.json");
    //setAnchorOffset(100, 30);
  }
  
  void drawSectorBackdrop()
  {
    stroke(60, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class TimberHearth extends Sector
{  
  void load()
  {
    _name = "Timber Hearth";
    loadFromJSON("sectors/timber_hearth.json");
    //setAnchorOffset(100, 30);
  }
  
  void drawSectorBackdrop()
  {
    stroke(200, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class BrittleHollow extends Sector
{  
  void load()
  { 
    _name = "Brittle Hollow";
    loadFromJSON("sectors/brittle_hollow.json");
  }
  
  void drawSectorBackdrop()
  {
    stroke(0, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
    drawBlackHole(35);
  }

  public void drawBlackHole(float radius) 
  {
    // black hole
    noStroke();
    fill(0,100,0);
    ellipse(0, 0, radius * 0.5f, radius * 0.5f);
    
    // spiral
    noFill();
    strokeWeight(1);
    stroke(260, 80, 70);

    float start = radius * 0.125f;
    float end = radius;
    float spirals = 3f;
    float step = 0.1f * PI;

    float x0 = start;
    float y0 = 0;

    for (float t = step; t < spirals * TWO_PI; t += step) 
    {
      float theta = t;

      float r = (theta / (spirals * TWO_PI)) * (end - start) + start;
      float x3 = r * cos(theta);
      float y3 = r * sin(theta);

      theta -= step / 3;
      r = (theta / (spirals * TWO_PI)) * (end - start) + start;
      float x2 = r * cos(theta);
      float y2 = r * sin(theta);

      theta -= step / 3;
      r = (theta / (spirals * TWO_PI)) * (end - start) + start;
      float x1 = r * cos(theta);
      float y1 = r * sin(theta);

      bezier(x0, y0, x1, y1, x2, y2, x3, y3);
      x0 = x3;
      y0 = y3;
    }
  }
}

class GiantsDeep extends Sector
{  
  void load()
  { 
    _name = "Giant's Deep";
    loadFromJSON("sectors/giants_deep.json");
    setAnchorOffset(0, 60);
  }
  
  void drawSectorBackdrop()
  {
    fill(0, 0, 0);

    stroke(180, 100, 100);
    ellipse(0, 0, 600, 600);

    stroke(200, 100, 60);
    ellipse(0, 0, 400, 400);

    // stroke(220, 100, 50);
    // ellipse(0, 0, 100, 100);
  }
}

class DarkBramble extends Sector
{
  ArrayList<Node> _fogLightNodes;
  ArrayList<Vector2> _fogLightPositions;

  DarkBramble()
  {
    super();
    _fogLightNodes = new ArrayList<Node>();
    _fogLightPositions = new ArrayList<Vector2>();
  }

  void load()
  {
    _name = "Dark Bramble";
    loadFromJSON("sectors/dark_bramble.json");

    for (int i = 0; i < _fogLightNodes.size(); i++)
    {
      int index = int(random(0, _fogLightPositions.size()));
      //println("pos: " + _fogLightPositions.get(index));
      _fogLightNodes.get(i).setPosition(_fogLightPositions.get(index));
      _fogLightPositions.remove(index);
    }
  }

  Node createNode(String name, JSONObject nodeObj)
  {
    Node newNode;

    if (nodeObj.hasKey("anglerfish"))
    {
      newNode = new AnglerfishNode(name, nodeObj);
      _fogLightNodes.add(newNode);
      _fogLightPositions.add(new Vector2(newNode.position));
    }
    else
    {
      newNode = new Node(name, nodeObj);
    }

    if (nodeObj.getBoolean("fog light", false))
    {
      _fogLightNodes.add(newNode);
      _fogLightPositions.add(new Vector2(newNode.position));
    }

    return newNode;
  }
  
  void drawSectorBackdrop()
  {
    stroke(120, 100, 100);
    fill(0, 0, 0);
    ellipse(100, 0, 700, 500);
  }
}

class QuantumMoon extends Sector
{
  int _quantumLocation = 0;
  int _turnsSinceEOTU = 0;

  void load()
  {
    _name = "Quantum Moon";
    loadFromJSON("sectors/quantum_moon.json");
    //setAnchorOffset(100, 30);
  }

  void onReceiveGlobalMessage(Message message)
  {
    if (message.id.equals("quantum entanglement"))
    {
      if (locator.player.currentSector == this)
      {
        collapse();
        onQuantumEntanglement();
      }
    }
  }

  void onArrival()
  {
    // overrides normal arrival screen
    gameManager.pushScreen(new QuantumArrivalScreen());
  }

  Node createNode(String name, JSONObject nodeObj)
  {
    return new QuantumNode(name, nodeObj);
  }

  void collapse()
  {
    // move to random location
    int tLocation = _quantumLocation;
    while (tLocation == _quantumLocation)
    {
      tLocation = int(random(5));
    }
    _quantumLocation = tLocation;

    // limit how many turns can pass without going to the EOTU
    if (_quantumLocation != 4)
    {
      _turnsSinceEOTU++;

      if (_turnsSinceEOTU > 4)
      {
        _quantumLocation = 4;
        _turnsSinceEOTU = 0;
      }
    }
    
    // update node visibility
    for (int i = 0; i < _nodes.size(); i++)
    {
      ((QuantumNode)_nodes.get(i)).updateQuantumStatus(_quantumLocation);
    }
  }

  void onQuantumEntanglement()
  {
    super.onQuantumEntanglement();
      
    // remove ship
    if (locator.ship.currentSector == this)
    {
      locator.ship.currentSector.removeActor(locator.ship);
    }
  }

  int getQuantumLocation() {return _quantumLocation;}

  boolean allowTelescope() {return false;}
  
  void drawSectorBackdrop()
  {
    stroke(300, 50, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 300, 300);
  }

  void removeActor(Actor actor)
  {
    super.removeActor(actor);
    actor.lastSector = null; // prevents animation of leaving Sector
  }
}

class EyeOfTheUniverse extends Sector
{  
  void load()
  {
    _name = "The Thing Older Than The Universe";
    loadFromJSON("sectors/eye_of_the_universe.json");
  }

  boolean allowTelescope() {return false;}
  
  void drawSectorBackdrop()
  {
  }
}