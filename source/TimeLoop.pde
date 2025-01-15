
class TimeLoop implements GlobalObserver
{
	final int ACTION_POINTS_PER_LOOP = 15;
	int _actionPoints;

	boolean _isTimeLoopEnabled;
	boolean _triggerSupernova;

	void init()
	{
		_actionPoints = ACTION_POINTS_PER_LOOP;
		_isTimeLoopEnabled = true;
		_triggerSupernova = false;
		
		feed.publish("You wake up next to a campfire near your village's launch tower. Today's the big day!");
		feed.publish("In the sky, you notice a bright object flying away from Giant's Deep...", true);

		messenger.addObserver(this);
	}

	void onReceiveGlobalMessage(Message message)
	{
		if (message.id.equals("disable time loop") && _isTimeLoopEnabled)
		{
			_isTimeLoopEnabled = false;
			feed.publish("you disable the time loop device", true);
		}
	}

	void lateUpdate()
	{
		if (_triggerSupernova)
		{
			_triggerSupernova = false;
			gameManager.swapScreen(new SupernovaScreen());
		}
	}

	boolean getEnabled()
	{
		return _isTimeLoopEnabled;
	}

	float getLoopPercent()
	{
		return (float)(ACTION_POINTS_PER_LOOP - _actionPoints) / (float)ACTION_POINTS_PER_LOOP;
	}

	int getActionPoints()
	{
		return _actionPoints;
	}

	void waitFor(int minutes)
	{
		feed.publish("you chill out for 1 minute", true);
		spendActionPoints(minutes);
	}

	void spendActionPoints(int points)
	{
		if (playerData.isPlayerAtEOTU()) {return;}
		
		int lastActionPoints = _actionPoints;

		_actionPoints = max(0, _actionPoints - points);
		messenger.sendMessage("action points spent");

		// detect when you have 1/4 your action points remaining
		if (lastActionPoints > ACTION_POINTS_PER_LOOP * 0.25f && _actionPoints <= ACTION_POINTS_PER_LOOP * 0.25f)
		{
			feed.publish("you notice the Sun is getting awfully big and red", true);
		}

		if (_actionPoints == 0)
		{
			_triggerSupernova = true;
		}
	}

	void renderTimer()
	{
		if (playerData.isPlayerAtEOTU()) {return;}

		float r = 50;
		float x = 50;
		float y = height - 50;

		stroke(0, 0, 100);
		fill(0, 0, 0);
		ellipse(x, y, r, r);
		fill(30, 100, 100);
		arc(x, y, r, r, 0 - PI * 0.5 + TAU * getLoopPercent(), 1.5 * PI);
		// fill(0, 0, 100);
		// textSize(20);
		// textAlign(RIGHT, TOP);
		// text("Time Remaining: " + _actionPoints + " min", width - 25, 25);
	}
}