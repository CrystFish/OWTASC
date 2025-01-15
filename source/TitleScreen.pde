
class TitleScreen extends Screen
{
  TitleScreen()
  {
    super();
    addButton(new Button("New Game", width/2 - 110, height - 50, 200, 50));
    addButton(new Button("Quit", width/2 + 110, height - 50, 200, 50));
  }
  
  void onEnter()
  {
    if (SKIP_TITLE)
    {
      gameManager.loadSector(SectorName.TIMBER_HEARTH);
      return;
    }
    
    AudioManager.play(SoundLibrary.kazooTheme);
  }
  
  void onExit()
  {
    AudioManager.pause();
  }
  
  void update()
  {
  }
  
  void render()
  {
    fill(142, 90, 90);
    textAlign(CENTER, CENTER);
    textSize(100);
    text("Outer Wilds", width/2, height/2 - 50);
    
    fill(0, 0, 100);
    textSize(22);
    text("a thrilling graphical text adventure", width/2, height/2 + 50);
  }
  
  void onButtonUp(Button button)
  {
    if (button.id == "New Game")
    {
      gameManager.loadSector(SectorName.TIMBER_HEARTH);
    }
    else if (button.id == "Quit")
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
    addButton(new Button("Exit", width/2, height - 50, 200, 50));
  }
  
  void onEnter()
  {
    AudioManager.play(SoundLibrary.kazooTheme);
  }
  
  void onExit()
  {
    AudioManager.pause();
  }
  
  void update()
  {
  }
  
  void render()
  {
    fill(142, 90, 90);
    textAlign(CENTER, CENTER);
    textSize(100);
    text("Outer Wilds", width/2, height/2 - 50);
    
    fill(0, 0, 100);
    textSize(22);
    text("thanks for playing!", width/2, height/2 + 50);
  }
  
  void onButtonUp(Button button)
  {
    if (button.id == "Exit")
    {
      exit();
    }
  }
}