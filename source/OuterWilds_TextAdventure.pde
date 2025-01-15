import java.util.Map;

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

void setup()
{
  size(960, 720);
  noSmooth();
  colorMode(HSB, 360, 100, 100, 100);
  rectMode(CENTER);
  ellipseMode(CENTER);

  smallFont = loadFont("fonts/Consolas-14.vlw");
  mediumFont = loadFont("fonts/Consolas-18.vlw");
  
  smallFont();
    
  minim = new Minim(this);

  initGame();
}

void initGame()
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

void draw()
{
  gameManager.runGameLoop();

  // stroke(0, 0, 100);
  // fill(0, 0, 0);
  // triangle(mouseX, mouseY, mouseX, mouseY - 10, mouseX + 10, mouseY - 10);
  
  deltaMillis = millis() - lastMillis;
  lastMillis = millis();
  
  //println(deltaMillis);
}

String frequencyToString(Frequency frequency)
{
  switch(frequency)
  {
    case TRAVELER:
      return "Traveler Frequency";
    case BEACON:
      return "Distress Beacon";
    case QUANTUM:
      return "Quantum Fluctuations";
    default:
      return "";
  }
}

void smallFont()
{
  textSize(14);
  textFont(smallFont);
}

void mediumFont()
{
  textSize(18);
  textFont(mediumFont);
}