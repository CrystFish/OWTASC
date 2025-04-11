import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Map; 
import ddf.minim.spi.*; 
import ddf.minim.signals.*; 
import ddf.minim.*; 
import ddf.minim.analysis.*; 
import ddf.minim.ugens.*; 
import ddf.minim.effects.*; 
import java.util.Iterator; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class OuterWilds_TextAdventure extends PApplet {



/*** GLOBALS ***/
final float TEXT_SIZE = 14;

Locator locator;
GameManager gameManager;
TimeLoop timeLoop;
GlobalMessenger messenger;
StatusFeed feed;
PlayerData playerData;

static Minim minim;
static AudioManager audioManager;

/*** DEBUG ***/
final boolean EDIT_MODE = false;
final boolean SKIP_TITLE = false;
final boolean START_WITH_LAUNCH_CODES = false;
final boolean START_WITH_CLUES = false;
final boolean START_WITH_SIGNALS = false;
final boolean START_WITH_COORDINATES = false;

float lastMillis;
float deltaMillis;

PFont smallFont;
PFont mediumFont;
PFont largeFont;
PFont titleFont;

public void setup()
{
  
  
  colorMode(HSB, 360, 100, 100, 100);
  rectMode(CENTER);
  ellipseMode(CENTER);

  smallFont = loadFont("fonts/14.vlw");
  mediumFont = loadFont("fonts/18.vlw");
  largeFont = loadFont("fonts/22.vlw");
  titleFont = loadFont("fonts/PixelMplus10-Regular-100.vlw");
  
  smallFont();
    
  minim = new Minim(this);

  initGame();
}

public void initGame()
{
  float startLoadTime = millis();
  
  timeLoop = new TimeLoop();
  audioManager = new AudioManager();
  messenger = new GlobalMessenger();
  feed = new StatusFeed();
  gameManager = new GameManager();
  playerData = new PlayerData();
  
  gameManager.newGame();
  
  println("Load time: " + (millis() - startLoadTime));
}

public void draw()
{
  gameManager.runGameLoop();

  // stroke(0, 0, 100);
  // fill(0, 0, 0);
  // triangle(mouseX, mouseY, mouseX, mouseY - 10, mouseX + 10, mouseY - 10);
  
  deltaMillis = millis() - lastMillis;
  lastMillis = millis();
  
  //println(deltaMillis);
}

public String frequencyToString(Frequency frequency)
{
  switch(frequency)
  {
    case TRAVELER:
      return "旅行者频率";
    case BEACON:
      return "求救信标";
    case QUANTUM:
      return "量子波动";
    default:
      return "";
  }
}

public void smallFont()
{
  textSize(14);
  textFont(smallFont);
}

public void mediumFont()
{
  textSize(18);
  textFont(mediumFont);
}
class AnglerfishNode extends Node
{
	AnglerfishNode(String nodeName, JSONObject nodeJSONObj)
	{
		super(nodeName, nodeJSONObj);
		entryPoint = true;
		shipAccess = true;
		gravity = false;

		_visible = true;
	}

	public String getKnownName()
	{
		if (_visited) return "鮟鱇鱼";
		else return "？？？";
	} 

	public String getDescription() 
	{
		return "一条巨大而饥饿的鮟鱇鱼";
	}

	public String getProbeDescription() 
	{
		return "一束光穿过雾气";
	}

	public boolean hasDescription() {return true;}

	public boolean isProbeable() {return true;}

	public boolean isExplorable() {return true;} // tricks graphics into rendering question mark

	public void visit()
	{
		_visited = true;
		setVisible(true);

		messenger.sendMessage("death by anglerfish");

		if (_observer != null)
		{
			_observer.onNodeVisited(this);
		}
	}
}







static class SoundLibrary
{
  static AudioPlayer kazooTheme;
  
  public static void loadSounds()
  {
    println("正在加载音频...");
    kazooTheme = minim.loadFile("audio/ow_kazoo_theme.mp3");
  }
}

static class AudioManager
{
  static AudioPlayer currentSound;
  
  AudioManager()
  {
    SoundLibrary.loadSounds();
  }
  
  public static void play(AudioPlayer sound)
  {
    currentSound = sound;
    currentSound.play();
  }
  
  public static void pause()
  {
    if (currentSound != null)
    {
      currentSound.pause();
    }
    else
    {
      println("Current sound is NULL!!!");
    }
  }
}

interface ButtonObserver
{
  public void onButtonUp(Button button);
  public void onButtonEnterHover(Button button);
  public void onButtonExitHover(Button button);
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

  int _buttonColor = color(0, 0, 100);
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

  public void setColor(int newColor) {_buttonColor = newColor;}
  public void setDisabledPrompt(String prompt) 
  {
    _disabledPrompt = prompt;
    _hasDisabledPrompt = true;
  }

  public float getWidth()
  {
    return _bounds.x;
  }
  
  public void setObserver(ButtonObserver observer)
  {
    _observer = observer;
  }
  
  public void update()
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

  public void onButtonExitHover(){}
  public void onButtonEnterHover(){}
  public void onButtonUp(){}

  public boolean mousePressed()
  {
    return mousePressed;// && mouseButton == LEFT;
  }
  
  public void draw()
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

  public void drawShape()
  {
    rectMode(CENTER);
    rect(screenPosition.x, screenPosition.y, _bounds.x, _bounds.y);
  }

  public void drawText(float alpha)
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
  
  public boolean isPointInBounds(float x, float y)
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
interface DatabaseObserver
{
  public void onInvokeClue(Clue clue);
}

class DatabaseScreen extends Screen implements ClueButtonObserver
{
  Entity _clueRoot;
  Clue _activeClue;
  DatabaseObserver _observer;

  DatabaseScreen()
  {
    super();
    addButtonToToolbar(new Button("关闭数据库",  0, 0, 150, 50));
    _clueRoot = new Entity(100, 140);

    // create clue buttons using PlayerData's list of clues
    for (int i = 0; i < playerData.getClueCount(); i++)
    {
      ClueButton clueButton = new ClueButton(playerData.getClueAt(i), i * 40, this);
      addButton(clueButton);
      _clueRoot.addChild(clueButton);
    }
  }

  public void setObserver(DatabaseObserver observer)
  {
    _observer = observer;
  }

  public void onEnter()
  {
  }

  public void onExit()
  {
    _observer = null;
  }

  public void onClueMouseOver(Clue clue)
  {
    _activeClue = clue;
  }

  public void onClueSelected(Clue clue)
  {
    if (_observer != null)
    {
      _observer.onInvokeClue(clue);
    }
    else
    {
      feed.publish("那个现在还不能帮助到你", true);
    }
  }

  public void onButtonUp(Button button)
  {
    if (button.id == "关闭数据库")
    {
      gameManager.popScreen();
    }
  }

  public void update() {}

  public void render()
  {
    fill(0, 0, 0);
    stroke(0, 0, 100);

    rectMode(CORNER);

    float x = width/2 - 100;
    float y = 200;
    float w = 500;
    float h = 300;

    rect(x, y, w, h);

    String _displayText = "选择一项条目...";

    if (_activeClue != null)
    {
      _displayText = _activeClue.description;
    }
    else if (playerData.getKnownClueCount() == 0)
    {
      _displayText = "暂无条目";
    }

    textFont(mediumFont);
    textSize(18);
    textAlign(LEFT, TOP);
    fill(0, 0, 100);
    text(_displayText, x + 10, y + 10, w - 20, h - 20);

    feed.render();
  }
}

interface ClueButtonObserver
{
  public void onClueSelected(Clue clue);
  public void onClueMouseOver(Clue clue);
}

class ClueButton extends Button
{
  Clue _clue;
  ClueButtonObserver _observer;

  ClueButton(Clue clue, float y, ClueButtonObserver observer)
  {
    super(clue.name, (textWidth(clue.name) + 10) * 0.5f, y, textWidth(clue.name) + 10, 20);
    _clue = clue;
    _observer = observer;
  }

  public Clue getClue()
  {
    return _clue;
  }

  public void update()
  {
    enabled = _clue.discovered;
    visible = _clue.discovered;
    super.update();
  }

  public void draw()
  {
    if (!visible) {return;}
    
    super.draw();

    int symbolColor;

    if (_clue.curiosity == Curiosity.VESSEL)
    {
        symbolColor = color(100, 100, 100);
    }
    else if (_clue.curiosity == Curiosity.ANCIENT_PROBE_LAUNCHER)
    {
      symbolColor = color(200, 100, 100);
    }
    else if (_clue.curiosity == Curiosity.TIME_LOOP_DEVICE)
    {
      symbolColor = color(20, 100, 100);
    }
    else
    {
      symbolColor = color(300, 100, 100);
    }

    fill(symbolColor);
    noStroke();
    ellipse(screenPosition.x - _bounds.x * 0.5f - 20, screenPosition.y, 10, 10);

    noFill();
    stroke(symbolColor);
    ellipse(screenPosition.x - _bounds.x * 0.5f - 20, screenPosition.y, 15, 15);
  }

  public void onButtonEnterHover()
  {
    _observer.onClueMouseOver(_clue);
  }

  public void onButtonUp()
  {
    _observer.onClueSelected(_clue);
  }
}

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

  public void setPosition(Vector2 newPos)
  {
    setPosition(newPos.x, newPos.y);
  }

  public void setPosition(float x, float y)
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

  public void updateScreenPosition(Vector2 parentScreenPos)
  {
    screenPosition.assign(parentScreenPos.add(position));

    for (int i = 0; i < _children.size(); i++)
    {
      _children.get(i).updateScreenPosition(screenPosition);
    }
  }

  public void setScreenPosition(Vector2 newScreenPos)
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

  public void draw()
  {
    // stub to override
  }

  public void render()
  {
    draw();
  }

  public void addChild(Entity child)
  {
    if (!_children.contains(child))
    {
      _children.add(child);
      child.parent = this;
      child.updateScreenPosition(screenPosition);
    }
  }

  public void removeChild(Entity child)
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

  public boolean isDead()
  {
    return false;
  }
  
  public void update()
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
  
  public void draw()
  {
    fill(0, 0, 100);
    ellipse(screenPosition.x, screenPosition.y, 10, 10);
  }

  public void setNode(Node node)
  {
    currentNode = node;
    _targetScreenPos.assign(node.screenPosition);
    setScreenPosition(node.screenPosition);
  }

  public void moveToScreenPosition(Vector2 screenPos)
  {
    _targetScreenPos.assign(screenPos);
    _moveTowardsTarget = true;
  }
  
  public void moveToNode(Node node)
  {
    currentNode = node;
    _targetScreenPos.assign(node.screenPosition);
    _moveTowardsTarget = true;
  }
}

class Player extends Actor
{
  public void setNode(Node node)
  {
    super.setNode(node);
    node.visit();
  }

  public void moveToNode(Node node)
  {
    super.moveToNode(node);
    node.visit();
  }

  public void update()
  {
    super.update();
    //println(_targetScreenPos);
  }
  
  public void draw()
  {
    drawAt(screenPosition.x, screenPosition.y + _offset.y, 1);
  }

  public void drawAt(float xPos, float yPos, float s)
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

  public void update()
  {
    super.update();
  }

  public void draw()
  {
    drawAt(screenPosition.x, screenPosition.y + _offset.y, 1, false);
  }

  public void drawAt(float xPos, float yPos, float s, boolean skipFill)
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
  public boolean isDead()
  {
    return _distToTarget < 0.1f;
  }

  public void update()
  {
    super.update();
  }

  public void draw()
  {
    noStroke();
    fill(30, 100, 100);
    
    pushMatrix();
      translate(screenPosition.x, screenPosition.y);
      ellipse(0, 0, 10, 10);
    popMatrix();
  }
}

abstract class EventScreen extends Screen implements DatabaseObserver
{
	final float BOX_WIDTH = 700;
	final float BOX_HEIGHT = 400;

	Button _nextButton;
	Button _databaseButton;

	EventScreen()
	{
		overlay = true; // continue to render BG
		initButtons();
	}

	public void initButtons()
	{
		addButtonToToolbar(_nextButton = new Button("继续", 0, 0, 150, 50));
	}

	public void addDatabaseButton()
	{
		addButtonToToolbar(_databaseButton = new Button("查看数据库", 0, 0, 150, 50));
	}

	public void addContinueButton()
	{
		addButtonToToolbar(_nextButton = new Button("继续", 0, 0, 150, 50));
	}

	public void update(){}

	public void renderBackground() {}

	public void render()
	{
		pushMatrix();
		translate(width/2 - BOX_WIDTH/2, height/2 - BOX_HEIGHT/2);

			stroke(0, 0, 100);
			fill(0, 0, 0);
			rectMode(CORNER);
			rect(0, 0, BOX_WIDTH, BOX_HEIGHT);

			fill(0, 0, 100);

		    textFont(mediumFont);
		    textSize(18);
			textAlign(LEFT, TOP);
			text(getDisplayText(), 10, 10, BOX_WIDTH - 20, BOX_HEIGHT - 20);

		popMatrix();

		feed.render();
		timeLoop.renderTimer();
	}

	public void onButtonUp(Button button)
	{
	    if (button == _nextButton)
	    {
	    	onContinue();
	    }
	    else if (button == _databaseButton)
	    {
			gameManager.pushScreen(gameManager.databaseScreen);
			gameManager.databaseScreen.setObserver(this);
	    }
	}

	public void onInvokeClue(Clue clue) {}

	public abstract String getDisplayText();

	public abstract void onContinue();

	public void onEnter(){}

	public void onExit(){}
}

class DeathByAnglerfishScreen extends EventScreen
{
	public void onEnter()
	{
		feed.clear();
		feed.publish("你被鮟鱇鱼吃掉了", true);
	}

	public String getDisplayText()
	{
		return "你在飞向闪耀的光芒时，突然意识到这实际上是巨型鮟鱇鱼的诱饵！\n\n你试图扭头就跑，但为时已晚 —— 鮟鱇鱼一口就把你吞没了\n\n世界变得一片漆黑...";
	}

	public void onContinue()
	{
		playerData.killPlayer();
	}
}

class DiveAttemptScreen extends EventScreen
{
	public void onEnter()
	{
		feed.clear();
		feed.publish("你尝试潜入水下", true);
	}

	public String getDisplayText()
	{
		return "你尝试潜入水下，但浅层强大的水流阻止你继续潜入几百米下的深水区";
	}

	public void onContinue()
	{
		gameManager.popScreen();
	}
}

class TeleportScreen extends EventScreen
{
	String _text;
	String _destination;

	TeleportScreen(String teleportText, String destination)
	{
		_text = teleportText;
		_destination = destination;
	}

	public void onExit()
	{
		feed.clear();
		feed.publish("你已被传送至新地点", true);
	}

	public String getDisplayText()
	{
		return _text;
	}

	public void onContinue()
	{
		gameManager.popScreen();
		messenger.sendMessage(new Message("teleport to", _destination));
	}
}

class MoveToScreen extends EventScreen
{
	String _text;
	String _destination;

	MoveToScreen(String moveText, String destination)
	{
		_text = moveText;
		_destination = destination;
	}

	public String getDisplayText()
	{
		return _text;
	}

	public void onContinue()
	{
		gameManager.popScreen();
		messenger.sendMessage(new Message("move to", _destination));
	}
}

class SectorArrivalScreen extends EventScreen
{
	String _arrivalText;
	String _sectorName;

	SectorArrivalScreen(String arrivalText, String sectorName)
	{
		_arrivalText = arrivalText;
		_sectorName = sectorName;
	}

	public void onEnter()
	{
		feed.clear();
		feed.publish("你已抵达" + _sectorName);
	}

	public String getDisplayText()
	{
		return _arrivalText;
	}

	public void onContinue()
	{
		gameManager.popScreen();
	}
}

class QuantumArrivalScreen extends EventScreen
{
	int _screenIndex = 0;
	boolean _photoTaken = false;

	public void initButtons()
	{
		addDatabaseButton();
		addContinueButton();
	}

	public String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			if (!_photoTaken)
			{
				return "你接近了量子卫星, 一团奇怪的迷雾开始遮挡你的视线...";
			}
			else
			{
				return "在它被逐渐逼近的迷雾完全遮挡之前，你使用小侦察兵拍摄了卫星的照片";
			}
		}
		else if (_screenIndex == 1)
		{
			if (!_photoTaken)
			{
				return "迷雾完全吞没了你的飞船，然后突然消散，就像它出现时那样\n\n你环顾四周，量子卫星已经神秘消失了";
			}
			else
			{
				return "你一头扎进雾中，确保自己盯着刚刚拍到的照片\n\n突然，巨大的形状从雾中浮现...你抵达了量子卫星的表面！";
			}
		}
		return "";
	}

	public void onInvokeClue(Clue clue)
	{
		if (clue.id.equals("QM_1"))
		{
			gameManager.popScreen();
			_photoTaken = true;
			_databaseButton.enabled = false;
		}
		else
		{
			feed.publish("那个现在还不能帮助到你", true);
		}
	}

	public void onContinue()
	{
		_screenIndex++;

		_databaseButton.enabled = false;

		if (_screenIndex > 1)
		{
			if (!_photoTaken)
			{
				gameManager.popScreen();
				messenger.sendMessage("quantum moon vanished");
			}
			else
			{
				gameManager.popScreen();
			}
		}
	}
}

