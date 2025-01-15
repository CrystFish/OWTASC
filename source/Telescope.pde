class FrequencyButton extends Button
{
	boolean _rightFacing = true;

	FrequencyButton(boolean rightFacing)
	{
		super("Switch Frequency", width/2 + 140, 40, 50, 50);

		_rightFacing = rightFacing;

		if (!rightFacing)
		{
			setScreenPosition(new Vector2(width/2 - 140, 40));
		}
		//_disabledPrompt = "Switch Frequency\n(none known)";
	}

	void drawShape()
	{
		pushMatrix();
		translate(screenPosition.x, screenPosition.y);

		if (_rightFacing)
		{
			triangle(25, 0, 0, 15, 0, -15);
		}
		else
		{
			triangle(-25, 0, 0, 15, 0, -15);
		}

		popMatrix();
	}

	void drawText(float alpha) {}
}

class Telescope
{
	Frequency _frequency = Frequency.TRAVELER;
	float _signalStrength;

	void update(ArrayList<SignalSource> signalSources)
	{
		_signalStrength = 0;

		for (int i = 0; i < signalSources.size(); i++)
		{
			_signalStrength += signalSources.get(i).getSignalStrength(new Vector2(mouseX, mouseY), _frequency);
		}
	}

	void render()
	{
		String frequencyText = frequencyToString(_frequency);

		if (!playerData.knowsFrequency(_frequency))
		{
			if (_frequency == Frequency.BEACON)
			{
				frequencyText = "Unknown Frequency 001";
			}
		    else if (_frequency == Frequency.QUANTUM)
		    {
		    	frequencyText = "Unknown Frequency 002";
		    }
		}

		float xPos = width/2;

		fill(0, 0, 100);
		textSize(18);
		textFont(mediumFont);
		text(frequencyText, xPos, 40);
		textFont(smallFont);

		// draw signal feedback
		stroke(0, 0, 100);
		fill(200, 100, 100);
		rectMode(CENTER);
		rect(xPos, 70, max(0, _signalStrength * 300 - random(50)), 7);

		// draw crosshairs
	    noFill();
	    stroke(0, 0, 100);
	    line(mouseX, 0, mouseX, height);
	    line(0, mouseY, width, mouseY);
	}

	void nextFrequency()
	{
		int index = (_frequency.ordinal() + 1) % Frequency.values().length;
		_frequency = Frequency.values()[index];

		// if (!playerData.knowsFrequency(_frequency))
		// {
		// 	nextFrequency();
		// }
	}

	void previousFrequency()
	{
		int index = _frequency.ordinal() - 1;

		if (index < 0)
		{
			index = Frequency.values().length - 1;
		}

		_frequency = Frequency.values()[index];

		// if (!playerData.knowsFrequency(_frequency))
		// {
		// 	previousFrequency();
		// }
	}
}

class SignalSource extends Entity
{
	ArrayList<Signal> _signals;

	SignalSource(Node node)
	{
		setScreenPosition(node.screenPosition);
		_signals = new ArrayList<Signal>();
		_signals.add(node.getSignal());
	}

	SignalSource(Vector2 screenPosition, Sector sector)
	{
		setScreenPosition(screenPosition);
		_signals = sector.getSectorSignals();
	}

	void addSignal(Signal signal)
	{
		_signals.add(signal);
	}

	float getSignalStrength(Vector2 telescopePos, Frequency frequency)
	{
		if (hasSignalWithFrequency(frequency))
		{
			Vector2 d = telescopePos.sub(screenPosition);
			float dist = d.magnitude();

			float u = max(0, 1 - (dist / 150));
			return u * u * u;
		}
		return 0;
	}

	boolean hasSignalWithFrequency(Frequency frequency)
	{
		for (int i = 0; i < _signals.size(); i++)
		{
			if (_signals.get(i).frequency == frequency)
			{
				return true;
			}
		}
		return false;
	}
}

class Signal
{
	Frequency frequency;

	Signal(String signalType)
	{
		if (signalType.equals("QUANTUM"))
		{
			frequency = Frequency.QUANTUM;
		}
		else if (signalType.equals("BEACON"))
		{
			frequency = Frequency.BEACON;
		}
		else if (signalType.equals("TRAVELER"))
		{
			frequency = Frequency.TRAVELER;
		}
	}
}