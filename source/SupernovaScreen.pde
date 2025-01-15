
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
  
  void onEnter()
  {
    initTime = millis();
    collapseTime = initTime + 1 * 500;
    supernovaTime = collapseTime + COLLAPSE_DURATION;

    feed.clear();
    feed.publish("the sun is going supernova!", true);
  }
  
  void onExit() {}
  
  void update()
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
  
  void render()
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
  
  void onButtonUp(Button button) {}
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
  
  void onEnter()
  {
    initTime = millis();

    feed.clear();
    feed.publish("?!gnineppah s'tahW", true);
  }
  
  void onExit()
  {

  }
  
  void update()
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

  float getFlashBackPercent()
  {
    return constrain((millis() - initTime) / FLASHBACK_DURATION, 0, 1);
  }
  
  void render()
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
  
  void onButtonUp(Button button) {}
}

class GameOverScreen extends Screen
{
  GameOverScreen()
  {
    super();
    addButtonToToolbar(new Button("Try Again"));
  }

  void update() {}

  void render()
  {
    fill(0, 90, 90);
    textAlign(CENTER, CENTER);
    textSize(100);
    text("You Are Dead", width/2, height/2);
  }

  void onButtonUp(Button button)
  {
    gameManager.resetTimeLoop();
  }
}