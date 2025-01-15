import ddf.minim.spi.*;
import ddf.minim.signals.*;
import ddf.minim.*;
import ddf.minim.analysis.*;
import ddf.minim.ugens.*;
import ddf.minim.effects.*;

static class SoundLibrary
{
  static AudioPlayer kazooTheme;
  
  static void loadSounds()
  {
    println("Sounds loading...");
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
  
  static void play(AudioPlayer sound)
  {
    currentSound = sound;
    currentSound.play();
  }
  
  static void pause()
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