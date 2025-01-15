class QuantumNode extends Node
{
	QuantumNode(String name, JSONObject nodeJSON)
	{
		super(name, nodeJSON);
	}

	void updateQuantumStatus(int quantumState)
	{
		boolean visible = _nodeJSONObj.getInt("quantum location") == quantumState;
		setVisible(visible);

		// hide connections
		if (!visible)
		{
			for (NodeConnection connection : _connections.values()) 
		    {
		    	connection.setVisible(visible);
		    }
		}
	}

	boolean allowQuantumEntanglement()
	{
		return _nodeJSONObj.getInt("quantum location") == locator.getQuantumMoonLocation() && _nodeJSONObj.getBoolean("entanglement node", false);
	}
}