class QuantumEntanglementScreen extends EventScreen
{
	String _displayText;

	QuantumEntanglementScreen()
	{
		if (locator.player.currentSector.getName().equals("Quantum Moon"))
		{
			_displayText = "你关闭了手电筒，世界变得一片漆黑\n\n当你重新打开手电筒时，四周的环境发生了变化...";
		}
		else
		{
			_displayText = "你爬上了量子碎片的顶部并关闭了手电筒。环境实在是太黑了，伸手不见五指\n\n当你重新打开手电筒时，你仍然站在量子碎片的顶部，但四周的环境发生了变化...";
		}
	}

	public void onEnter()
	{
		feed.clear();
		feed.publish("你与量子物体一块发生纠缠现象了");
	}

	public String getDisplayText()
	{
		return _displayText;
	}

	public void onContinue()
	{
		gameManager.popScreen();
	}
}

class FollowTheVineScreen extends EventScreen
{
	int _screenIndex = 0;
	boolean _silentRunning = false;

	public void initButtons()
	{
		addDatabaseButton();
		addContinueButton();
	}

	public String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "你向其中一朵怪异的蓝色花朵发射小侦察兵，它很快就被吞噬了。你跟随小侦察兵的追踪信号，顺着错综复杂的藤蔓进入黑荆星的中心深处\n\n你如此专注于跟随小侦察兵的信号，以至于你没有注意到发光的诱饵，但已为时已晚。你直接飞进了鮟鱇鱼的巢穴里！";
		}
		else if (_screenIndex == 1)
		{
			if (!_silentRunning)
			{
				return "你反转了飞船的推进器，但为时已晚。鮟鱇鱼飞速猛扑过来，吞噬了整个飞船";
			}
			else
			{
				return "你突然想起了那个古老儿童游戏的规则，你关掉了引擎，悄悄地漂进了巢穴\n\n似乎没有鮟鱇鱼注意到你，你安全地到达了另一边。你继续跟随小侦察兵的信号前进，没过多久，你到达了一个纠缠于黑荆根部的古老遗迹";
			}
		}
		return "";
	}

	public void onButtonUp(Button button)
	{
	    if (button == _nextButton)
	    {
	    	onContinue();
	    }
	    else if (button == _databaseButton)
	    {
			gameManager.pushScreen(gameManager.databaseScreen);
			gameManager.databaseScreen.setObserver(this);
	    }
	}

	public void onInvokeClue(Clue clue)
	{
		if (clue.id.equals("D_2"))
		{
			gameManager.popScreen();
			_silentRunning = true;
			_screenIndex++;
			_databaseButton.enabled = false;
		}
		else
		{
			feed.publish("那个现在还不能帮助到你", true);
		}
	}

	public void onContinue()
	{
		_screenIndex++;
		_databaseButton.enabled = false;

		if (_screenIndex > 1)
		{
			if (!_silentRunning)
			{
				gameManager.popScreen();
				playerData.killPlayer();
			}
			else
			{
				gameManager.popScreen();
				messenger.sendMessage(new Message("move to", "古飞船"));
			}
		}
	}
}

class AncientVesselScreen extends EventScreen
{
	Button _warpButton;
	String _displayText;

	AncientVesselScreen()
	{
		super();
		_displayText = "你探索了这片废墟，最终找到了这里。尽管飞船的生命维持系统已经失效，但一些计算机终端仍在使用某种辅助电源运行。你找到了挪麦人从宇宙之眼接收的原始信号的记录。根据它们的分析，无论信号来自什么物体，都比宇宙本身更为古老！\n\n你又四处探查了一番，发现这艘船的跃迁装置在几百年前就完成了充能";
	}

	public void initButtons()
	{
		addButtonToToolbar(_warpButton = new Button("使用跃迁装置", 0, 0, 150, 50));
		super.initButtons();
	}

	public String getDisplayText()
	{
		return _displayText;
	}

	public void onButtonUp(Button button)
	{
	    if (button == _warpButton)
	    {
	    	if (playerData.knowsSignalCoordinates())
	    	{
	    		if (!timeLoop.getEnabled())
	    		{
	    			gameManager.popScreen();
	    			messenger.sendMessage(new Message("teleport to", "Ancient Vessel 2"));
	    			feed.clear();
	    			feed.publish("古飞船跃迁到了宇宙之眼所在的坐标");
	    		}
	    		else
	    		{
	    			_displayText = "你输入了宇宙之眼的坐标并尝试使用跃迁装置，但由于存在“巨大的时间扭曲”，它拒绝启动";
	    		}
	    	}
	    	else
	    	{
	    		_displayText = "你试图使用跃迁装置，但没有目的地坐标，装置显然无法启动";
	    	}
	    }
	    else if (button == _nextButton)
	    {
			onContinue();
	    }
	}

	public void onContinue()
	{
		gameManager.popScreen();
	}
}

class TimeLoopCentralScreen extends EventScreen
{
	int _screenIndex = 0;
	Button _yes;
	Button _no;

	public void initButtons()
	{
		addContinueButton();
	}

	public String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "你正位于灰烬双星中心的一个巨大球形舱内。你在外面看到的两根巨型天线延伸到了地表以下，并汇聚到了密室中心一个精心制作的线圈状的挪麦科技装置中。这一定就是时间循环的源头！\n\n你发现了一个信号发射器，它能自动通知深巨星轨道上的挪麦空间站在每次循环开始时发射一个探测器\n\n时间循环装置需要超新星的能量来改变时间的流逝。几千年前，挪麦人曾试人工激发超新星，但没有成功";
		}

		return "你最终找到了通往密室中心的路，并找到了看起来像是时间循环机器的界面\n\n你想关闭时间循环吗？";
	}

	public void onButtonUp(Button button)
	{
		super.onButtonUp(button);
		
		if (button == _yes)
		{
			messenger.sendMessage("关闭时间循环");
			gameManager.popScreen();
		}
		else if (button == _no)
		{
			gameManager.popScreen();
		}
	}

	public void onContinue()
	{
		_screenIndex++;
		removeButtonFromToolbar(_nextButton);
		addButtonToToolbar(_yes = new Button("是"));
		addButtonToToolbar(_no = new Button("否"));
	}
}

class EyeOfTheUniverseScreen extends EventScreen
{
	int _screenIndex = 0;

	public String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "当你靠近围绕着宇宙之眼的奇特能量云时，你看到最后几颗恒星在远处爆发。宇宙变成了一片漆黑的虚无\n\n当你到达云层中心时，云层逐渐消散，露出一个漂浮在空中的奇特球形物体。在它闪闪发光的表面上，你看到了数十亿个光点。当你靠近时，你发现这些光点像是恒星和星系团。每当你把目光从球体上移开，再移回来时，恒星和星系的结构就会发生变化\n\n你启动了喷气背包，进入了球体...";
		}

		return "有那么一瞬间，你发现自己漂浮在星辰和银河的海洋中。突然，所有的恒星都坍缩成你正前方的一个光点。前几秒什么都没有发生，然后光点突然向外爆发出惊人的能量。冲击波将你猛烈撞向了空中\n\n你的生命维持系统正在崩溃，而你只能眼睁睁地看着能量与物质四向喷向太空\n\n过了一会，你的视野正在逐渐变黑";
	}

	public void onContinue()
	{
		_screenIndex++;

		if (_screenIndex == 2)
		{
			gameManager._solarSystem.player.currentSector = null;
			gameManager.pushScreen(new EndScreen());
		}
	}
}

class BrambleOutskirtsScreen extends EventScreen
{
	int _screenIndex = 0;
	Button _yes;
	Button _no;

