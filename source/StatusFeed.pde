
class StatusFeed implements GlobalObserver
{
  final int MAX_LINES = 3;
  
  ArrayList<StatusLine> _feed;

  void init()
  {
    _feed = new ArrayList<StatusLine>();
    messenger.addObserver(this);
  }

  void clear()
  {
    _feed.clear();
  }

  void onReceiveGlobalMessage(Message message)
  {

  }

  void publish(String newLine)
  {
    publish(newLine, false);
  }

  void publish(String newLine, boolean important)
  {
    _feed.add(new StatusLine(newLine, important));

    if (_feed.size() > MAX_LINES)
    {
      _feed.remove(0);
    }
  }
  
  void render()
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
  color _lineColor = color(0, 0, 100);

  final float SPEED = 0.08f;

  StatusLine(String newLine, boolean important)
  {
    _line = newLine;

    if (important)
    {
      _lineColor = color(100, 100, 100);
    }
  }

  boolean draw(float x, float y)
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

  int getDisplayLength()
  {
    return (int)((millis() - _initTime) * SPEED);
  }
}