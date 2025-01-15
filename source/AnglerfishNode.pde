class AnglerfishNode extends Node
{
	AnglerfishNode(String nodeName, JSONObject nodeJSONObj)
	{
		super(nodeName, nodeJSONObj);
		entryPoint = true;
		shipAccess = true;
		gravity = false;

		_visible = true;
	}

	String getKnownName()
	{
		if (_visited) return "Anglerfish";
		else return "???";
	} 

	String getDescription() 
	{
		return "an enormous hungry-looking anglerfish";
	}

	String getProbeDescription() 
	{
		return "a light shining through the fog";
	}

	boolean hasDescription() {return true;}

	boolean isProbeable() {return true;}

	boolean isExplorable() {return true;} // tricks graphics into rendering question mark

	void visit()
	{
		_visited = true;
		setVisible(true);

		messenger.sendMessage("death by anglerfish");

		if (_observer != null)
		{
			_observer.onNodeVisited(this);
		}
	}
}