	public void initButtons()
	{
		addDatabaseButton();
		addButtonToToolbar(_yes = new Button("是"));
		addButtonToToolbar(_no = new Button("否"));
	}

	public String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "你正在探索黑荆星的外围，藤蔓的顶端会开出巨大的异形白花（以及几朵蓝花）\n\n你注意到靠近每朵花中心的地方都有一个小开口...你想靠近仔细看看吗？";
		}

		return "当你靠近时，花朵打开了，一股奇怪的力量开始把你推进去。你拼命想逃离，但没有用\n\n你被一朵巨大的花朵毫不留情地吞噬了。世界一片漆黑，你能听到自己被消化的声音...";
	}

	public void onButtonUp(Button button)
	{
		super.onButtonUp(button);
		
		if (button == _yes)
		{
			_screenIndex++;
			removeButtonFromToolbar(_yes);
			removeButtonFromToolbar(_no);
			removeButtonFromToolbar(_databaseButton);
			addContinueButton();
		}
		else if (button == _no)
		{
			gameManager.popScreen();
		}
	}

	public void onInvokeClue(Clue clue)
	{
		if (clue.id.equals("D_3"))
		{
			gameManager.popScreen();
			messenger.sendMessage("follow the vine");
		}
		else
		{
			feed.publish("那个现在还不能帮助到你", true);
		}
	}

	public void onContinue()
	{
		playerData.killPlayer();
	}
}

class ExploreData
{
	Node _node;
	String _exploreString;
	JSONArray _exploreArray;

	JSONObject _nodeObj;
	JSONObject _activeExploreObj;

	boolean _dirty;

	ExploreData(Node node, JSONObject nodeObj)
	{
		_node = node;
		_nodeObj = nodeObj;
	}

	public void parseJSON()
	{
	    // parse as string
	    _exploreString = _nodeObj.getString("explore", "Nothing to see here!");
	    _exploreArray = new JSONArray();

	    // parse as explore object
	    if (_exploreString.charAt(0) == '{')
	    {
	    	_activeExploreObj = _nodeObj.getJSONObject("explore");
	    }
	    // parse as array of explore objects
	    else if (_exploreString.charAt(0) == '[')
	    {
	    	_exploreArray = _nodeObj.getJSONArray("explore");
	    	_activeExploreObj = _exploreArray.getJSONObject(0);
	    }

	    _dirty = true;
	}

	public void updateActiveExploreData()
	{
		// check wait times
		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	int turnCycle = exploreObj.getInt("turn cycle", 1);
			int turn = timeLoop.getActionPoints() % turnCycle;

			if (exploreObj.getInt("on turn", -1) == turn && exploreObj != _activeExploreObj)
			{
				_activeExploreObj = exploreObj;
				_dirty = true;
			}
	    }
	}

	public boolean canClueBeInvoked(String clueID)
	{
		if (clueID.equals("QM_2") && _node.allowQuantumEntanglement())
		{
			return true;
		}

		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	if (exploreObj.getString("require clue", "").equals(clueID) && exploreObj != _activeExploreObj)
	    	{
	    		return true;
	    	}

	    	// NO LONGER IN USE
	    	if (exploreObj.hasKey("clue event") && exploreObj.getJSONObject("clue event").getString("clue id").equals(clueID))
	    	{
	    		return true;
	    	}
	    }
	    return false;
	}

	public void invokeClue(String clueID)
	{
		if (clueID.equals("QM_2") && _node.allowQuantumEntanglement())
		{
			gameManager.popScreen();
			messenger.sendMessage("quantum entanglement");
		}

		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	// unlock explore screens
	    	if (exploreObj.getString("require clue", "").equals(clueID) && exploreObj != _activeExploreObj)
	    	{
	    		_activeExploreObj = exploreObj;
	    		_dirty = true;
	    	}

	    	// NO LONGER IN USE
	    	// fire clue events
	    	if (exploreObj.hasKey("clue event"))
	    	{
	    		String eventClueID = exploreObj.getJSONObject("clue event").getString("clue id");

	    		if (eventClueID.equals(clueID))
	    		{
	    			String eventID = exploreObj.getJSONObject("clue event").getString("event id");
		    		messenger.sendMessage(eventID);
	    		}
	    	}
	    }
	}

	public void explore()
	{
		updateActiveExploreData(); // sets dirty flag if explore data has changed

		if (_dirty && _activeExploreObj != null)
		{
		    fireEvents(_activeExploreObj);
		    discoverClues(_activeExploreObj);
		    revealHiddenPaths(_activeExploreObj);
		    _dirty = false;
		}
	}

	public void fireEvents(JSONObject exploreObj)
	{
		if (exploreObj.hasKey("fire event"))
		{
			messenger.sendMessage(exploreObj.getString("fire event"));
		}
		if (exploreObj.hasKey("move to"))
		{
			gameManager.swapScreen(new MoveToScreen(exploreObj.getString("text"), exploreObj.getString("move to")));
		}
		if (exploreObj.hasKey("teleport to"))
		{
			gameManager.swapScreen(new TeleportScreen(exploreObj.getString("text"), exploreObj.getString("teleport to")));
		}
	}

	public void discoverClues(JSONObject exploreObj)
	{
		if (exploreObj.hasKey("discover clue"))
		{
			playerData.discoverClue(exploreObj.getString("discover clue"));
		}
	}

	public void revealHiddenPaths(JSONObject exploreObj)
	{
		// reveal hidden paths
	    if (exploreObj.hasKey("reveal paths"))
	    {
	    	JSONArray pathArray = exploreObj.getJSONArray("reveal paths");

	    	for (int i = 0; i < pathArray.size(); i++)
	    	{
	    		_node.getConnection(pathArray.getString(i)).revealHidden();
	    	}

	    	feed.publish("你发现了一条隐藏通道！", true);
	    }
	}

	public String getExploreText()
	{
		if (_activeExploreObj != null)
		{
			return _activeExploreObj.getString("text", "no explore text");
		}

		return _exploreString;
	}
}

class ExploreScreen extends Screen implements DatabaseObserver
{
	final float BOX_WIDTH = 700;
	final float BOX_HEIGHT = 400;
	ExploreData _exploreData;

	Button _databaseButton;
	Button _backButton;
	Button _waitButton;

	ExploreScreen(Node location)
	{
		_exploreData = location.getExploreData();
		overlay = true; // continue to render BG

		addButtonToToolbar(_databaseButton = new Button("查看数据库", 0, 0, 150, 50));
		addButtonToToolbar(_waitButton  = new Button("等待 [ 1分钟 ]", 0, 0, 150, 50));
		addButtonToToolbar(_backButton = new Button("继续", 0, 0, 150, 50));

		_exploreData.parseJSON();
	}

	public void update(){}

	public void renderBackground() {}

	public void render()
	{
		pushMatrix();
		translate(width/2 - BOX_WIDTH/2, height/2 - BOX_HEIGHT/2);

			stroke(0, 0, 100);
			fill(0, 0, 0);
			rectMode(CORNER);
			rect(0, 0, BOX_WIDTH, BOX_HEIGHT);

			fill(0, 0, 100);

		    textFont(mediumFont);
		    textSize(18);
			textAlign(LEFT, TOP);
			text(_exploreData.getExploreText(), 10, 10, BOX_WIDTH - 20, BOX_HEIGHT - 10);

		popMatrix();

		feed.render();
		timeLoop.renderTimer();
	}

	public void onEnter() {}

	public void onExit() {}

	public void onInvokeClue(Clue clue)
	{
		// try to invoke it on the node first
		if (_exploreData.canClueBeInvoked(clue.id))
		{
			// force-quit the database screen
			gameManager.popScreen();
			_exploreData.invokeClue(clue.id);
			_exploreData.explore();
		}
		// next try the whole sector
		else if (locator.player.currentSector != null && locator.player.currentSector.canClueBeInvoked(clue))
		{
			gameManager.popScreen();
			locator.player.currentSector.invokeClue(clue);
		}
		else
		{
			feed.publish("那个现在还不能帮助到你", true);
		}
	}

	public void onButtonUp(Button button)
	{
		if (button == _databaseButton)
	    {
	      gameManager.pushScreen(gameManager.databaseScreen);
	      gameManager.databaseScreen.setObserver(this);
	    }
	    else if (button == _backButton)
	    {
	    	gameManager.popScreen();
	    }
	    else if (button == _waitButton)
	    {
	    	timeLoop.waitFor(1);
	    	_exploreData.explore();
	    }
	}

	public void onButtonEnterHover(Button button){}
	public void onButtonExitHover(Button button){}
}
class GameManager extends ScreenManager implements GlobalObserver
{
  // game screens
  TitleScreen titleScreen;
  DatabaseScreen databaseScreen;
  SolarSystemMapScreen solarSystemMapScreen;
  
  // game objects
  SolarSystem _solarSystem;
  Telescope _telescope;

  boolean _flashbackTriggered = false;
  
  public void newGame()
  {
    setupSolarSystem();

    titleScreen = new TitleScreen();
    databaseScreen = new DatabaseScreen();
    
    pushScreen(titleScreen);
  }

  public void resetTimeLoop()
  {
    _flashbackTriggered = false;
    _screenStack.clear();
    setupSolarSystem();
    loadSector(SectorName.TIMBER_HEARTH);
  }

  public void setupSolarSystem()
  {
    messenger.removeAllObservers();
    messenger.addObserver(this);

    feed.init();
    timeLoop.init();
    playerData.init();

    _telescope = new Telescope();

    _solarSystem = new SolarSystem();
    _solarSystem.timberHearth.addActor(_solarSystem.player, "村庄");

    solarSystemMapScreen = new SolarSystemMapScreen(_solarSystem);

    if (playerData.knowsLaunchCodes())
    {
      messenger.sendMessage("spawn ship");
    }

    locator = new Locator();
  }

  // runs after everything else updates
  public void lateUpdate()
  {
    // check if the sun explodes (this check has to be last to override all other screens)
    timeLoop.lateUpdate();

    // check if the player died
    if (playerData.isPlayerDead() && !_flashbackTriggered)
    {
      _flashbackTriggered = true;

      if (timeLoop.getEnabled())
      {
        swapScreen(new FlashbackScreen());
      }
      else
      {
        swapScreen(new GameOverScreen());
      }
    }
  }

  public void onReceiveGlobalMessage(Message message)
  {
    // TRIGGERED FROM SECTORSCREEN (NOT EXPLORE SCREEN)
    if (message.id.equals("death by anglerfish"))
    {
      pushScreen(new DeathByAnglerfishScreen());
    }
    else if (message.id.equals("dive attempt"))
    {
      pushScreen(new DiveAttemptScreen());
    }
    // TRIGGERED FROM EVENT SCREEN
    else if (message.id.equals("follow the vine"))
    {
      swapScreen(new FollowTheVineScreen());
    }
    // TRIGGERED FROM EXPLORE DATA
    else if (message.id.equals("explore ancient vessel"))
    {
      swapScreen(new AncientVesselScreen());
    }
    else if (message.id.equals("time loop central"))
    {
      swapScreen(new TimeLoopCentralScreen());
    }
    else if (message.id.equals("older than the universe"))
    {
      swapScreen(new EyeOfTheUniverseScreen());
    }
    else if (message.id.equals("explore bramble outskirts"))
    {
      swapScreen(new BrambleOutskirtsScreen());
    }
  }

  public void loadTelescopeView()
  {
    pushScreen(new SolarSystemTelescopeScreen(_solarSystem, _telescope));

    // if (_solarSystem.player.currentSector != null)
    // {
    //   loadSectorTelescopeView(_solarSystem.player.currentSector);
    // }
  }

  public void loadSectorTelescopeView(Sector sector)
  {
    pushScreen(new SectorTelescopeScreen(sector, _telescope));
  }
  
  public void loadSolarSystemMap()
  {
    swapScreen(solarSystemMapScreen);
  }
  
  public void loadSector(SectorName sectorName)
  {
    loadSector(_solarSystem.getSectorByName(sectorName));
  }

  public void loadSector(Sector sector)
  {
    swapScreen(new SectorScreen(sector, _solarSystem.player, _solarSystem.ship));
  }
}

interface GlobalObserver
{
	public void onReceiveGlobalMessage(Message message);
}

class GlobalMessenger
{
	ArrayList<GlobalObserver> _observers;

	GlobalMessenger()
	{
		_observers = new ArrayList<GlobalObserver>();
	}

