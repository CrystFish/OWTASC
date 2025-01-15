class NodeConnection
{
  Node node1;
  Node node2;

  String _description;
  boolean _hasDescription = false;

  boolean _adjacentToPlayer = false;
  
  boolean _traversed = false;
  boolean _visible = false;
  boolean _gated = false;
  boolean _oneWay = false;

  boolean _hidden = false;

  JSONObject _connectionObj;
  
  NodeConnection(Node n1, Node n2, JSONObject connectionObj)
  {
    node1 = n1;
    node2 = n2;

    _connectionObj = connectionObj;
    
    node1.addConnection(this);
    node2.addConnection(this);

    _oneWay = connectionObj.getBoolean("one-way", _oneWay);
    _hidden = connectionObj.getBoolean("hidden", _hidden);
    _gated = connectionObj.getBoolean("gated", _gated);

    if (EDIT_MODE) 
    {
      _visible = true;
    }

    if (connectionObj.hasKey("description"))
    {
      _hasDescription = true;
      _description = connectionObj.getString("description");
    }
  }

  void updateAdjacentToPlayer(Node playerNode)
  {
    _adjacentToPlayer = false;

    if (node1 == playerNode || node2 == playerNode)
    {
      _adjacentToPlayer = true;
    }
  }

  boolean hasDescription()
  {
    return _hasDescription;
  }

  String getDescription()
  {
    return _description;
  }

  String getWrongWayText()
  {
    return "looks like this path is only traversible from the other direction";
  }

  Node getOtherNode(Node node)
  {
    if (node == node1)
    {
      return node2;
    }

    return node1;
  }
  
  boolean traversibleFrom(Node startingNode)
  {
    return (!_gated && (!_oneWay || startingNode == node1));
  }

  void fireTraverseEvent()
  {
    if (_connectionObj.hasKey("traverse event"))
    {
      messenger.sendMessage(_connectionObj.getString("traverse event"));
    }
  }

  void fireFailEvent()
  {
    if (_connectionObj.hasKey("fail event"))
    {
      messenger.sendMessage(_connectionObj.getString("fail event"));
    }
  }

  void traverse()
  {
    //_oneWay = false;
    _traversed = true;
  }

  void revealHidden()
  {
    _hidden = false;
    reveal();
  }

  void setVisible(boolean visible) {_visible = visible;}
  
  void reveal()
  {
    if (_hidden)
    {
      return;
    }

    node1.setVisible(true);
    node2.setVisible(true);
    _visible = true;
  }

  float getAlpha()
  {
    if (!_adjacentToPlayer)
    {
      return 35;
    }
    return 100;
  }
  
  void render()
  {
      if (!_visible) {return;}

      Vector2 dir = (node2.screenPosition.sub(node1.screenPosition));
      float dist = dir.magnitude();
      dir.normalize();

      // draw segmented line
      if (!_traversed)
      {
        stroke(0, 0, 100, getAlpha());
        //stroke(200, 100, 100);

        float l = 0;
        float segmentLength = 5;

        while(l < dist)
        {
          Vector2 startPos = node1.screenPosition.add(dir.mult(l));
          Vector2 endPos = node1.screenPosition.add(dir.mult(l+segmentLength));
          line(startPos.x, startPos.y, endPos.x, endPos.y);
          l += segmentLength * 3;
        }
      }
      // draw solid line
      else
      {
        stroke(0, 0, 100, getAlpha());
        line(node1.screenPosition.x, node1.screenPosition.y, node2.screenPosition.x, node2.screenPosition.y);
      }
      
      if (!_oneWay) {return;}
      
      // draw arrow
      Vector2 tip = node1.screenPosition.add(dir.mult(dist * 0.6f));
      Vector2 base = tip.sub(dir.mult(14));
      
      Vector2 right = base.add(dir.rightNormal().scale(7));
      Vector2 left = base.add(dir.leftNormal().scale(7));
      
      fill(0, 0, 0);
      triangle(right.x, right.y, left.x, left.y, tip.x, tip.y);
  }
}