
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

		addButtonToToolbar(_databaseButton = new Button("Use Database", 0, 0, 150, 50));
		addButtonToToolbar(_waitButton  = new Button("Wait [1 min]", 0, 0, 150, 50));
		addButtonToToolbar(_backButton = new Button("Continue", 0, 0, 150, 50));

		_exploreData.parseJSON();
	}

	void update(){}

	void renderBackground() {}

	void render()
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

	void onEnter() {}

	void onExit() {}

	void onInvokeClue(Clue clue)
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
			feed.publish("that doesn't help you right now", true);
		}
	}

	void onButtonUp(Button button)
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

	void onButtonEnterHover(Button button){}
	void onButtonExitHover(Button button){}
}