	public void addObserver(GlobalObserver observer)
	{
		if (!_observers.contains(observer))
		{
			_observers.add(observer);
		}
		//println("Observer Count: " + _observers.size());
	}

	public void removeObserver(GlobalObserver observer)
	{
		_observers.remove(observer);
	}

	public void removeAllObservers()
	{
		_observers.clear();
	}

	public void sendMessage(String messageID)
	{
		sendMessage(new Message(messageID));
	}

	public void sendMessage(Message message)
	{
		for (int i = 0; i < _observers.size(); i++)
		{
			_observers.get(i).onReceiveGlobalMessage(message);
		}
	}
}

class Message
{
	String id;
	String text;

	Message(String messageID)
	{
		id = messageID;
	}

	Message(String messageID, String t)
	{
		this(messageID);
		text = t;
	}
}
class Locator
{
	Actor player;
	Actor ship;

	QuantumMoon _quantumSector;

	Locator()
	{
		player = gameManager._solarSystem.player;
		ship = gameManager._solarSystem.ship;
		_quantumSector = (QuantumMoon)(gameManager._solarSystem.quantumMoon);
	}

	public int getQuantumMoonLocation()
	{
		return _quantumSector.getQuantumLocation();
	}
}
interface NodeButtonObserver
{
  public void onNodeSelected(Node node);
  public void onNodeGainFocus(Node node);
  public void onNodeLoseFocus(Node node);
}

interface NodeObserver
{
  public void onNodeVisited(Node node);
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

  public void loadJSON(JSONObject nodeJSONObj)
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

  public void savePosition()
  {
    _nodeJSONObj.getJSONObject("position").setFloat("x", position.x);
    _nodeJSONObj.getJSONObject("position").setFloat("y", position.y);
    println(_id + " position saved: " + position);
  }

  public void onReceiveGlobalMessage(Message message)
  {

  }

  public boolean isExplorable() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("explore"));}

  public ExploreData getExploreData() {return _exploreData;}

  public String getProbeDescription() 
  {
    if (_nodeJSONObj.hasKey("probe description"))
    {
      return _nodeJSONObj.getString("probe description");
    }
    return getDescription();
  }

  public String getDescription() {return _nodeJSONObj.getString("description", "a vast expanse of nothing");}

  public boolean hasDescription() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("description"));}

  public boolean isProbeable() {return (_nodeJSONObj != null && _nodeJSONObj.hasKey("description"));}

  public boolean isConnectedTo(Node node) {return _connections.containsKey(node);}

  public boolean inRange()
  {
    return _inRange;
  }

  public void updateInRange(boolean isPlayerInShip, Node playerNode) 
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
  
  public NodeConnection getConnection(Node node) {return _connections.get(node);}

  public NodeConnection getConnection(String nodeID)
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

  public boolean allowQuantumEntanglement() // note - "quantum state" only used for Quantum Moon right now
  {
    if (_nodeJSONObj == null) return false;
    return _nodeJSONObj.getBoolean("entanglement node", false);
  }

  public Signal getSignal() {return _signal;}

  public String getID() {return _id;}

  public String getActualName() {return _name;}
  
  public String getKnownName()
  {
    if (_visited) return getActualName();
    else return "？？？";
  } 

  public void setVisible(boolean visible) {_visible = visible;}
  
  public void visit()
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

  public void explore()
  {
    _explored = true;
    _exploreData.explore();

    if (_signal != null)
    {
      playerData.learnFrequency(_signal.frequency);
    }
  }
  
  public void update()
  {
    if (!_visible) {return;}
    _button.enabled = inRange() || EDIT_MODE;
    _button.update();
  }

  public float getAlpha()
  {
    if (!inRange())
    {
      return 35;
    }
    return 100;
  }

  public float getSize()
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
  
  public void draw()
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

  public void drawName()
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

  public void addConnection(NodeConnection connection)
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

  public void setNodeObserver(NodeObserver observer)
  {
    _observer = observer;
  }

  public void addObserver(NodeButtonObserver observer)
  {
    _observers.add(observer);
  }
  
  public void removeAllObservers()
  {
    _observers.clear();
  }
  
  public void onButtonUp(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeSelected(this);
    }
  }
  
  public void onButtonEnterHover(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeGainFocus(this);
    }
  }
  
  public void onButtonExitHover(Button button)
  {
    for (int i = 0; i < _observers.size(); i++)
    {
      _observers.get(i).onNodeLoseFocus(this);
    }
  }
}
interface NodeActionObserver
{
	public void onExploreNode(Node node);
	public void onProbeNode(Node node);
	public void onTravelAttempt(boolean succeeded, Node node, NodeConnection connection);
}

abstract class NodeAction
{
	String _prompt = "";
	int _mouseButton = LEFT;
	NodeActionObserver _observer;

	public void setObserver(NodeActionObserver observer)
	{
		_observer = observer;
	}

	public int getMouseButton()
	{
		return _mouseButton;
	}

	public void setMouseButton(int button)
	{
		_mouseButton = button;
	}

	public abstract void execute();
	
	public int getCost()
	{
		return 0;
	}

	public String getPrompt()
	{
		return _prompt;
	}

	public void setPrompt(String description)
	{
		if (_mouseButton == LEFT)
		{
			_prompt += "左键 - " + description;
		}
		else
		{
			_prompt += "右键 - " + description;
		}

		_prompt += " [ " + getCost() + " 分钟 ]";
	}
}

class ProbeAction extends NodeAction
{
	Actor _player;
	Node _location;

	ProbeAction(int button, Actor player, Node location, NodeActionObserver observer)
	{
		_player = player;
		_location = location;
		setMouseButton(button);
		setObserver(observer);
		setPrompt("发射侦察兵");
	}

	public void execute()
	{
		feed.publish("你看见了" + _location.getProbeDescription());

		Actor probe = new Probe();
		_player.currentSector.addActor(probe);
		probe.setScreenPosition(_player.screenPosition);
		probe.moveToNode(_location);

		_observer.onProbeNode(_location);
	}
}

class ExploreAction extends NodeAction
{
	Node _location;

	ExploreAction(int button, Node location, NodeActionObserver observer)
	{
		_location = location;
		setMouseButton(button);
		setObserver(observer);
		setPrompt("探索");
	}

	public int getCost()
	{
		return 1;
	}

	public void execute()
	{
		timeLoop.spendActionPoints(getCost());

		// prevent the action from happening if the sun's going to explode
		if (timeLoop.getActionPoints() == 0) 
		{
			return;
		}

		feed.clear();
		feed.publish("你探索了" + _location.getActualName());
		
		_observer.onExploreNode(_location);
		gameManager.pushScreen(new ExploreScreen(_location));
		_location.explore();
	}
}

class TravelAction extends NodeAction
{
	Actor _player;
	Actor _ship;
	Node _destination;

	TravelAction(int button, Actor player, Node destination, NodeActionObserver observer)
	{
		this(button, player, null, destination, observer);
	}

	TravelAction(int button, Actor player, Actor ship, Node destination, NodeActionObserver observer)
	{
		_ship = ship;
		_player = player;
		_destination = destination;
		setMouseButton(button);
		setObserver(observer);
		setPrompt();
	}

	public void setPrompt()
	{
		if (_ship != null)
		{
			if (_ship.currentNode == null && _destination.gravity)
			{
				setPrompt("降落到此处");
				return;
			}
			setPrompt("飞到此处");
		}
		else if (_destination.gravity)
		{
			setPrompt("移动到此处");
		}
		else
		{
			setPrompt("飞到此处");
		}
	}

	public int getCost()
	{
		return 1;
	}

	public void execute()
	{
		feed.clear();

		if (_player.currentNode != null)
		{
			NodeConnection connection = _destination.getConnection(_player.currentNode);

			if (connection != null)
			{	
				if (!connection.traversibleFrom(_player.currentNode))
				{
					connection.fireFailEvent();
					feed.publish(connection.getWrongWayText(), true);
					return;
				}

				connection.fireTraverseEvent();
				connection.traverse();

				if (connection.hasDescription())
				{
					feed.publish("你穿过了" + connection.getDescription());
				}
			}
		}

		// publish feed first in case we want to override it (e.g. death-by-anglerfish scenario)
		if (_destination.hasDescription())
		{
			feed.publish("你已抵达" + _destination.getDescription());
		}

		if (_ship != null)
		{
			_ship.moveToNode(_destination);
		}
		
		messenger.sendMessage("reset reachability");
		_player.moveToNode(_destination);
		_observer.onTravelAttempt(true, _destination, _destination.getConnection(_player.currentNode));
		timeLoop.spendActionPoints(getCost());
	}
}
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

  public void updateAdjacentToPlayer(Node playerNode)
  {
    _adjacentToPlayer = false;

    if (node1 == playerNode || node2 == playerNode)
    {
      _adjacentToPlayer = true;
    }
  }

  public boolean hasDescription()
  {
    return _hasDescription;
  }

  public String getDescription()
  {
    return _description;
  }

  public String getWrongWayText()
  {
    return "看起来这条路只能从另一侧才能通行";
  }

  public Node getOtherNode(Node node)
  {
    if (node == node1)
    {
      return node2;
    }

    return node1;
  }
  
  public boolean traversibleFrom(Node startingNode)
  {
    return (!_gated && (!_oneWay || startingNode == node1));
  }

  public void fireTraverseEvent()
  {
    if (_connectionObj.hasKey("traverse event"))
    {
      messenger.sendMessage(_connectionObj.getString("traverse event"));
    }
  }

  public void fireFailEvent()
  {
    if (_connectionObj.hasKey("fail event"))
    {
      messenger.sendMessage(_connectionObj.getString("fail event"));
    }
  }

  public void traverse()
  {
    //_oneWay = false;
    _traversed = true;
  }

  public void revealHidden()
  {
    _hidden = false;
    reveal();
  }

  public void setVisible(boolean visible) {_visible = visible;}
  
  public void reveal()
  {
    if (_hidden)
    {
      return;
    }

    node1.setVisible(true);
    node2.setVisible(true);
    _visible = true;
  }

  public float getAlpha()
  {
    if (!_adjacentToPlayer)
    {
      return 35;
    }
    return 100;
  }
  
  public void render()
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
class PlayerData implements GlobalObserver
{
	boolean _knowsLaunchCodes;
	boolean _knowsSignalCoordinates;

	ArrayList<Clue> _clueList;
	ArrayList<Frequency> _knownFrequencies;

	int _knownClueCount = 0;

	// resets every loop
	boolean _isDead = false;

	PlayerData()
	{
		_knowsLaunchCodes = START_WITH_LAUNCH_CODES;
		_knowsSignalCoordinates = START_WITH_COORDINATES;

		_clueList = new ArrayList<Clue>();
		_knownFrequencies = new ArrayList<Frequency>();
		_knownFrequencies.add(Frequency.TRAVELER);

		if (START_WITH_SIGNALS)
		{ 
			_knownFrequencies.add(Frequency.BEACON);
			_knownFrequencies.add(Frequency.QUANTUM);
		}

		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_1", "沉底模块", "数据收集模块从挪麦探测器发射器上脱落，掉进了深巨星的中心"));
		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_2", "汹涌的龙卷风", "深巨星上的大多数龙卷风都有强烈的上升气流，但有些逆时针旋转的龙卷风有着下行气流"));
		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_3", "水母", "深巨星水母的空腔恰好能够容下一个人"));

		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_3", "第五个位置", "量子卫星有时会拜访太阳系外的第五个位置"));
		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_1", "量子成像", "观察量子物体的照片与直接观察物体本身一样，能有效地防止物体移动"));
		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_2", "量子纠缠", "普通物体在靠近量子物体时会与之“纠缠”在一起，并开始表现出量子属性\n\n只要无法观察自己或周围环境，即使是生命体也会被纠缠"));

		_clueList.add(new Clue(Curiosity.VESSEL, "D_1", "失落的飞船", "挪麦人来到这个太阳系是为了寻找它们称之为“宇宙之眼”的神秘的信号，它们乘坐的飞船在黑荆星的某处遇难"));
		_clueList.add(new Clue(Curiosity.VESSEL, "D_2", "孩童的游戏", "挪麦人孩童们玩了一个游戏，重现了族人逃离黑荆星的情景。根据游戏规则，三名玩家（逃生舱）必须在不被发现的情况下偷偷溜过蒙着眼睛的玩家（鮟鱇鱼）"));
		_clueList.add(new Clue(Curiosity.VESSEL, "D_3", "追踪装置", "挪麦飞船坠毁在黑荆的根部。挪麦人试图将追踪装置插入黑荆的一根藤蔓中，以重新定位根部，但它们无法穿透藤蔓坚硬的外表"));

		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_1", "时间循环装置", "挪麦研究人员在深巨星制造出一个小型但功能正常的时间循环装置后，计划在灰烬双星上建造一个完整规模的装置（前提是能产生足够的能量为其提供动力）"));
		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_2", "跃迁塔", "挪麦人建造了特殊的塔楼，可以将塔内的任何人传送到相应的接收平台。只有当你能透过塔顶看到目的地时，才会传送"));
		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_3", "大工程", "挪麦人挖掘了沙漏双星，建造了一台能够利用超新星能量的巨大装置\n\n控制中心位于行星中心的一个中空空腔内，与地表完全隔绝"));
	}

