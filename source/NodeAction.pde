interface NodeActionObserver
{
	void onExploreNode(Node node);
	void onProbeNode(Node node);
	void onTravelAttempt(boolean succeeded, Node node, NodeConnection connection);
}

abstract class NodeAction
{
	String _prompt = "";
	int _mouseButton = LEFT;
	NodeActionObserver _observer;

	void setObserver(NodeActionObserver observer)
	{
		_observer = observer;
	}

	int getMouseButton()
	{
		return _mouseButton;
	}

	void setMouseButton(int button)
	{
		_mouseButton = button;
	}

	abstract void execute();
	
	int getCost()
	{
		return 0;
	}

	String getPrompt()
	{
		return _prompt;
	}

	void setPrompt(String description)
	{
		if (_mouseButton == LEFT)
		{
			_prompt += "L-Clk - " + description;
		}
		else
		{
			_prompt += "R-Clk - " + description;
		}

		_prompt += " [" + getCost() + " min]";
	}
}

class ProbeAction extends NodeAction
{
	Actor _player;
	Node _location;

	ProbeAction(int button, Actor player, Node location, NodeActionObserver observer)
	{
		_player = player;
		_location = location;
		setMouseButton(button);
		setObserver(observer);
		setPrompt("probe");
	}

	void execute()
	{
		feed.publish("you see " + _location.getProbeDescription());

		Actor probe = new Probe();
		_player.currentSector.addActor(probe);
		probe.setScreenPosition(_player.screenPosition);
		probe.moveToNode(_location);

		_observer.onProbeNode(_location);
	}
}

class ExploreAction extends NodeAction
{
	Node _location;

	ExploreAction(int button, Node location, NodeActionObserver observer)
	{
		_location = location;
		setMouseButton(button);
		setObserver(observer);
		setPrompt("explore");
	}

	int getCost()
	{
		return 1;
	}

	void execute()
	{
		timeLoop.spendActionPoints(getCost());

		// prevent the action from happening if the sun's going to explode
		if (timeLoop.getActionPoints() == 0) 
		{
			return;
		}

		feed.clear();
		feed.publish("you explore the " + _location.getActualName());
		
		_observer.onExploreNode(_location);
		gameManager.pushScreen(new ExploreScreen(_location));
		_location.explore();
	}
}

class TravelAction extends NodeAction
{
	Actor _player;
	Actor _ship;
	Node _destination;

	TravelAction(int button, Actor player, Node destination, NodeActionObserver observer)
	{
		this(button, player, null, destination, observer);
	}

	TravelAction(int button, Actor player, Actor ship, Node destination, NodeActionObserver observer)
	{
		_ship = ship;
		_player = player;
		_destination = destination;
		setMouseButton(button);
		setObserver(observer);
		setPrompt();
	}

	void setPrompt()
	{
		if (_ship != null)
		{
			if (_ship.currentNode == null && _destination.gravity)
			{
				setPrompt("land here");
				return;
			}
			setPrompt("fly here");
		}
		else if (_destination.gravity)
		{
			setPrompt("hike here");
		}
		else
		{
			setPrompt("jetpack here");
		}
	}

	int getCost()
	{
		return 1;
	}

	void execute()
	{
		feed.clear();

		if (_player.currentNode != null)
		{
			NodeConnection connection = _destination.getConnection(_player.currentNode);

			if (connection != null)
			{	
				if (!connection.traversibleFrom(_player.currentNode))
				{
					connection.fireFailEvent();
					feed.publish(connection.getWrongWayText(), true);
					return;
				}

				connection.fireTraverseEvent();
				connection.traverse();

				if (connection.hasDescription())
				{
					feed.publish("you traverse " + connection.getDescription());
				}
			}
		}

		// publish feed first in case we want to override it (e.g. death-by-anglerfish scenario)
		if (_destination.hasDescription())
		{
			feed.publish("you reach " + _destination.getDescription());
		}

		if (_ship != null)
		{
			_ship.moveToNode(_destination);
		}
		
		messenger.sendMessage("reset reachability");
		_player.moveToNode(_destination);
		_observer.onTravelAttempt(true, _destination, _destination.getConnection(_player.currentNode));
		timeLoop.spendActionPoints(getCost());
	}
}