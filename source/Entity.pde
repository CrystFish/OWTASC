
class Entity
{
  Vector2 position;
  Vector2 screenPosition;

  Entity parent;
  ArrayList<Entity> _children;
  
  Entity(Vector2 pos)
  {
    position = new Vector2();
    screenPosition = new Vector2();
    _children = new ArrayList<Entity>();

    setPosition(pos);
  }
  
  Entity(float x, float y)
  {
    this(new Vector2(x, y));
  }
  
  Entity()
  {
    this(0, 0);
  }

  void setPosition(Vector2 newPos)
  {
    setPosition(newPos.x, newPos.y);
  }

  void setPosition(float x, float y)
  {
    position.x = x;
    position.y = y;

    // update child screen positions
    if (parent != null)
    {
      updateScreenPosition(parent.screenPosition);
    }
    else 
    {
      updateScreenPosition(new Vector2(0, 0));
    }
  }

  void updateScreenPosition(Vector2 parentScreenPos)
  {
    screenPosition.assign(parentScreenPos.add(position));

    for (int i = 0; i < _children.size(); i++)
    {
      _children.get(i).updateScreenPosition(screenPosition);
    }
  }

  void setScreenPosition(Vector2 newScreenPos)
  {
    if (parent == null)
    {
      setPosition(newScreenPos);
    }
    else 
    {
      setPosition(newScreenPos.sub(parent.screenPosition));
    }
  }

  void draw()
  {
    // stub to override
  }

  void render()
  {
    draw();
  }

  void addChild(Entity child)
  {
    if (!_children.contains(child))
    {
      _children.add(child);
      child.parent = this;
      child.updateScreenPosition(screenPosition);
    }
  }

  void removeChild(Entity child)
  {
    _children.remove(child);
    child.parent = null;
  }
}

class Actor extends Entity
{
  Sector currentSector;
  Sector lastSector;
  Node currentNode;

  final float SPEED = 10;
  
  boolean _moveTowardsTarget = false;
  Vector2 _targetScreenPos;
  float _distToTarget;
  Vector2 _offset;
  
  Actor()
  {
    super(new Vector2(0, 0));
    _targetScreenPos = new Vector2();
    _offset = new Vector2(0, 0);
  }

  boolean isDead()
  {
    return false;
  }
  
  void update()
  {
    _offset.y = 0;
    
    if (currentNode == null || !currentNode.gravity)
    {
      _offset.y = sin(millis() * 0.005f) * 5.0f;
    }

    if (_moveTowardsTarget)
    {
      Vector2 d = _targetScreenPos.sub(screenPosition);
      _distToTarget = d.magnitude();
      float v = min(_distToTarget, SPEED);
      setScreenPosition(screenPosition.add(d.normalize().mult(v)));
    }
  }
  
  void draw()
  {
    fill(0, 0, 100);
    ellipse(screenPosition.x, screenPosition.y, 10, 10);
  }

  void setNode(Node node)
  {
    currentNode = node;
    _targetScreenPos.assign(node.screenPosition);
    setScreenPosition(node.screenPosition);
  }

  void moveToScreenPosition(Vector2 screenPos)
  {
    _targetScreenPos.assign(screenPos);
    _moveTowardsTarget = true;
  }
  
  void moveToNode(Node node)
  {
    currentNode = node;
    _targetScreenPos.assign(node.screenPosition);
    _moveTowardsTarget = true;
  }
}

class Player extends Actor
{
  void setNode(Node node)
  {
    super.setNode(node);
    node.visit();
  }

  void moveToNode(Node node)
  {
    super.moveToNode(node);
    node.visit();
  }

  void update()
  {
    super.update();
    //println(_targetScreenPos);
  }
  
  void draw()
  {
    drawAt(screenPosition.x, screenPosition.y + _offset.y, 1);
  }

  void drawAt(float xPos, float yPos, float s)
  {
    stroke(30, 100, 100);
    fill(0, 0, 0);

    pushMatrix();
      translate(xPos, yPos);
      scale(s);
      ellipse(0, 0, 10, 20);
    popMatrix();
  }
}

class Ship extends Actor
{
  Actor _player;

  Ship(Actor player)
  {
    super();
    _player = player;
  }

  void update()
  {
    super.update();
  }

  void draw()
  {
    drawAt(screenPosition.x, screenPosition.y + _offset.y, 1, false);
  }

  void drawAt(float xPos, float yPos, float s, boolean skipFill)
  {
    stroke(30, 100, 100);
    fill(0, 0, 0);

    if (_player.currentNode == currentNode && !skipFill) 
    {
      fill(30,100,100);
    }
    
    pushMatrix();
      translate(xPos, yPos);
      scale(s);
      triangle(-20, 15, 20, 15, 0, -20);
    popMatrix();
  }
}

class Probe extends Actor
{
  boolean isDead()
  {
    return _distToTarget < 0.1f;
  }

  void update()
  {
    super.update();
  }

  void draw()
  {
    noStroke();
    fill(30, 100, 100);
    
    pushMatrix();
      translate(screenPosition.x, screenPosition.y);
      ellipse(0, 0, 10, 10);
    popMatrix();
  }
}