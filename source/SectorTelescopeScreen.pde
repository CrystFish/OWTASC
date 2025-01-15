
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

		addButtonToToolbar(_zoomOutButton = new Button("Zoom Out", 0, 0, 150, 50));
		addButtonToToolbar(_exitButton = new Button("Exit", 0, 0, 150, 50));
	}

	void onEnter()
	{
		noCursor();
	}

	void onExit()
	{
		cursor();
	}

	void update()
	{
		// don't want to update node buttons
		//_sector.update();
		_telescope.update(_signalSources);
	}

	void renderBackground()
	{
		super.renderBackground();
		_sector.renderBackground();
	}

	void render()
	{
		_sector.render();
		_telescope.render();
	}

	void onButtonUp(Button button)
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