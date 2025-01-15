interface NodeButtonObserver
{
  void onNodeSelected(Node node);
  void onNodeGainFocus(Node node);
  void onNodeLoseFocus(Node node);
}

interface NodeObserver
{
  void onNodeVisited(Node node);
}

class Node extends Entity implements ButtonObserver, GlobalObserver
{
  /** NODE DATA **/
  String _id = "";
  String _name = "";
  
  boolean entryPoint = false;
  boolean shipAccess = false;
  boolean allowTelescope = true;
  boolean gravity = true;

  Signal _signal;
  
  /** EXPLORE STATE **/
  boolean _visible = false;
  boolean _visited = false;
  boolean _explored = false;

  boolean _inRange = false;

  Button _button;
  HashMap<Node, NodeConnection> _connections;

  ArrayList<NodeButtonObserver> _observers;
  NodeObserver _observer;
  
  JSONObject _nodeJSONObj;
  ExploreData _exploreData;

  Node(float x, float y)
  {
    super(x, y);
    _connections = new HashMap<Node, NodeConnection>();
    _observers = new ArrayList<NodeButtonObserver>();
    messenger.addObserver(this);
  }
  
  Node(String id, JSONObject nodeJSONObj)
  {
    this(0, 0);
    
    _id = id;
    _name = id;

    loadJSON(nodeJSONObj);

    if (entryPoint) 
    {
      _visible = true;
    }
    
    _button = new Button(_id, 0, 0, getSize() * 1.5f, getSize() * 1.5f);
    _button.setObserver(this);
    _button.visible = false;
    addChild(_button);
  }

  void loadJSON(JSONObject nodeJSONObj)
  {
    _nodeJSONObj = nodeJSONObj;

    _name = _nodeJSONObj.getString("name", _id);

    if (nodeJSONObj.hasKey("explore"))
    {
      _exploreData = new ExploreData(this, nodeJSONObj);
    }
    
    position.x = _nodeJSONObj.getJSONObject("position").getFloat("x");
    position.y = _nodeJSONObj.getJSONObject("position").getFloat("y");

    _visible = _nodeJSONObj.getBoolean("start visible", _visible);

    if (EDIT_MODE)
    {
      _visible = true;
    }
    
    entryPoint = _nodeJSONObj.getBoolean("entry point", entryPoint);
    shipAccess = entryPoint || _nodeJSONObj.getBoolean("ship access", shipAccess);
    allowTelescope = _nodeJSONObj.getBoolean("allow telescope", allowTelescope);
    gravity = _nodeJSONObj.getBoolean("gravity", gravity);
    
    if (_nodeJSONObj.hasKey("signal"))
    {
      _signal = new Signal(_nodeJSONObj.getString("signal"));
    }
  }

  void savePosition()
  {
    _nodeJSONObj.getJSONObject("position").setFloat("x", position.x);
    _nodeJSONObj.getJSONObject("position").setFloat("y", position.y);
    println(_id + " position saved: " + position);
  }

  void onReceiveGlobalMessage(Message message)
  {

  }

