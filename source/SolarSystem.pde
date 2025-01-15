
class SolarSystem implements GlobalObserver
{
  Actor player;
  Actor ship;
  
  Sector comet;
  Sector rockyTwin;
  Sector sandyTwin;
  Sector timberHearth;
  Sector brittleHollow;
  Sector giantsDeep;
  Sector darkBramble;
  Sector quantumMoon;
  Sector eyeOfTheUniverse;

  ArrayList<Sector> _sectorList;
  
  SolarSystem()
  {
    messenger.addObserver(this);

    println("Solar System loading...");

    _sectorList = new ArrayList<Sector>();

    _sectorList.add(comet = new Comet());
    _sectorList.add(rockyTwin = new RockyTwin());
    _sectorList.add(sandyTwin = new SandyTwin());
    _sectorList.add(timberHearth = new TimberHearth());
    _sectorList.add(brittleHollow = new BrittleHollow());
    _sectorList.add(giantsDeep = new GiantsDeep());
    _sectorList.add(darkBramble = new DarkBramble());
    _sectorList.add(quantumMoon = new QuantumMoon());
    _sectorList.add(eyeOfTheUniverse = new EyeOfTheUniverse());
    
    comet.load();
    rockyTwin.load();
    sandyTwin.load();
    timberHearth.load();
    brittleHollow.load();
    giantsDeep.load();
    darkBramble.load();
    quantumMoon.load();
    eyeOfTheUniverse.load();

    player = new Player();
    ship = new Ship(player);
  }

  void onReceiveGlobalMessage(Message message)
  {
    if (message.id.equals("spawn ship"))
    {
      timberHearth.addActor(ship, "Village");

      // keeps player on top of ship
      Node tNode = player.currentNode;
      timberHearth.removeActor(player);
      timberHearth.addActor(player, tNode);
    }
    else if (message.id.equals("move to"))
    {
      Node node = getNodeByName(message.text);

      player.moveToNode(node);

      if (node.shipAccess)
      {
        ship.moveToNode(node);
      }

      feed.clear();
      feed.publish("you reach " + node.getDescription());
    }
    else if (message.id.equals("teleport to"))
    {
      for (int i = 0; i < _sectorList.size(); i++)
      {
        Node teleportNode = _sectorList.get(i).getNode(message.text);

        if (teleportNode != null)
        {
          player.currentSector.removeActor(player);
          _sectorList.get(i).addActor(player, teleportNode);
          gameManager.loadSector(_sectorList.get(i));

          feed.clear();
          feed.publish("you are teleported to " + teleportNode.getDescription());
        }
      }
    }
    else if (message.id.equals("quantum moon vanished"))
    {
      player.currentSector.removeActor(player);
      ship.currentSector.removeActor(ship);
      gameManager.loadSolarSystemMap();
    }
  }

  boolean isPlayerInShip()
  {
    return(player.currentNode == ship.currentNode);
  }

  Node getNodeByName(String nodeName)
  {
    for (int i = 0; i < _sectorList.size(); i++)
    {
      Node node = _sectorList.get(i).getNode(nodeName);

      if (node != null)
      {
        return node;
      }
    }
    return null;
  }
  
  Sector getSectorByName(SectorName sectorName)
  {    
    switch(sectorName)
    {
      case COMET:
        return comet;
      case ROCKY_TWIN:
        return rockyTwin;
      case SANDY_TWIN:
        return sandyTwin;
      case TIMBER_HEARTH:
        return timberHearth;
      case BRITTLE_HOLLOW:
        return brittleHollow;
      case GIANTS_DEEP:
        return giantsDeep;
      case DARK_BRAMBLE:
        return darkBramble;
      case QUANTUM_MOON:
        return quantumMoon;
      case EYE_OF_THE_UNIVERSE:
        return eyeOfTheUniverse;
      default:
        break;
    }
    return null;
  }
}