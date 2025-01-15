
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

	void initButtons()
	{
		addButtonToToolbar(_nextButton = new Button("Continue", 0, 0, 150, 50));
	}

	void addDatabaseButton()
	{
		addButtonToToolbar(_databaseButton = new Button("Use Database", 0, 0, 150, 50));
	}

	void addContinueButton()
	{
		addButtonToToolbar(_nextButton = new Button("Continue", 0, 0, 150, 50));
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
			text(getDisplayText(), 10, 10, BOX_WIDTH - 20, BOX_HEIGHT - 20);

		popMatrix();

		feed.render();
		timeLoop.renderTimer();
	}

	void onButtonUp(Button button)
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

	void onInvokeClue(Clue clue) {}

	abstract String getDisplayText();

	abstract void onContinue();

	void onEnter(){}

	void onExit(){}
}

class DeathByAnglerfishScreen extends EventScreen
{
	void onEnter()
	{
		feed.clear();
		feed.publish("you are eaten by an anglerfish", true);
	}

	String getDisplayText()
	{
		return "As you fly towards the shining light, you realize it's actually the lure of a huge anglerfish!\n\nYou try to turn around, but it's too late - the anglerfish devours you in a single bite.\n\nThe world goes black...";
	}

	void onContinue()
	{
		playerData.killPlayer();
	}
}

class DiveAttemptScreen extends EventScreen
{
	void onEnter()
	{
		feed.clear();
		feed.publish("you try to dive underwater", true);
	}

	String getDisplayText()
	{
		return "You try to dive underwater, but a strong surface current prevents you from submerging more than a few hundred meters.";
	}

	void onContinue()
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

	void onExit()
	{
		feed.clear();
		feed.publish("you are teleported to a new location", true);
	}

	String getDisplayText()
	{
		return _text;
	}

	void onContinue()
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

	String getDisplayText()
	{
		return _text;
	}

	void onContinue()
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

	void onEnter()
	{
		feed.clear();
		feed.publish("you arrive at " + _sectorName);
	}

	String getDisplayText()
	{
		return _arrivalText;
	}

	void onContinue()
	{
		gameManager.popScreen();
	}
}

class QuantumArrivalScreen extends EventScreen
{
	int _screenIndex = 0;
	boolean _photoTaken = false;

	void initButtons()
	{
		addDatabaseButton();
		addContinueButton();
	}

	String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			if (!_photoTaken)
			{
				return "As you approach the Quantum Moon, a strange mist starts to obscure your vision...";
			}
			else
			{
				return "You use your probe to take a snapshot of the moon mere moments before it's utterly obscured by the encroaching mist.";
			}
		}
		else if (_screenIndex == 1)
		{
			if (!_photoTaken)
			{
				return "The mist completely engulfs your ship, then dissipates as suddenly as it appeared.\n\nYou scan your surroundings, but the Quantum Moon has mysteriously vanished.";
			}
			else
			{
				return "You plunge into the mist, making sure to keep an eye on the photo you took just moments before.\n\nSuddenly a huge shape materializes out of the mist...you've made it to the surface of the Quantum Moon!";
			}
		}
		return "";
	}

	void onInvokeClue(Clue clue)
	{
		if (clue.id.equals("QM_1"))
		{
			gameManager.popScreen();
			_photoTaken = true;
			_databaseButton.enabled = false;
		}
		else
		{
			feed.publish("that doesn't help you right now", true);
		}
	}

	void onContinue()
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
			_displayText = "You turn off your flashlight and the world becomes pitch black.\n\nWhen you turn the flashlight back on, your surroundings have changed...";
		}
		else
		{
			_displayText = "You climb on top of the quantum rock and turn off your flashlight. It's so dark you can't even see your hand in front of your face.\n\nWhen you turn the flashlight back on, you're still standing on top of the quantum rock, but your surrounding have changed...";
		}
	}

	void onEnter()
	{
		feed.clear();
		feed.publish("you become entangled with the quantum object");
	}

	String getDisplayText()
	{
		return _displayText;
	}

	void onContinue()
	{
		gameManager.popScreen();
	}
}

class FollowTheVineScreen extends EventScreen
{
	int _screenIndex = 0;
	boolean _silentRunning = false;

	void initButtons()
	{
		addDatabaseButton();
		addContinueButton();
	}

