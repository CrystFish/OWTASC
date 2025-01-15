
class ExploreData
{
	Node _node;
	String _exploreString;
	JSONArray _exploreArray;

	JSONObject _nodeObj;
	JSONObject _activeExploreObj;

	boolean _dirty;

	ExploreData(Node node, JSONObject nodeObj)
	{
		_node = node;
		_nodeObj = nodeObj;
	}

	void parseJSON()
	{
	    // parse as string
	    _exploreString = _nodeObj.getString("explore", "Nothing to see here!");
	    _exploreArray = new JSONArray();

	    // parse as explore object
	    if (_exploreString.charAt(0) == '{')
	    {
	    	_activeExploreObj = _nodeObj.getJSONObject("explore");
	    }
	    // parse as array of explore objects
	    else if (_exploreString.charAt(0) == '[')
	    {
	    	_exploreArray = _nodeObj.getJSONArray("explore");
	    	_activeExploreObj = _exploreArray.getJSONObject(0);
	    }

	    _dirty = true;
	}

	void updateActiveExploreData()
	{
		// check wait times
		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	int turnCycle = exploreObj.getInt("turn cycle", 1);
			int turn = timeLoop.getActionPoints() % turnCycle;

			if (exploreObj.getInt("on turn", -1) == turn && exploreObj != _activeExploreObj)
			{
				_activeExploreObj = exploreObj;
				_dirty = true;
			}
	    }
	}

	boolean canClueBeInvoked(String clueID)
	{
		if (clueID.equals("QM_2") && _node.allowQuantumEntanglement())
		{
			return true;
		}

		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	if (exploreObj.getString("require clue", "").equals(clueID) && exploreObj != _activeExploreObj)
	    	{
	    		return true;
	    	}

	    	// NO LONGER IN USE
	    	if (exploreObj.hasKey("clue event") && exploreObj.getJSONObject("clue event").getString("clue id").equals(clueID))
	    	{
	    		return true;
	    	}
	    }
	    return false;
	}

	void invokeClue(String clueID)
	{
		if (clueID.equals("QM_2") && _node.allowQuantumEntanglement())
		{
			gameManager.popScreen();
			messenger.sendMessage("quantum entanglement");
		}

		for(int i = 0; i < _exploreArray.size(); i++)
	    {
	    	JSONObject exploreObj = _exploreArray.getJSONObject(i);

	    	// unlock explore screens
	    	if (exploreObj.getString("require clue", "").equals(clueID) && exploreObj != _activeExploreObj)
	    	{
	    		_activeExploreObj = exploreObj;
	    		_dirty = true;
	    	}

	    	// NO LONGER IN USE
	    	// fire clue events
	    	if (exploreObj.hasKey("clue event"))
	    	{
	    		String eventClueID = exploreObj.getJSONObject("clue event").getString("clue id");

	    		if (eventClueID.equals(clueID))
	    		{
	    			String eventID = exploreObj.getJSONObject("clue event").getString("event id");
		    		messenger.sendMessage(eventID);
	    		}
	    	}
	    }
	}

	void explore()
	{
		updateActiveExploreData(); // sets dirty flag if explore data has changed

		if (_dirty && _activeExploreObj != null)
		{
		    fireEvents(_activeExploreObj);
		    discoverClues(_activeExploreObj);
		    revealHiddenPaths(_activeExploreObj);
		    _dirty = false;
		}
	}

	void fireEvents(JSONObject exploreObj)
	{
		if (exploreObj.hasKey("fire event"))
		{
			messenger.sendMessage(exploreObj.getString("fire event"));
		}
		if (exploreObj.hasKey("move to"))
		{
			gameManager.swapScreen(new MoveToScreen(exploreObj.getString("text"), exploreObj.getString("move to")));
		}
		if (exploreObj.hasKey("teleport to"))
		{
			gameManager.swapScreen(new TeleportScreen(exploreObj.getString("text"), exploreObj.getString("teleport to")));
		}
	}

	void discoverClues(JSONObject exploreObj)
	{
		if (exploreObj.hasKey("discover clue"))
		{
			playerData.discoverClue(exploreObj.getString("discover clue"));
		}
	}

	void revealHiddenPaths(JSONObject exploreObj)
	{
		// reveal hidden paths
	    if (exploreObj.hasKey("reveal paths"))
	    {
	    	JSONArray pathArray = exploreObj.getJSONArray("reveal paths");

	    	for (int i = 0; i < pathArray.size(); i++)
	    	{
	    		_node.getConnection(pathArray.getString(i)).revealHidden();
	    	}

	    	feed.publish("you discover a hidden path!", true);
	    }
	}

	String getExploreText()
	{
		if (_activeExploreObj != null)
		{
			return _activeExploreObj.getString("text", "no explore text");
		}

		return _exploreString;
	}
}