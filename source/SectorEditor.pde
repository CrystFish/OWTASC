
class SectorEditor implements NodeButtonObserver, ButtonObserver
{
  Sector _activeSector;
  Button _saveButton;
  
  Node _selection;
  boolean _dragging = false;
  
  SectorEditor(Sector sector)
  {
    _activeSector = sector;
    _saveButton = new Button("Save", width - 75, height - 50, 100, 50);
    _saveButton.setObserver(this);
  }
  
  void update()
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
  
  void render()
  {
    _saveButton.render();
  }
  
  void onButtonUp(Button button)
  {
    if (button == _saveButton)
    {
      _activeSector.saveSectorJSON();
    }
  }
  
  void onNodeGainFocus(Node node)
  {
    _selection = node;
  }
  
  void onNodeLoseFocus(Node node)
  {
    if (!_dragging)
    {
      _selection = null;
    }
  }

  void onTravelToNode(Node node){}
  void onExploreNode(Node node){}
  void onProbeNode(Node node){}
  
  void onButtonEnterHover(Button button){}
  void onButtonExitHover(Button button){}
  void onNodeSelected(Node node){}
}