	String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "You launch your probe at one of the strangely-bluish flowers and it is promptly ingested. You follow your probe's tracking signal as the vine's vascular system carries it deep into the heart of Dark Bramble.\n\nYou're so focused on following the probe's signal, you don't notice the glowing lures until it's too late. You've flown straight into a nest of anglerfish!";
		}
		else if (_screenIndex == 1)
		{
			if (!_silentRunning)
			{
				return "You reverse your thrusters, but it's too late. The anglerfish lunge with terrifying speed and devour your spacecraft whole.";
			}
			else
			{
				return "Suddenly remembering the rules to an ancient children's game, you switch off your engines and silently drift into the nest.\n\nNone of the anglerfish seem to notice, and you safely reach the other side. You resume following the probe's signal, and it's not long before you reach an ancient derelict tangled in the roots of Dark Bramble.";
			}
		}
		return "";
	}

	void onButtonUp(Button button)
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

	void onInvokeClue(Clue clue)
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
			feed.publish("that doesn't help you right now", true);
		}
	}

	void onContinue()
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
				messenger.sendMessage(new Message("move to", "Ancient Vessel"));
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
		_displayText = "You explore the derelict and eventually find the bridge. Although the ship's life support systems are dead, a few computer terminals are still running on some sort of auxiliary power. You find records of the original signal the Nomai received from the Eye of the Universe. According to their analysis, whatever the signal came from must be older than the Universe itself!\n\nYou poke around a bit more and discover that the ship's warp drive finished recharging a few hundred years ago.";
	}

	void initButtons()
	{
		addButtonToToolbar(_warpButton = new Button("Use Warp Drive", 0, 0, 150, 50));
		super.initButtons();
	}

	String getDisplayText()
	{
		return _displayText;
	}

	void onButtonUp(Button button)
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
	    			feed.publish("the ancient vessel warps to the coordinates of the Eye of the Universe");
	    		}
	    		else
	    		{
	    			_displayText = "You input the coordinates to the Eye of the Universe and try to use the warp drive, but it refuses to activate due to the presence of a 'massive temporal distortion'.";
	    		}
	    	}
	    	else
	    	{
	    		_displayText = "You try to use the warp drive, but apparently it won't activate without destination coordinates.";
	    	}
	    }
	    else if (button == _nextButton)
	    {
			onContinue();
	    }
	}

	void onContinue()
	{
		gameManager.popScreen();
	}
}

class TimeLoopCentralScreen extends EventScreen
{
	int _screenIndex = 0;
	Button _yes;
	Button _no;

	void initButtons()
	{
		addContinueButton();
	}

	String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "You're inside a huge spherical chamber at the center of the sandy Hourglass Twin. The two giant antennae you saw outside extend below the surface and converge within an elaborate coil-like piece of Nomai technology at the center of the chamber. This must be the source of the time loop!\n\nYou discover a transmitter that automatically tells the Nomai space station in orbit around Giant's Deep to launch a probe at the start of each loop.\n\nThe time loop machine requires the energy from a supernova to alter the flow of time. The Nomai tried to artificially make the sun go supernova thousands of years ago, but their attempts were unsuccessful.";
		}

		return "You eventually find a way to the center of the chamber and find what looks like the interface to the Time Loop Machine.\n\nDo you want to disable the time loop?";
	}

	void onButtonUp(Button button)
	{
		super.onButtonUp(button);
		
		if (button == _yes)
		{
			messenger.sendMessage("disable time loop");
			gameManager.popScreen();
		}
		else if (button == _no)
		{
			gameManager.popScreen();
		}
	}

	void onContinue()
	{
		_screenIndex++;
		removeButtonFromToolbar(_nextButton);
		addButtonToToolbar(_yes = new Button("Yes"));
		addButtonToToolbar(_no = new Button("No"));
	}
}

class EyeOfTheUniverseScreen extends EventScreen
{
	int _screenIndex = 0;

	String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "As you apprach the strange energy cloud surrounding the Eye of the Universe, you see the last few stars die in the distance. The Universe has become a pitch black void of nothingness.\n\nThe cloud dissipates as you reach its center, revealing a strange spherical object floating in space. Beneath its shimmering surface you see billions of tiny lights. As you get closer, you realize these lights resemble clusters of stars and galaxies. Every time you look away from the sphere and back again, the configuration of stars and galaxies changes.\n\nYou fire your jetpack thrusters and move into the sphere itself...";
		}

		return "For the briefest moment you find yourself floating in a sea of stars and galaxies. Suddenly all of the stars collapse into a single point of light directly in front of you. Nothing happens for several seconds, then the point of light explodes outwards in a spectacular burst of energy. The shockwave slams into you and sends you careening away into space.\n\nYour life-support systems are failing, but you watch as energy and matter are spewed into space in all directions.\n\nAfter a while, your vision fades to black.";
	}

	void onContinue()
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

	void initButtons()
	{
		addDatabaseButton();
		addButtonToToolbar(_yes = new Button("Yes"));
		addButtonToToolbar(_no = new Button("No"));
	}

	String getDisplayText()
	{
		if (_screenIndex == 0)
		{
			return "You explore the outer perimeter of Dark Bramble, where the tips of its thorny vines end in massive alien-looking white flowers (along with a few bluish ones).\n\nYou notice a small opening near the center of each flower...do you want to move in for a closer look?";
		}

		return "As you approach, the flower opens and a strange force starts to pull you in. You desperately try to escape, but it's no use.\n\nYou are unceremoniously devoured by a giant space flower. You can hear yourself being digested as the world goes black...";
	}

	void onButtonUp(Button button)
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

	void onInvokeClue(Clue clue)
	{
		if (clue.id.equals("D_3"))
		{
			gameManager.popScreen();
			messenger.sendMessage("follow the vine");
		}
		else
		{
			feed.publish("that doesn't help you right now", true);
		}
	}

	void onContinue()
	{
		playerData.killPlayer();
	}
}