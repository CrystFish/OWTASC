class PlayerData implements GlobalObserver
{
	boolean _knowsLaunchCodes;
	boolean _knowsSignalCoordinates;

	ArrayList<Clue> _clueList;
	ArrayList<Frequency> _knownFrequencies;

	int _knownClueCount = 0;

	// resets every loop
	boolean _isDead = false;

	PlayerData()
	{
		_knowsLaunchCodes = START_WITH_LAUNCH_CODES;
		_knowsSignalCoordinates = START_WITH_COORDINATES;

		_clueList = new ArrayList<Clue>();
		_knownFrequencies = new ArrayList<Frequency>();
		_knownFrequencies.add(Frequency.TRAVELER);

		if (START_WITH_SIGNALS)
		{ 
			_knownFrequencies.add(Frequency.BEACON);
			_knownFrequencies.add(Frequency.QUANTUM);
		}

		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_1", "Sunken Module", "The data-collection module broke off from the Nomai probe launcher and fell into the center of Giant's Deep."));
		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_2", "Rogue Tornadoes", "While most tornadoes on Giant's Deep have upward-moving winds, the tornadoes that rotate counterclockwise have strong downward winds."));
		_clueList.add(new Clue(Curiosity.ANCIENT_PROBE_LAUNCHER, "APL_3", "Jellyfish", "The jellyfish on Giant's Deep have a hollow body cavity just big enough to fit a person."));

		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_3", "The 5th Location", "The Quantum Moon sometimes visits a 5th location outside the Solar System."));
		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_1", "Quantum Snapshot", "Observing an image of a quantum object prevents it from moving just as effectively as directly observing the object itself."));
		_clueList.add(new Clue(Curiosity.QUANTUM_MOON, "QM_2", "Quantum Entanglement", "Ordinary objects in close proximity to a quantum object can become 'entangled' with it and begin to exhibit quantum attributes.\n\nEven lifeforms can become entangled so long as they are unable to observe themselves or their surroundings."));

		_clueList.add(new Clue(Curiosity.VESSEL, "D_1", "Lost Vessel", "The Nomai came to this Solar System in search of a mysterious signal they refer to as 'The Eye of the Universe'. The interstellar Vessel they traveled in is shipwrecked somewhere in Dark Bramble."));
		_clueList.add(new Clue(Curiosity.VESSEL, "D_2", "Children's Game", "Nomai children played a game that reenacted their people's escape from Dark Bramble. According to the rules, three players (the escape pods) had to sneak past a blindfolded player (the anglerfish) without being heard."));
		_clueList.add(new Clue(Curiosity.VESSEL, "D_3", "Tracking Device", "The Nomai Vessel crashed in the roots of Dark Bramble. The Nomai attempted to relocate the roots by inserting a tracking device into one of Dark Bramble's vines, but they were unable to penetrate the vine's tough exterior."));

		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_1", "Time Machine", "After Nomai researchers created a small-but-functional time loop device on Giant's Deep, they made plans to construct a full-sized version on the Sandy Hourglass Twin (provided they could generate enough energy to power it)."));
		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_2", "Warp Towers", "The Nomai created special towers that can warp anyone inside to a corresponding receiver platform. The towers only activate when you can see your destination through a slit in the top of the tower."));
		_clueList.add(new Clue(Curiosity.TIME_LOOP_DEVICE, "TLD_3", "Construction Project", "The Nomai excavated the Sandy Hourglass Twin to construct an enormous machine designed to harness the energy from a supernova.\n\nThe control center is located inside a hollow cavity at the center of the planet, but it is completely sealed off from the surface.\n\n"));
	}

	void init()
	{
		messenger.addObserver(this);
		_isDead = false;
	}

	void onReceiveGlobalMessage(Message message)
	{
		if (message.id.equals("learn launch codes") && !_knowsLaunchCodes)
		{
			_knowsLaunchCodes = true;
			feed.publish("learned launch codes", true);
			messenger.sendMessage("spawn ship");
		}
		else if (message.id.equals("learn signal coordinates") && !_knowsSignalCoordinates)
		{
			_knowsSignalCoordinates = true;
			feed.publish("learned signal coordinates", true);
		}
	}

	void killPlayer()
	{
		_isDead = true;
	}

	boolean isPlayerDead()
	{
		return _isDead;
	}

	boolean isPlayerAtEOTU()
	{
		return ((locator.player.currentSector == gameManager._solarSystem.quantumMoon && locator.getQuantumMoonLocation() == 4) || locator.player.currentSector == gameManager._solarSystem.eyeOfTheUniverse);
	}

	boolean knowsFrequency(Frequency frequency)
	{
		return _knownFrequencies.contains(frequency);
	}

	boolean knowsSignalCoordinates()
	{
		return _knowsSignalCoordinates;
	}

	void learnFrequency(Frequency frequency)
	{
		if (!knowsFrequency(frequency))
		{
			_knownFrequencies.add(frequency);
			feed.publish("frequency identified: " + frequencyToString(frequency), true);
		}
	}

	int getFrequencyCount()
	{
		return _knownFrequencies.size();
	}

	boolean knowsLaunchCodes()
	{
		return _knowsLaunchCodes;
	}

	Clue getClueAt(int i)
	{
		return _clueList.get(i);
	}

	int getClueCount()
	{
		return _clueList.size();
	}

	int getKnownClueCount()
	{
		return _knownClueCount;
	}

	void discoverClue(String id)
	{
		for (int i = 0; i < _clueList.size(); i++)
		{
			if (_clueList.get(i).id.equals(id) && !_clueList.get(i).discovered)
			{
				_clueList.get(i).discovered = true;
				_knownClueCount++;
				feed.publish("information added to database", true);
			}
		}
	}
}

class Clue
{
	String id;
	String name;
	String description;
	boolean discovered;
	boolean invoked = false;
	Curiosity curiosity;

	Clue(Curiosity curiosity, String id, String name, String description)
	{
		this.curiosity = curiosity;
		this.id = id;
		this.name = name;
		this.description = description;
		this.discovered = false || START_WITH_CLUES;
	}
}