	public void init()
	{
		messenger.addObserver(this);
		_isDead = false;
	}

	public void onReceiveGlobalMessage(Message message)
	{
		if (message.id.equals("learn launch codes") && !_knowsLaunchCodes)
		{
			_knowsLaunchCodes = true;
			feed.publish("已获取发射密码", true);
			messenger.sendMessage("spawn ship");
		}
		else if (message.id.equals("learn signal coordinates") && !_knowsSignalCoordinates)
		{
			_knowsSignalCoordinates = true;
			feed.publish("已获取信号坐标", true);
		}
	}

	public void killPlayer()
	{
		_isDead = true;
	}

	public boolean isPlayerDead()
	{
		return _isDead;
	}

	public boolean isPlayerAtEOTU()
	{
		return ((locator.player.currentSector == gameManager._solarSystem.quantumMoon && locator.getQuantumMoonLocation() == 4) || locator.player.currentSector == gameManager._solarSystem.eyeOfTheUniverse);
	}

	public boolean knowsFrequency(Frequency frequency)
	{
		return _knownFrequencies.contains(frequency);
	}

	public boolean knowsSignalCoordinates()
	{
		return _knowsSignalCoordinates;
	}

	public void learnFrequency(Frequency frequency)
	{
		if (!knowsFrequency(frequency))
		{
			_knownFrequencies.add(frequency);
			feed.publish("频率已识别: " + frequencyToString(frequency), true);
		}
	}

	public int getFrequencyCount()
	{
		return _knownFrequencies.size();
	}

	public boolean knowsLaunchCodes()
	{
		return _knowsLaunchCodes;
	}

	public Clue getClueAt(int i)
	{
		return _clueList.get(i);
	}

	public int getClueCount()
	{
		return _clueList.size();
	}

	public int getKnownClueCount()
	{
		return _knownClueCount;
	}

	public void discoverClue(String id)
	{
		for (int i = 0; i < _clueList.size(); i++)
		{
			if (_clueList.get(i).id.equals(id) && !_clueList.get(i).discovered)
			{
				_clueList.get(i).discovered = true;
				_knownClueCount++;
				feed.publish("信息已添加至数据库", true);
			}
		}
	}
}

class Clue
{
	String id;
	String name;
	String description;
	boolean discovered;
	boolean invoked = false;
	Curiosity curiosity;

	Clue(Curiosity curiosity, String id, String name, String description)
	{
		this.curiosity = curiosity;
		this.id = id;
		this.name = name;
		this.description = description;
		this.discovered = false || START_WITH_CLUES;
	}
}
class QuantumNode extends Node
{
	QuantumNode(String name, JSONObject nodeJSON)
	{
		super(name, nodeJSON);
	}

	public void updateQuantumStatus(int quantumState)
	{
		boolean visible = _nodeJSONObj.getInt("quantum location") == quantumState;
		setVisible(visible);

		// hide connections
		if (!visible)
		{
			for (NodeConnection connection : _connections.values()) 
		    {
		    	connection.setVisible(visible);
		    }
		}
	}

	public boolean allowQuantumEntanglement()
	{
		return _nodeJSONObj.getInt("quantum location") == locator.getQuantumMoonLocation() && _nodeJSONObj.getBoolean("entanglement node", false);
	}
}

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
  
  public void addButton(Button button)
  {
    _buttons.add(button);
    button.setObserver(this);
  }
  
  public void removeButton(Button button)
  {
    _buttons.remove(button);
  }

  public void addButtonToToolbar(Button button)
  {
    addButton(button);
    _toolbarButtons.add(button);
    _toolbarRoot.addChild(button);
    updateToolbarPositions();
  }

  public void removeButtonFromToolbar(Button button)
  {
    removeButton(button);
    _toolbarButtons.remove(button);
    _toolbarRoot.removeChild(button);
    updateToolbarPositions();
  }

  public void updateToolbarPositions()
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
  
  public abstract void update();
  public abstract void render();
  
  public void onEnter(){}
  public void onExit(){}
  
  public void onButtonUp(Button button){}
  public void onButtonEnterHover(Button button){}
  public void onButtonExitHover(Button button){}
  
  public void updateInput()
  {
    for(int i = 0; i < _buttons.size(); i++)
    {
      _buttons.get(i).update();
    }
  }
  
  public void renderBackground()
  {
    int bgColor = color(0, 0, 0);
    int starColor = color(0, 0, 100);

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
  
  public void renderButtons()
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
abstract class ScreenManager
{
  ArrayList<Screen> _screenStack;
  boolean _skipRender = false;

  ScreenManager()
  {
    _screenStack = new ArrayList<Screen>();
  }

  public void lateUpdate()
  {
    // just in case gamemanager needs to do anything
  }

  public void runGameLoop()
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
  
  public void swapScreen(Screen newScreen)
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
  
  public void pushScreen(Screen nextScreen)
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
  
  public void popScreen()
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

  public void setAnchorOffset(float offsetX, float offsetY)
  {
    _sectorRoot.setPosition(width/2 + offsetX, height/2 + offsetY);
  }
  
  public void loadFromJSON(String filename)
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

  public Node createNode(String name, JSONObject nodeObj)
  {
    return new Node(name, nodeObj);
  }

  public void saveSectorJSON()
  {
    saveJSONObject(_sectorJSON, "data/" + _JSONFilename);
    println("Sector JSON saved");
  }
  
  public void load()
  {
    // stub to override
  }
  
  public void drawSectorBackdrop()
  {
    // stub to override
  }

  public void onReceiveGlobalMessage(Message message)
  {
    if (message.id.equals("quantum entanglement"))
    {
      onQuantumEntanglement();
    }
  }

  public void onNodeVisited(Node node)
  {
    updateNodeRanges(gameManager._solarSystem.isPlayerInShip(), node);
  }

  public void onArrival()
  {
    if (!_skipArrivalScreen && _sectorJSON.hasKey("Sector Arrival"))
    {
      _skipArrivalScreen = true;
      gameManager.pushScreen(new SectorArrivalScreen(_sectorJSON.getJSONObject("Sector Arrival").getString("text"), getName()));
    }
  }

  public void onQuantumEntanglement()
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

  public boolean canClueBeInvoked(Clue clue)
  {
    return false;
  }

  // called from SectorScreen
  public void invokeClue(Clue clue)
  {
    // override in derrived class
  }

  // update whether each node is "in range" (i.e. selectable)
  public void updateNodeRanges(boolean isPlayerInShip, Node playerNode)
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

  public boolean allowTelescope() {return true;}

  public String getName()
  {
    return _name;
  }
  
  public Node getNode(int index)
  {
    if (_nodes.get(index) != null)
    {
      return _nodes.get(index);
    }
    println("No nodes at index " + index);
    return null;
  }
  
  public Node getNode(String nodeID)
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

  public ArrayList<SignalSource> getSectorSignalSources()
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

  public ArrayList<Signal> getSectorSignals()
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
  
  public void addNodeButtonObserver(NodeButtonObserver observer)
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).addObserver(observer);
    }
  }
  
  public void removeAllNodeButtonObservers()
  {
    for (int i = 0; i < _nodes.size(); i++)
    {
      _nodes.get(i).removeAllObservers();
    }
  }
  
  public void update()
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

  public void renderBackground()
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
  
  public void render()
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
  
  public void addNode(Node node)
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
  
  public void removeNode(Node node)
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
  
  public void addActor(Actor actor)
  {
    addActor(actor, (Node)null);
  }
  
  public void addActor(Actor actor, String nodeName)
  {
    addActor(actor, getNode(nodeName));
  }
  
  public void addActor(Actor actor, Node atNode)
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
  
  public void removeActor(Actor actor)
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

  public float getRadius()
  {
    return _bounds.y * 0.5f;
  }

  public Sector getSector()
  {
    return _sector;
  }
  
  public void render()
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
  
  public abstract void drawPlanet();

  public void drawNonHighlighted(){}
  
  public void drawName(String name, float xPos)
  {
    fill(0, 0, 100);
    textAlign(CENTER, CENTER);
    textSize(18);
    text(name, xPos, screenPosition.y - getRadius() - 40);
  }

  public void drawName()
  {
    drawName(id, screenPosition.x);
  }

  public void drawYouAreHere()
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

  public void drawZoomPrompt()
  {
    textSize(14);
    textFont(smallFont);

    fill(0, 0, 0);
    stroke(0, 0, 100);
    rectMode(CENTER);
    rect(screenPosition.x, screenPosition.y + getRadius() + 40, textWidth("左键放大") + 10, 20);

    fill(0, 0, 100);
    textAlign(CENTER, CENTER);
    text("左键放大", screenPosition.x, screenPosition.y + getRadius() + 40);
  }
}

class CometButton extends SectorButton
{
  CometButton(float x, float y, Sector sector)
  {
    super("彗星", x, y, 40, 40, sector);
  }
  
  public void drawPlanet()
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
    super("烬双星", centerX, y, 50, 50, sector);
    _isRightTwin = isRightTwin;
    _centerX = centerX;
    
    if (isRightTwin) 
    {
      _twinName = "灰";
      setPosition(centerX + 35, position.y);
    }
    else
    {
      _twinName = "余";
      setPosition(centerX - 35, position.y);
    }
  }

  public void drawName()
  {
    super.drawName(_twinName + id, _centerX);
  }

  public void drawNonHighlighted()
  {
    if (!_isRightTwin)
    {
      stroke(60, 100, 100);
      fill(0, 0, 0);
      rectMode(CENTER);
      rect(_centerX, position.y, _bounds.y * 1.5f, _bounds.y * 0.2f);
    }
  }
  
  public void drawPlanet()
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
    super("木灶星", x, y, 80, 80, sector);
  }
  
  public void drawPlanet()
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
    super("碎空星", x, y, 80, 80, sector);
  }
  
  public void drawPlanet()
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
    super("深巨星", x, y, 150, 150, sector);
  }
  
  public void drawPlanet()
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
    super("黑荆星", x, y, 230, 230, sector);
  }
  
  public void drawPlanet()
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
    super("量子卫星", 0, 0, 30, 30, sector);
    _targets = targets;
  }

  public void collapse()
  {
    ((QuantumMoon)_sector).collapse();
    updatePosition();
  }

  public void updatePosition()
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
  
  public void drawPlanet()
  {
    stroke(300, 50, 100);
    fill(0, 0, 0);
    ellipse(position.x, position.y, _bounds.x, _bounds.y);
  }
}

class SectorEditor implements NodeButtonObserver, ButtonObserver
{
  Sector _activeSector;
  Button _saveButton;
  
  Node _selection;
  boolean _dragging = false;
  
  SectorEditor(Sector sector)
  {
    _activeSector = sector;
    _saveButton = new Button("保存", width - 75, height - 50, 100, 50);
    _saveButton.setObserver(this);
  }
  
  public void update()
  {
    _saveButton.update();
    
    if (_selection != null)
    {
      if (_dragging)
      {
        _selection.setScreenPosition(new Vector2(mouseX, mouseY));
        
        if (!mousePressed)
        {
          _selection.savePosition();
          _selection = null;
          _dragging = false;
        }
      }
      
      _dragging = (mousePressed && mouseButton == CENTER);
    }
  }
  
  public void render()
  {
    _saveButton.render();
  }
  
  public void onButtonUp(Button button)
  {
    if (button == _saveButton)
    {
      _activeSector.saveSectorJSON();
    }
  }
  
  public void onNodeGainFocus(Node node)
  {
    _selection = node;
  }
  
  public void onNodeLoseFocus(Node node)
  {
    if (!_dragging)
    {
      _selection = null;
    }
  }

