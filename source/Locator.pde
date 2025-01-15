class Locator
{
	Actor player;
	Actor ship;

	QuantumMoon _quantumSector;

	Locator()
	{
		player = gameManager._solarSystem.player;
		ship = gameManager._solarSystem.ship;
		_quantumSector = (QuantumMoon)(gameManager._solarSystem.quantumMoon);
	}

	int getQuantumMoonLocation()
	{
		return _quantumSector.getQuantumLocation();
	}
}