  boolean isExplorable() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("explore"));}

  ExploreData getExploreData() {return _exploreData;}

  String getProbeDescription() 
  {
    if (_nodeJSONObj.hasKey("probe description"))
    {
      return _nodeJSONObj.getString("probe description");
    }
    return getDescription();
  }

  String getDescription() {return _nodeJSONObj.getString("description", "a vast expanse of nothing");}

  boolean hasDescription() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("description"));}

  boolean isProbeable() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("description"));}

  boolean isConnectedTo(Node node) {return _connections.containsKey(node);}

  boolean inRange()
  {
    return _inRange;
  }

  void updateInRange(boolean isPlayerInShip, Node playerNode) 
  {
    _inRange = false;

    if (playerNode == this)
    {
      _inRange = true;
    }

    if (entryPoint && isPlayerInShip)
    {
      _inRange = true;
    }
    
    if (playerNode != null && this.isConnectedTo(playerNode))
    {
      _inRange = true;
    }
  }
  
  NodeConnection getConnection(Node node) {return _connections.get(node);}

  NodeConnection getConnection(String nodeID)
  {
    for (Node node : _connections.keySet()) 
    {
      if (node.getID().equals(nodeID))
      {
        return getConnection(node);
      }
    }
    return null;
  }

  boolean allowQuantumEntanglement() // note - "quantum state" only used for Quantum Moon right now
  {
    if (_nodeJSONObj == null) return false;
    return _nodeJSONObj.getBoolean("entanglement node", false);
  }

  Signal getSignal() {return _signal;}

  String getID() {return _id;}

  String getActualName() {return _name;}
  
  String getKnownName()
  {
    if (_visited) return getActualName();
    else return "???";
  } 

  void setVisible(boolean visible) {_visible = visible;}
  
  void visit()
  {
    _visited = true;
    setVisible(true);

    if (_nodeJSONObj != null && _nodeJSONObj.hasKey("fire event"))
    {
      messenger.sendMessage(_nodeJSONObj.getString("fire event"));
    }

    for (NodeConnection connection : _connections.values()) 
    {
      connection.reveal();
    }

    if (_observer != null)
    {
      _observer.onNodeVisited(this);
    }
  }

  void explore()
  {
    _explored = true;
    _exploreData.explore();

    if (_signal != null)
    {
      playerData.learnFrequency(_signal.frequency);
    }
  }
  
  void update()
  {
    if (!_visible) {return;}
    _button.enabled = inRange() || EDIT_MODE;
    _button.update();
  }

  float getAlpha()
  {
    if (!inRange())
    {
      return 35;
    }
    return 100;
  }

  float getSize()
  {
    if (entryPoint) 
    {
      return 50;
    }
    else if (!isExplorable())
    {
      return 25;
    }

    return 35;
  }
  
  void draw()
  {
    if (!_visible) {return;}

    if (_button.hoverState)
    {
      stroke(200, 100, 100, getAlpha());
    }
    else
    {
      stroke(0, 0, 100, getAlpha());
    }

    pushMatrix();
    translate(screenPosition.x, screenPosition.y);

      if (!isExplorable())
      {
        fill(0, 0, 0);
        ellipse(0, 0, getSize(), getSize());
        popMatrix();
        return;
      }
          
      if (entryPoint)
      {
        fill(0, 0, 0);
        ellipse(0, 0, getSize(), getSize());

        // float halfWidth = getSize() * 0.5;
        // float halfHeight = getSize() * 0.5f;
        // float offset = 7;

        // pushMatrix();
        // rotate(PI * 0.25f);
        //   line(-halfWidth, 0, -halfWidth + offset, 0);
        //   line(halfWidth - offset, 0, halfWidth, 0);
        //   line(0, -halfHeight, 0, -halfHeight + offset);
        //   line(0, halfHeight - offset, 0, halfHeight);
        // popMatrix();
      }
      else
      {
        fill(0, 0, 0);
        rect(0, 0, getSize(), getSize());
      }

      if (!_explored)
      {
        fill(0, 0, 100, getAlpha());
        textAlign(CENTER, CENTER);
        textSize(30);
        text('?', 0, 0);
      }

    popMatrix();
  }

  void drawName()
  {
    if (!_visible) {return;}
    if (!isExplorable()) {return;}

    // draw text backdrop
    Vector2 textPos = new Vector2(screenPosition.x, screenPosition.y - getSize() / 2 - 20);
    
    noStroke();
    fill(0, 0, 0);
    textSize(TEXT_SIZE);
    rect(textPos.x, textPos.y, textWidth(getKnownName()), TEXT_SIZE + 4);
    
    fill(0, 0, 100, getAlpha());
    
    textAlign(CENTER, CENTER);
    text(getKnownName(), textPos.x, textPos.y);
  }

  void addConnection(NodeConnection connection)
  {
    if (_connections.containsValue(connection))
    {
      println("These nodes are already connected!!!");
      return;
    }
        
    if (connection.node1 != this)
    {
      _connections.put(connection.node1, connection);
    }
    else
    {
      _connections.put(connection.node2, connection);
    }
  }

  void setNodeObserver(NodeObserver observer)
  {
    _observer = observer;
  }

  void addObserver(NodeButtonObserver observer)
  {
    _observers.add(observer);
  }
  
  void removeAllObservers()
  {
    _observers.clear();
  }
  
  void onButtonUp(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeSelected(this);
    }
  }
  
  void onButtonEnterHover(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeGainFocus(this);
    }
  }
  
  void onButtonExitHover(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeLoseFocus(this);
    }
  }
}