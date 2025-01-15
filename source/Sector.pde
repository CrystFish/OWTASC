import java.util.Iterator;

class Sector implements NodeObserver, GlobalObserver
{
  Entity _sectorRoot;

  ArrayList<Actor> _actors;
  ArrayList<Node> _nodes;
  ArrayList<NodeConnection> _nodeConnections;
  
  String _name = "unnamed";
  String _JSONFilename;
  JSONObject _sectorJSON;

  boolean _skipArrivalScreen = false;
  
  Node orbitNode;
  Vector2 defaultActorPosition = new Vector2(width - 100, height - 100);
      
  Sector()
  {
    _sectorRoot = new Entity(width/2, height/2);
    _actors = new ArrayList<Actor>();
    _nodes = new ArrayList<Node>();
    _nodeConnections = new ArrayList<NodeConnection>();

    orbitNode = new Node(80, 170); // screen space
    orbitNode.gravity = false;

    messenger.addObserver(this);
  }

  void setAnchorOffset(float offsetX, float offsetY)
  {
    _sectorRoot.setPosition(width/2 + offsetX, height/2 + offsetY);
  }
  
  void loadFromJSON(String filename)
  {
    _JSONFilename = filename;
    _sectorJSON = loadJSONObject(filename);
    
    /** LOAD NODES **/
    JSONObject nodes = _sectorJSON.getJSONObject("Nodes");
    Iterator<String> iter = nodes.keyIterator();
    
    while(iter.hasNext())
    {
      String nodeName = iter.next();
      addNode(createNode(nodeName, nodes.getJSONObject(nodeName)));
    }
    
    /** LOAD CONNECTIONS **/
    JSONArray connectionJSONArray = _sectorJSON.getJSONArray("Connections");
    
    for(int i = 0; i < connectionJSONArray.size(); i++)
    {
      JSONObject connection = connectionJSONArray.getJSONObject(i);
      
      Node node1 = getNode(connection.getString("Node 1"));
      Node node2 = getNode(connection.getString("Node 2"));

      _nodeConnections.add(new NodeConnection(node1, node2, connection));
    }
  }

  Node createNode(String name, JSONObject nodeObj)
  {
    return new Node(name, nodeObj);
  }

  void saveSectorJSON()
  {
    saveJSONObject(_sectorJSON, "data/" + _JSONFilename);
    println("Sector JSON saved");
  }
  
  void load()
  {
    // stub to override
  }
  
  void drawSectorBackdrop()
  {
    // stub to override
  }

  void onReceiveGlobalMessage(Message message)
  {
    if (message.id.equals("quantum entanglement"))
    {
      onQuantumEntanglement();
    }
  }

  void onNodeVisited(Node node)
  {
    updateNodeRanges(gameManager._solarSystem.isPlayerInShip(), node);
  }

  void onArrival()
  {
    if (!_skipArrivalScreen && _sectorJSON.hasKey("Sector Arrival"))
    {
      _skipArrivalScreen = true;
      gameManager.pushScreen(new SectorArrivalScreen(_sectorJSON.getJSONObject("Sector Arrival").getString("text"), getName()));
    }
  }

  void onQuantumEntanglement()
  {
    if (locator.player.currentSector == this)
    {
      gameManager.pushScreen(new QuantumEntanglementScreen());

      // teleport player
      for (int i = 0; i < _nodes.size(); i++)
      {
        if (_nodes.get(i).allowQuantumEntanglement() && _nodes.get(i) != locator.player.currentNode)
        {
          locator.player.setNode(_nodes.get(i));
          break;
        }
      }
    }
  }

  boolean canClueBeInvoked(Clue clue)
  {
    return false;
  }

  // called from SectorScreen
  void invokeClue(Clue clue)
  {
    // override in derrived class
  }

