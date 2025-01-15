
interface GlobalObserver
{
	void onReceiveGlobalMessage(Message message);
}

class GlobalMessenger
{
	ArrayList<GlobalObserver> _observers;

	GlobalMessenger()
	{
		_observers = new ArrayList<GlobalObserver>();
	}

	void addObserver(GlobalObserver observer)
	{
		if (!_observers.contains(observer))
		{
			_observers.add(observer);
		}
		//println("Observer Count: " + _observers.size());
	}

	void removeObserver(GlobalObserver observer)
	{
		_observers.remove(observer);
	}

	void removeAllObservers()
	{
		_observers.clear();
	}

	void sendMessage(String messageID)
	{
		sendMessage(new Message(messageID));
	}

	void sendMessage(Message message)
	{
		for (int i = 0; i < _observers.size(); i++)
		{
			_observers.get(i).onReceiveGlobalMessage(message);
		}
	}
}

class Message
{
	String id;
	String text;

	Message(String messageID)
	{
		id = messageID;
	}

	Message(String messageID, String t)
	{
		this(messageID);
		text = t;
	}
}