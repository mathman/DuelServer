package duelserver;

public class Missile extends ObjectMap
{
    private long _shipLauncher;
    
    Missile(DuelGame game, byte orientation, long shipLauncher, byte Team, byte Type, int PositionX, int PositionY)
    {
        super(game, orientation, Team, Type, PositionX, PositionY);
        _shipLauncher = shipLauncher;
        _objectType = ObjectMap.MISSILE;
        switch (_type)
        {
            case 0:
                _SizeX = 10;
                _SizeY = 25;
                _Life = 1;
                _LengthMove = 5;
                break;
        }
    }
    
    public long getShipLauncher()
    {
        return _shipLauncher;
    }
}