  // update whether each node is "in range" (i.e. selectable)
  void updateNodeRanges(boolean isPlayerInShip, Node playerNode)
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).updateInRange(isPlayerInShip, playerNode);
    }

    for (int i = 0; i < _nodeConnections.size(); i++)
    {
       _nodeConnections.get(i).updateAdjacentToPlayer(playerNode);
    }
  }

  boolean allowTelescope() {return true;}

  String getName()
  {
    return _name;
  }
  
  Node getNode(int index)
  {
    if (_nodes.get(index) != null)
    {
      return _nodes.get(index);
    }
    println("No nodes at index " + index);
    return null;
  }
  
  Node getNode(String nodeID)
  {
    for(int i = 0; i < _nodes.size(); i++)
    {
      if (_nodes.get(i).getID().equals(nodeID))
      {
        return _nodes.get(i);
      }
    }
    return null;
  }

  ArrayList<SignalSource> getSectorSignalSources()
  {
    ArrayList<SignalSource> signalSources = new ArrayList<SignalSource>();

    for (int i = 0; i < _nodes.size(); i++)
    {
      if (_nodes.get(i).getSignal() != null)
      {
        signalSources.add(new SignalSource(_nodes.get(i)));
      }
    }
    return signalSources;
  }

  ArrayList<Signal> getSectorSignals()
  {
    ArrayList<Signal> nodeSignals = new ArrayList<Signal>();

    for (int i = 0; i < _nodes.size(); i++)
    {
      if (_nodes.get(i).getSignal() != null)
      {
        nodeSignals.add(_nodes.get(i).getSignal());
      }
    }
    return nodeSignals;
  }
  
  void addNodeButtonObserver(NodeButtonObserver observer)
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).addObserver(observer);
    }
  }
  
  void removeAllNodeButtonObservers()
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).removeAllObservers();
    }
  }
  
  void update()
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).update();
    }
    
    for (int i = 0; i < _actors.size(); i++)
    {
      _actors.get(i).update();

      if (_actors.get(i).isDead())
      {
        removeActor(_actors.get(i));
      }
    }
  }

  void renderBackground()
  {
    pushMatrix();
      translate(_sectorRoot.position.x, _sectorRoot.position.y);
      drawSectorBackdrop();
    popMatrix();

    // draw letterbox
    fill(0, 0, 0);
    noStroke();
    rectMode(CORNER);
    rect(0, 0, width, 90);
    rect(0, height - 90, width, 90);
    rectMode(CENTER);
  }
  
  void render()
  {
    for (int i = 0; i < _nodeConnections.size(); i++)
    {
      _nodeConnections.get(i).render();
    }
    
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).render();
    }
    
    for (int i = 0; i < _actors.size(); i++)
    {
      _actors.get(i).render();
    }

    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).drawName();
    }
  }
  
  void addNode(Node node)
  {
    if (!_nodes.contains(node))
    {
      _nodes.add(node);
      node.setNodeObserver(this);
      _sectorRoot.addChild(node);
    }
    else
    {
      println("Node is already in this Sector!!!");
    }
  }
  
  void removeNode(Node node)
  {
    if (_nodes.contains(node))
    {
      _nodes.remove(node);
      node.setNodeObserver(null);
      _sectorRoot.removeChild(node);
    }
    else
    {
      println("Node is not in this Sector!!!");
    }
  }
  
  void addActor(Actor actor)
  {
    addActor(actor, (Node)null);
  }
  
  void addActor(Actor actor, String nodeName)
  {
    addActor(actor, getNode(nodeName));
  }
  
  void addActor(Actor actor, Node atNode)
  {
    if (!_actors.contains(actor))
    {
      actor.currentSector = this;

      _actors.add(actor);
      _sectorRoot.addChild(actor);
      
      if (atNode != null)
      {
        actor.setNode(atNode);
      }
      else
      {
        // spawn actor off-screen
        actor.setScreenPosition(orbitNode.screenPosition.sub(new Vector2(200, 0)));
        actor.moveToNode(orbitNode);
      }
    }
    else
    {
      println("Actor is already in this Sector!!!");
    }
  }
  
  void removeActor(Actor actor)
  {
    if (_actors.contains(actor))
    {
      actor.currentNode = null;
      actor.currentSector = null;
      actor.lastSector = this;
      _actors.remove(actor);
      _sectorRoot.removeChild(actor);
    }
    else
    {
      println("Actor is not in this Sector!!!");
    }
  }
}