  public void onTravelToNode(Node node){}
  public void onExploreNode(Node node){}
  public void onProbeNode(Node node){}
  
  public void onButtonEnterHover(Button button){}
  public void onButtonExitHover(Button button){}
  public void onNodeSelected(Node node){}
}
class Comet extends Sector
{  
  public void load()
  {
    _name = "彗星";
    loadFromJSON("sectors/comet.json");
    //setAnchorOffset(100, 30);
  }
  
  public void drawSectorBackdrop()
  {
    stroke(200, 30, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 300, 300);
  }
}

class RockyTwin extends Sector
{  
  public void load()
  {
    _name = "余烬双星";
    loadFromJSON("sectors/rocky_twin.json");
    //setAnchorOffset(100, 30);
  }
  
  public void drawSectorBackdrop()
  {
    stroke(60, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class SandyTwin extends Sector
{  
  public void load()
  {
    _name = "灰烬双星";
    loadFromJSON("sectors/sandy_twin.json");
    //setAnchorOffset(100, 30);
  }
  
  public void drawSectorBackdrop()
  {
    stroke(60, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class TimberHearth extends Sector
{  
  public void load()
  {
    _name = "木灶星";
    loadFromJSON("sectors/timber_hearth.json");
    //setAnchorOffset(100, 30);
  }
  
  public void drawSectorBackdrop()
  {
    stroke(200, 100, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 500, 500);
  }
}

class BrittleHollow extends Sector
{  
  public void load()
  { 
    _name = "碎空星";
    loadFromJSON("sectors/brittle_hollow.json");
  }
  
  public void drawSectorBackdrop()
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
  public void load()
  { 
    _name = "深巨星";
    loadFromJSON("sectors/giants_deep.json");
    setAnchorOffset(0, 60);
  }
  
  public void drawSectorBackdrop()
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

  public void load()
  {
    _name = "黑荆星";
    loadFromJSON("sectors/dark_bramble.json");

    for (int i = 0; i < _fogLightNodes.size(); i++)
    {
      int index = PApplet.parseInt(random(0, _fogLightPositions.size()));
      //println("pos: " + _fogLightPositions.get(index));
      _fogLightNodes.get(i).setPosition(_fogLightPositions.get(index));
      _fogLightPositions.remove(index);
    }
  }

  public Node createNode(String name, JSONObject nodeObj)
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
  
  public void drawSectorBackdrop()
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

  public void load()
  {
    _name = "量子卫星";
    loadFromJSON("sectors/quantum_moon.json");
    //setAnchorOffset(100, 30);
  }

  public void onReceiveGlobalMessage(Message message)
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

  public void onArrival()
  {
    // overrides normal arrival screen
    gameManager.pushScreen(new QuantumArrivalScreen());
  }

  public Node createNode(String name, JSONObject nodeObj)
  {
    return new QuantumNode(name, nodeObj);
  }

  public void collapse()
  {
    // move to random location
    int tLocation = _quantumLocation;
    while (tLocation == _quantumLocation)
    {
      tLocation = PApplet.parseInt(random(5));
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

  public void onQuantumEntanglement()
  {
    super.onQuantumEntanglement();
      
    // remove ship
    if (locator.ship.currentSector == this)
    {
      locator.ship.currentSector.removeActor(locator.ship);
    }
  }

  public int getQuantumLocation() {return _quantumLocation;}

  public boolean allowTelescope() {return false;}
  
  public void drawSectorBackdrop()
  {
    stroke(300, 50, 100);
    fill(0, 0, 0);
    ellipse(0, 0, 300, 300);
  }

  public void removeActor(Actor actor)
  {
    super.removeActor(actor);
    actor.lastSector = null; // prevents animation of leaving Sector
  }
}

class EyeOfTheUniverse extends Sector
{  
  public void load()
  {
    _name = "比宇宙更为古老的事物";
    loadFromJSON("sectors/eye_of_the_universe.json");
  }

  public boolean allowTelescope() {return false;}
  
  public void drawSectorBackdrop()
  {
  }
}

class SectorScreen extends Screen implements NodeButtonObserver, NodeActionObserver, DatabaseObserver
{
  Actor _player;
  Actor _ship;
  Sector _sector;
  
  SectorEditor _editor;
  
  Node _focusNode;

  Button _databaseButton;
  Button _liftoffButton;
  Button _waitButton;
  Button _telescopeButton;

  String _actionlessPrompt = "inaccessible";
  ArrayList<NodeAction> _actions;

  SectorScreen(Sector sector, Actor player, Actor ship)
  {
    super();
    
    _actions = new ArrayList<NodeAction>();
    _editor = new SectorEditor(sector);
    
    _player = player;
    _ship = ship;
    _sector = sector;

    addButtonToToolbar(_databaseButton = new Button("查看数据库", 0, 0, 150, 50));
    addButtonToToolbar(_telescopeButton = new Button("扫描信号", 0, 0, 150, 50));
    _telescopeButton.setDisabledPrompt("扫描信号\n（视野受阻）");

    addButtonToToolbar(_waitButton  = new Button("等待 [ 1 分钟 ]", 0, 0, 150, 50));
    addButtonToToolbar(_liftoffButton  = new Button("离开该区域", 0, 0, 150, 50));
    _liftoffButton.setDisabledPrompt("离开该区域\n（必须位于飞船旁边）");
  }

  public void onEnter()
  {
    _sector.addNodeButtonObserver(this);
    _sector.addNodeButtonObserver(_editor);
    _sector.updateNodeRanges(isPlayerInShip(), _player.currentNode);
    _focusNode = null;
  }
  
  public void onExit()
  {
    _sector.removeAllNodeButtonObservers();
  }

  public void onButtonUp(Button button)
  {
    if (button == _databaseButton)
    {
      gameManager.databaseScreen.setObserver(this);
      gameManager.pushScreen(gameManager.databaseScreen);
    }
    else if (button == _liftoffButton)
    {
      _sector.removeActor(_player);
      _sector.removeActor(_ship);
      gameManager.loadSolarSystemMap();
      feed.clear();
      feed.publish("你已离开" + _sector.getName());
    }
    else if (button == _telescopeButton)
    {
      gameManager.loadTelescopeView();
    }
    else if (button == _waitButton)
    {
      timeLoop.waitFor(1);
    }
  }

  public void onInvokeClue(Clue clue)
  {
    // check for node-specific clue effects
    if (_player.currentNode != null && _player.currentNode.isExplorable())
    {
      ExploreData exploreData = _player.currentNode.getExploreData();
      exploreData.parseJSON();

      if (exploreData.canClueBeInvoked(clue.id))
      {
        gameManager.popScreen(); // force-quit the database screen
        gameManager.pushScreen(new ExploreScreen(_player.currentNode)); // push on a new explorescreen
        exploreData.invokeClue(clue.id);
        exploreData.explore();
        return;
      }
    }
    
    // next try the whole sector
    if (_player.currentSector != null && _player.currentSector.canClueBeInvoked(clue))
    {
      _player.currentSector.invokeClue(clue);
    }
    else
    {
      feed.publish("那个现在还不能帮助到你", true);
    }
  }

  public void update()
  {
    // update action button visibility
    _liftoffButton.enabled = (_player.currentNode == _ship.currentNode);
    _telescopeButton.enabled = (_player.currentNode != null && _player.currentNode.allowTelescope && _player.currentSector.allowTelescope());

    _sector.update();
    if (EDIT_MODE) _editor.update();
  }

  public void renderBackground()
  {
    super.renderBackground();
    _sector.renderBackground();
  }

  public void render()
  {
    _sector.render();
    feed.render();
    timeLoop.renderTimer();

    fill(0, 0, 100);
    textSize(18);
    textFont(mediumFont);
    textAlign(RIGHT);
    text(_sector.getName(),width - 20, height - 100);

    if (!active) return;
    if (EDIT_MODE) _editor.render();

    drawNodeGUI(_focusNode, _actions);
  }

  public void drawNodeGUI(Node target, ArrayList<NodeAction> actions)
  {
    if (target == null || (_actions.size() == 0 && target == _player.currentNode)) {return;}

    smallFont();

    float yOffset = 60;
    float promptWidth = textWidth(_actionlessPrompt);

    // find prompt width
    for (int i = 0; i < actions.size(); i++) 
    {
      promptWidth = max(promptWidth, textWidth(actions.get(i).getPrompt()));
    }

    // draw box
    stroke(200, 100, 100);
    fill(0, 0, 0);
    rectMode(CORNER);
    rect(target.screenPosition.x - promptWidth * 0.5f - 10, target.screenPosition.y + yOffset - 15, promptWidth + 15, max(20, 20 * actions.size()) + 10);

    // draw prompts
    fill(0, 0, 100);
    textAlign(LEFT, CENTER);

    for (int i = 0; i < actions.size(); i++) 
    {
      text(actions.get(i).getPrompt(), target.screenPosition.x - promptWidth * 0.5f, target.screenPosition.y + yOffset + 20 * i);
    }

    if (actions.size() == 0)
    {
      text(_actionlessPrompt, target.screenPosition.x - promptWidth * 0.5f, target.screenPosition.y + yOffset);
    }
  }

  public void onTravelAttempt(boolean succeeded, Node node, NodeConnection connection){}
  public void onExploreNode(Node node){}
  public void onProbeNode(Node node){}

  public void onNodeSelected(Node node)
  {
    /** EXCECUTE ACTION **/
    for (int i = 0; i < _actions.size(); i++) 
    {
      if (_actions.get(i).getMouseButton() == mouseButton)
      {
        _actions.get(i).execute();
        break;
      }
    }

    refreshAvailableActions();
  }
  
  public void onNodeGainFocus(Node node)
  {
    _focusNode = node;
    refreshAvailableActions();
  }
  
  public void onNodeLoseFocus(Node node)
  {
    if (node == _focusNode)
    {
      _focusNode = null;
      refreshAvailableActions();
    }
  }

  public void refreshAvailableActions()
  {
    _actions.clear();

    if (_focusNode == null) 
    {
      return;
    }

    if (_player.currentNode != _focusNode)
    {
      if (_focusNode.inRange())
      {
        if (_focusNode.isProbeable())
        {
          _actions.add(new ProbeAction(RIGHT, _player, _focusNode, this));
        }
        
        if (isPlayerInShip() && _focusNode.shipAccess)
        {
          _actions.add(new TravelAction(LEFT, _player, _ship, _focusNode, this));
        }
        else
        {
          _actions.add(new TravelAction(LEFT, _player, _focusNode, this));
        }
      }
    }
    else
    {
      if (_focusNode.isExplorable())
      {
        _actions.add(new ExploreAction(LEFT, _focusNode, this));
      }
    }
  }

  public boolean isPlayerInShip()
  {
    return(_player.currentNode == _ship.currentNode);
  }
}

class SectorTelescopeScreen extends Screen
{
	Sector _sector;
	Telescope _telescope;
	Button _exitButton;
	Button _zoomOutButton;
	Button _nextFrequency;
	Button _previousFrequency;

	ArrayList<SignalSource> _signalSources;

	SectorTelescopeScreen(Sector sector, Telescope telescope)
	{
		_sector = sector;
		_telescope = telescope;
		_signalSources = sector.getSectorSignalSources();
		
		addButton(_nextFrequency = new FrequencyButton(true));
		addButton(_previousFrequency = new FrequencyButton(false));

		addButtonToToolbar(_zoomOutButton = new Button("缩小", 0, 0, 150, 50));
		addButtonToToolbar(_exitButton = new Button("退出", 0, 0, 150, 50));
	}

	public void onEnter()
	{
		noCursor();
	}

	public void onExit()
	{
		cursor();
	}

	public void update()
	{
		// don't want to update node buttons
		//_sector.update();
		_telescope.update(_signalSources);
	}

	public void renderBackground()
	{
		super.renderBackground();
		_sector.renderBackground();
	}

	public void render()
	{
		_sector.render();
		_telescope.render();
	}

	public void onButtonUp(Button button)
	{
		if (button == _exitButton)
		{
			gameManager.popScreen();
			gameManager.popScreen();
		}
		else if (button == _zoomOutButton)
		{
			gameManager.popScreen();
		}
		else if (button == _nextFrequency)
		{
			_telescope.nextFrequency();
		}
		else if (button == _previousFrequency)
	    {
	      _telescope.previousFrequency();
	    }
	}
}

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

    println("正在加载太阳系...");

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

  public void onReceiveGlobalMessage(Message message)
  {
    if (message.id.equals("spawn ship"))
    {
      timberHearth.addActor(ship, "村庄");

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
      feed.publish("你已抵达" + node.getDescription());
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
          feed.publish("你已被传送至" + teleportNode.getDescription());
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

  public boolean isPlayerInShip()
  {
    return(player.currentNode == ship.currentNode);
  }

  public Node getNodeByName(String nodeName)
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
  
  public Sector getSectorByName(SectorName sectorName)
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

class SolarSystemScreen extends Screen
{
  Actor _player;
  Actor _ship;
  SolarSystem _solarSystem;
    
  SectorButton _cometButton;
  SectorButton _hourglassButton_left;
  SectorButton _hourglassButton_right;
  SectorButton _timberButton;
  SectorButton _brittleButton;
  SectorButton _giantsButton;
  SectorButton _darkButton;
  QuantumMoonButton _quantumButton;

  SectorButton _focusSectorButton;

  ArrayList<SectorButton> _sectorButtons;
  
  SolarSystemScreen(SolarSystem solarSystem)
  {
    super();
    
    _solarSystem = solarSystem;
    _player = solarSystem.player;
    _ship = solarSystem.ship;

    _sectorButtons = new ArrayList<SectorButton>();
    float buttonHeight = height / 2;

    _sectorButtons.add(_hourglassButton_left = new HourglassTwinsButton(170, buttonHeight, false, _solarSystem.rockyTwin));
    _sectorButtons.add(_hourglassButton_right = new HourglassTwinsButton(170, buttonHeight, true, _solarSystem.sandyTwin));
    _sectorButtons.add(_timberButton = new TimberHearthButton(300, buttonHeight, _solarSystem.timberHearth));
    _sectorButtons.add(_brittleButton = new BrittleHollowButton(420, buttonHeight, _solarSystem.brittleHollow));
    _sectorButtons.add(_giantsButton = new GiantsDeepButton(580, buttonHeight, _solarSystem.giantsDeep));
    _sectorButtons.add(_darkButton = new DarkBrambleButton(810, buttonHeight, _solarSystem.darkBramble));
    _sectorButtons.add(_cometButton = new CometButton(600, buttonHeight + 200, _solarSystem.comet));

    SectorButton[] targets = {_hourglassButton_right, _timberButton, _brittleButton, _giantsButton};
    _sectorButtons.add(_quantumButton = new QuantumMoonButton(targets, _solarSystem.quantumMoon));

    for (int i = 0; i < _sectorButtons.size(); i++)
    {
      addButton(_sectorButtons.get(i));
    }
  }

  public void onEnter()
  {
    _quantumButton.collapse();
  }

  public void update() {}
  
  public void render()
  {
    // draw sun
    stroke(40, 100, 100);
    fill(0, 0, 0);
    //fill(20, 100, 100);
    ellipse(-430, height/2, 1000, 1000);

    if (_ship.currentSector == null)
    {
      _ship.render();
    }
    if (_player.currentSector == null)
    {
      _player.render();
    }
  }

  public void onButtonEnterHover(Button button)
  {
    if (_sectorButtons.contains(button))
    {
      _focusSectorButton = (SectorButton)button;
    }
  }

  public void onButtonExitHover(Button button)
  {
    if (button == _focusSectorButton)
    {
      _focusSectorButton = null;
    }
  }
  
  public void onButtonUp(Button button)
  { 
    /** SECTOR SELECTION **/
    if (_sectorButtons.contains(button))
    {
      selectSector(((SectorButton)button).getSector());
    }
  }
  
  public void selectSector(Sector selectedSector) {}

  public Vector2 getSectorScreenPosition(Sector sector)
  {
    for (int i = 0; i < _sectorButtons.size(); i++)
    {
      if (_sectorButtons.get(i).getSector() == sector)
      {
        return _sectorButtons.get(i).screenPosition;
      }
    }

    return null;
  }
}

class SolarSystemTelescopeScreen extends SolarSystemScreen
{
  Telescope _telescope;
  Button _exitButton;
  Button _nextFrequency;
  Button _previousFrequency;

  ArrayList<SignalSource> _signalSources;

  SolarSystemTelescopeScreen(SolarSystem solarSystem, Telescope telescope)
  {
    super(solarSystem);
    
    addButton(_nextFrequency = new FrequencyButton(true));
    addButton(_previousFrequency = new FrequencyButton(false));

    addButtonToToolbar(_exitButton = new Button("退出", 0, 0, 150, 50));

    _telescope = telescope;
  }

  public void onEnter()
  {
    super.onEnter();
    noCursor();

    // must do this after quantum moon collapses
    _signalSources = new ArrayList<SignalSource>();
    _signalSources.add(new SignalSource(_cometButton.screenPosition, _cometButton.getSector()));
    _signalSources.add(new SignalSource(_hourglassButton_left.screenPosition, _hourglassButton_left.getSector()));
    _signalSources.add(new SignalSource(_timberButton.screenPosition, _timberButton.getSector()));
    _signalSources.add(new SignalSource(_brittleButton.screenPosition, _brittleButton.getSector()));
    _signalSources.add(new SignalSource(_giantsButton.screenPosition, _giantsButton.getSector()));
    _signalSources.add(new SignalSource(_darkButton.screenPosition, _darkButton.getSector()));
    _signalSources.add(new SignalSource(_quantumButton.screenPosition, _quantumButton.getSector()));
  }

  public void onExit()
  {
    cursor();
  }

  public void update()
  {
    super.update();
    _telescope.update(_signalSources);
  }

  public void render()
  {
    super.render();
    _telescope.render();

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawName();
    }

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawZoomPrompt();
    }
  }

  public void selectSector(Sector selectedSector)
  {
    gameManager.loadSectorTelescopeView(selectedSector);
  }

  public void onButtonUp(Button button)
  {
    super.onButtonUp(button);

    if (button == _exitButton)
    {
      gameManager.popScreen();
    }
    else if (button == _nextFrequency)
    {
      _telescope.nextFrequency();
    }
    else if (button == _previousFrequency)
    {
      _telescope.previousFrequency();
    }
  }
}

class SolarSystemMapScreen extends SolarSystemScreen
{
  Button _databaseButton;
  Button _telescopeButton;

  SolarSystemMapScreen(SolarSystem solarSystem)
  {
    super(solarSystem);
    addButtonToToolbar(_databaseButton = new Button("查看数据库", 0, 0, 150, 50));
    addButtonToToolbar(_telescopeButton = new Button("扫描信号", 0, 0, 150, 50));
  }

  public void render()
  {
    super.render();

    if (_focusSectorButton != null)
    {
      _focusSectorButton.drawName();
    }

    feed.render();
    timeLoop.renderTimer();
  }

  public void onEnter()
  {
    super.onEnter();
    setActorPosition(_player);
    setActorPosition(_ship);
  }

  public void setActorPosition(Actor actor)
  {
    Vector2 idlePos = new Vector2(200, height - 200);

    // set player position
    if (actor.lastSector != null)
    {
      actor.setScreenPosition(getSectorScreenPosition(actor.lastSector));
    }
    else
    {
      actor.setScreenPosition(idlePos);
    }

    actor.moveToScreenPosition(idlePos);
    actor.lastSector = null;
  }

  public void update()
  {
    _player.update();
    _ship.update();
  }

  public void onButtonUp(Button button)
  {
    /** OPEN DATABASE **/
    if (button == _databaseButton)
    {
      gameManager.pushScreen(gameManager.databaseScreen);
    }
    else if (button == _telescopeButton)
    {
      gameManager.loadTelescopeView();
    }

    super.onButtonUp(button);
  }

  public void selectSector(Sector selectedSector)
  {
    Sector nextSector = selectedSector;
    
    // check if the player is already in a Sector...if not, fly to that Sector
    if (_solarSystem.ship.currentSector == null)
    {
      nextSector.addActor(_solarSystem.ship);
    }
    
    // check if the ship is already in a Sector...if not fly to that Sector
    if (_solarSystem.player.currentSector == null)
    {
      nextSector.addActor(_solarSystem.player);
    }

    gameManager.loadSector(nextSector);
    nextSector.onArrival();
  }
}

class StatusFeed implements GlobalObserver
{
  final int MAX_LINES = 3;
  
  ArrayList<StatusLine> _feed;

  public void init()
  {
    _feed = new ArrayList<StatusLine>();
    messenger.addObserver(this);
  }

  public void clear()
  {
    _feed.clear();
  }

  public void onReceiveGlobalMessage(Message message)
  {

  }

  public void publish(String newLine)
  {
    publish(newLine, false);
  }

  public void publish(String newLine, boolean important)
  {
    _feed.add(new StatusLine(newLine, important));

    if (_feed.size() > MAX_LINES)
    {
      _feed.remove(0);
    }
  }
  
  public void render()
  {
    for (int i = 0; i < _feed.size(); i++)
    {
      if (!_feed.get(i).draw(20, 30 + i * 25))
      {
        break; // break if the current line hasn't finished displaying
      }
    }
  }
}

class StatusLine
{
  String _line;
  float _initTime;
  boolean _displayTriggered = false;
  int _lineColor = color(0, 0, 100);

  final float SPEED = 0.08f;

  StatusLine(String newLine, boolean important)
  {
    _line = newLine;

    if (important)
    {
      _lineColor = color(100, 100, 100);
    }
  }

  public boolean draw(float x, float y)
  {
    if (!_displayTriggered)
    {
      _initTime = millis();
      _displayTriggered = true;
    }

    textAlign(LEFT);
    textFont(mediumFont);
    textSize(18);
    fill(_lineColor);
    text(_line.substring(0, min(_line.length(), getDisplayLength())), x, y);
    textFont(smallFont);

    // is this line fully displayed?
    if (_line.length() <= getDisplayLength())
    {
      return true;
    }
    return false;
  }

  public int getDisplayLength()
  {
    return (int)((millis() - _initTime) * SPEED);
  }
}

class SupernovaScreen extends Screen
{
  float initTime = 0;
  float collapseTime;
  float supernovaTime;
  float collapsePercent = 0;
  float supernovaPercent = 0;

  final float SUN_SIZE = 300;
  final float COLLAPSE_DURATION = 2000;

  final float SUPERNOVA_SIZE = 2000;
  final float SUPERNOVA_DURATION = 2000;

  SupernovaScreen()
  {
    super();
  }
  
  public void onEnter()
  {
    initTime = millis();
    collapseTime = initTime + 1 * 500;
    supernovaTime = collapseTime + COLLAPSE_DURATION;

    feed.clear();
    feed.publish("太阳正在演化为超新星！", true);
  }
  
  public void onExit() {}
  
  public void update()
  {
    collapsePercent = constrain((millis() - collapseTime) / COLLAPSE_DURATION, 0, 1);
    collapsePercent = collapsePercent * collapsePercent * collapsePercent;

    supernovaPercent = constrain((millis() - supernovaTime) / SUPERNOVA_DURATION, 0, 1);
    supernovaPercent = supernovaPercent * supernovaPercent;

    if (supernovaPercent == 1)
    {
      playerData.killPlayer();
    }
  }
  
  public void render()
  {
    pushMatrix();
    translate(width/2, height/2);

      // draw supernova
      noStroke();
      fill(300 * supernovaPercent, 100, 100);
      ellipse(0, 0, 5 + SUPERNOVA_SIZE * supernovaPercent, 5 + SUPERNOVA_SIZE * supernovaPercent * (1 - supernovaPercent * 0.5f));

      // draw sun
      if (collapsePercent < 1)
      {
        stroke(40, 100, 100);
        fill(0, 0, 0);
        ellipse(0, 0, SUN_SIZE * (1 - collapsePercent), SUN_SIZE * (1 - collapsePercent));
      }

    popMatrix();

    feed.render();
  }
  
  public void onButtonUp(Button button) {}
}

class FlashbackScreen extends Screen
{
  float initTime = 0;
  float lastSpawnTime = 0;
  float flashbackPercent = 0;

  FloatList _ringSizes;

  final float FLASHBACK_DURATION = 2200;

  FlashbackScreen()
  {
    super();

    _ringSizes = new FloatList();
  }
  
  public void onEnter()
  {
    initTime = millis();

    feed.clear();
    feed.publish("?!gnineppah s'tahW", true);
  }
  
  public void onExit()
  {

  }
  
  public void update()
  {
    if (millis() - lastSpawnTime > 50 && random(1) > 0.3f)
    {
      lastSpawnTime = millis();
      _ringSizes.append(5.0f);
    }

    for (int i = 0; i < _ringSizes.size(); i++)
    {
      _ringSizes.set(i, _ringSizes.get(i) + _ringSizes.get(i) * 0.1f + 0.5f);
    }

    if (getFlashBackPercent() == 1)
    {
      gameManager.resetTimeLoop();
    }
  }

  public float getFlashBackPercent()
  {
    return constrain((millis() - initTime) / FLASHBACK_DURATION, 0, 1);
  }
  
  public void render()
  {
    pushMatrix();
    translate(width/2, height/2);

      for (int i = 0; i < _ringSizes.size(); i++)
      {
        stroke(0, 0, 100);
        noFill();
        ellipse(0, 0, _ringSizes.get(i), _ringSizes.get(i));
      }

    popMatrix();

    float fadeAlpha = constrain((getFlashBackPercent() - 0.9f) / 0.1f, 0, 1);
    fill(0, 0, 100, fadeAlpha * 100);
    rectMode(CORNER);
    rect(0, 0, width, height);

    feed.render();
  }
  
  public void onButtonUp(Button button) {}
}

class GameOverScreen extends Screen
{
  GameOverScreen()
  {
    super();
    addButtonToToolbar(new Button("再次尝试"));
  }

  public void update() {}

  public void render()
  {
    fill(0, 90, 90);
    textAlign(CENTER, CENTER);
    textFont(titleFont);
    text("You Are Dead", width/2, height/2);
    
    fill(0, 0, 100);
    textFont(largeFont);
    text("你死了", width/2, height/2 + 70);
  }

  public void onButtonUp(Button button)
  {
    gameManager.resetTimeLoop();
  }
}
class FrequencyButton extends Button
{
	boolean _rightFacing = true;

	FrequencyButton(boolean rightFacing)
	{
		super("切换频率", width/2 + 140, 40, 50, 50);

		_rightFacing = rightFacing;

		if (!rightFacing)
		{
			setScreenPosition(new Vector2(width/2 - 140, 40));
		}
		//_disabledPrompt = "Switch Frequency\n(none known)";
	}

	public void drawShape()
	{
		pushMatrix();
		translate(screenPosition.x, screenPosition.y);

		if (_rightFacing)
		{
			triangle(25, 0, 0, 15, 0, -15);
		}
		else
		{
			triangle(-25, 0, 0, 15, 0, -15);
		}

		popMatrix();
	}

	public void drawText(float alpha) {}
}

class Telescope
{
	Frequency _frequency = Frequency.TRAVELER;
	float _signalStrength;

	public void update(ArrayList<SignalSource> signalSources)
	{
		_signalStrength = 0;

		for (int i = 0; i < signalSources.size(); i++)
		{
			_signalStrength += signalSources.get(i).getSignalStrength(new Vector2(mouseX, mouseY), _frequency);
		}
	}

	public void render()
	{
		String frequencyText = frequencyToString(_frequency);

		if (!playerData.knowsFrequency(_frequency))
		{
			if (_frequency == Frequency.BEACON)
			{
				frequencyText = "未知频率 001";
			}
		    else if (_frequency == Frequency.QUANTUM)
		    {
		    	frequencyText = "未知频率 002";
		    }
		}

		float xPos = width/2;

		fill(0, 0, 100);
		textSize(18);
		textFont(mediumFont);
		text(frequencyText, xPos, 40);
		textFont(smallFont);

		// draw signal feedback
		stroke(0, 0, 100);
		fill(200, 100, 100);
		rectMode(CENTER);
		rect(xPos, 70, max(0, _signalStrength * 300 - random(50)), 7);

		// draw crosshairs
	    noFill();
	    stroke(0, 0, 100);
	    line(mouseX, 0, mouseX, height);
	    line(0, mouseY, width, mouseY);
	}

	public void nextFrequency()
	{
		int index = (_frequency.ordinal() + 1) % Frequency.values().length;
		_frequency = Frequency.values()[index];

		// if (!playerData.knowsFrequency(_frequency))
		// {
		// 	nextFrequency();
		// }
	}

	public void previousFrequency()
	{
		int index = _frequency.ordinal() - 1;

		if (index < 0)
		{
			index = Frequency.values().length - 1;
		}

		_frequency = Frequency.values()[index];

		// if (!playerData.knowsFrequency(_frequency))
		// {
		// 	previousFrequency();
		// }
	}
}

class SignalSource extends Entity
{
	ArrayList<Signal> _signals;

	SignalSource(Node node)
	{
		setScreenPosition(node.screenPosition);
		_signals = new ArrayList<Signal>();
		_signals.add(node.getSignal());
	}

	SignalSource(Vector2 screenPosition, Sector sector)
	{
		setScreenPosition(screenPosition);
		_signals = sector.getSectorSignals();
	}

	public void addSignal(Signal signal)
	{
		_signals.add(signal);
	}

	public float getSignalStrength(Vector2 telescopePos, Frequency frequency)
	{
		if (hasSignalWithFrequency(frequency))
		{
			Vector2 d = telescopePos.sub(screenPosition);
			float dist = d.magnitude();

			float u = max(0, 1 - (dist / 150));
			return u * u * u;
		}
		return 0;
	}

	public boolean hasSignalWithFrequency(Frequency frequency)
	{
		for (int i = 0; i < _signals.size(); i++)
		{
			if (_signals.get(i).frequency == frequency)
			{
				return true;
			}
		}
		return false;
	}
}

class Signal
{
	Frequency frequency;

	Signal(String signalType)
	{
		if (signalType.equals("QUANTUM"))
		{
			frequency = Frequency.QUANTUM;
		}
		else if (signalType.equals("BEACON"))
		{
			frequency = Frequency.BEACON;
		}
		else if (signalType.equals("TRAVELER"))
		{
			frequency = Frequency.TRAVELER;
		}
	}
}

class TimeLoop implements GlobalObserver
{
	final int ACTION_POINTS_PER_LOOP = 15;
	int _actionPoints;

	boolean _isTimeLoopEnabled;
	boolean _triggerSupernova;

	public void init()
	{
		_actionPoints = ACTION_POINTS_PER_LOOP;
		_isTimeLoopEnabled = true;
		_triggerSupernova = false;
		
		feed.publish("你在村庄发射塔下的篝火旁醒来。今天是个大日子！");
		feed.publish("在天上，你注意到一个明亮的物体正从深巨星飞离...", true);

		messenger.addObserver(this);
	}

	public void onReceiveGlobalMessage(Message message)
	{
		if (message.id.equals("关闭时间循环") && _isTimeLoopEnabled)
		{
			_isTimeLoopEnabled = false;
			feed.publish("你关闭了时间循环装置", true);
		}
	}

	public void lateUpdate()
	{
		if (_triggerSupernova)
		{
			_triggerSupernova = false;
			gameManager.swapScreen(new SupernovaScreen());
		}
	}

	public boolean getEnabled()
	{
		return _isTimeLoopEnabled;
	}

	public float getLoopPercent()
	{
		return (float)(ACTION_POINTS_PER_LOOP - _actionPoints) / (float)ACTION_POINTS_PER_LOOP;
	}

	public int getActionPoints()
	{
		return _actionPoints;
	}

	public void waitFor(int minutes)
	{
		feed.publish("你等待了 1 分钟", true);
		spendActionPoints(minutes);
	}

	public void spendActionPoints(int points)
	{
		if (playerData.isPlayerAtEOTU()) {return;}
		
		int lastActionPoints = _actionPoints;

		_actionPoints = max(0, _actionPoints - points);
		messenger.sendMessage("action points spent");

		// detect when you have 1/4 your action points remaining
		if (lastActionPoints > ACTION_POINTS_PER_LOOP * 0.25f && _actionPoints <= ACTION_POINTS_PER_LOOP * 0.25f)
		{
			feed.publish("你注意到太阳正变得又大又红，十分糟糕", true);
		}

		if (_actionPoints == 0)
		{
			_triggerSupernova = true;
		}
	}

	public void renderTimer()
	{
		if (playerData.isPlayerAtEOTU()) {return;}

		float r = 50;
		float x = 50;
		float y = height - 50;

		stroke(0, 0, 100);
		fill(0, 0, 0);
		ellipse(x, y, r, r);
		fill(30, 100, 100);
		arc(x, y, r, r, 0 - PI * 0.5f + TAU * getLoopPercent(), 1.5f * PI);
		// fill(0, 0, 100);
		// textSize(20);
		// textAlign(RIGHT, TOP);
		// text("Time Remaining: " + _actionPoints + " min", width - 25, 25);
	}
}

class TitleScreen extends Screen
{
  TitleScreen()
  {
    super();
    addButton(new Button("新游戏", width/2 - 110, height - 50, 200, 50));
    addButton(new Button("退出", width/2 + 110, height - 50, 200, 50));
  }
  
  public void onEnter()
  {
    if (SKIP_TITLE)
    {
      gameManager.loadSector(SectorName.TIMBER_HEARTH);
      return;
    }
    
    AudioManager.play(SoundLibrary.kazooTheme);
  }
  
  public void onExit()
  {
    AudioManager.pause();
  }
  
  public void update()
  {
  }
  
  public void render()
  {
    fill(142, 90, 90);
    textAlign(CENTER, CENTER);
    textFont(titleFont);
    text("Outer Wilds", width/2, height/2 - 50);
    
    fill(0, 0, 100);
    textFont(largeFont);
    text("一场惊心动魄的图形文本冒险", width/2, height/2 + 50);
  }
  
  public void onButtonUp(Button button)
  {
    if (button.id == "新游戏")
    {
      gameManager.loadSector(SectorName.TIMBER_HEARTH);
    }
    else if (button.id == "退出")
    {
      exit();
    }
  }
}

class EndScreen extends Screen
{
  EndScreen()
  {
    super();
    addButton(new Button("退出", width/2, height - 50, 200, 50));
  }
  
  public void onEnter()
  {
    AudioManager.play(SoundLibrary.kazooTheme);
  }
  
  public void onExit()
  {
    AudioManager.pause();
  }
  
  public void update()
  {
  }
  
  public void render()
  {
    fill(142, 90, 90);
    textAlign(CENTER, CENTER);
    textFont(titleFont);
    text("Outer Wilds", width/2, height/2 - 50);
    
    fill(0, 0, 100);
    textFont(largeFont);
    text("感谢游玩！", width/2, height/2 + 50);
  }
  
  public void onButtonUp(Button button)
  {
    if (button.id == "退出")
    {
      exit();
    }
  }
}

public class Vector2
{
  float x;
  float y;
  
  Vector2(){
    x = y = 0;
  }

  Vector2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  Vector2(Vector2 vec)
  {
    this.assign(vec);
  }

  public void assign(Vector2 vec)
  {
    x = vec.x;
    y = vec.y;
  }
  
  public String toString(){
    return "(" + x + ", " + y + ")";
  }
  
  public float dist(Vector2 v){
    return v.sub(this).magnitude();
  }
  
  public Vector2 add(Vector2 v){
    return new Vector2(this.x + v.x, this.y + v.y);
  }
  
  public Vector2 sub(Vector2 v){
    return new Vector2(this.x - v.x, this.y - v.y);
  }
  
  public Vector2 mult(float value){
    return new Vector2(this.x * value, this.y * value);
  }
  
  public Vector2 scale(float value){
    this.x *= value;
    this.y *= value;
    return this;
  }
  
  public float magnitude(){
    return max(sqrt(x*x+y*y), 0.001f);
  }
  
  public Vector2 normalize(){
    float mag = magnitude();
    x /= mag;
    y /= mag;
    return this;
  }
  
  public Vector2 normalized(){
    float mag = magnitude();
    return new Vector2(x/mag, y/mag);
  }
  
  public float theta(){
    return (float)Math.atan2(y, x);
  }
  
  public float dx(){
    return x/magnitude();
  }
  
  public float dy(){
    return y/magnitude();
  }
  
  public Vector2 leftNormal(){
    return new Vector2(y,-x);
  }
  public Vector2 rightNormal(){
    return new Vector2(-y,x);
  }
  
  public float dot(Vector2 v1){
    return(this.x * v1.x + this.y * v1.y);
  }
  
  public float scaledDot(Vector2 v1){
    return(this.x * v1.dx() + this.y * v1.dy());
  }
  
  public Vector2 projectOnto(Vector2 v2){
    float dot = this.scaledDot(v2);
    return new Vector2(dot * v2.dx(), dot * v2.dy());
  }
}
  public void settings() {  size(960, 720);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "OuterWilds_TextAdventure" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
