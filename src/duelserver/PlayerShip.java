package duelserver;

public class PlayerShip extends ObjectMap
{
    private byte _weaponType;
    private long _guidPlayer;
    
    public  PlayerShip(DuelGame game, long guidPlayer, byte orientation, byte weaponType, byte Team, byte typeImage, int PositionX, int PositionY)
    {
        super(game, orientation, Team, typeImage, PositionX, PositionY);
        _guidPlayer = guidPlayer;
        _objectType = ObjectMap.PLAYER;
        _weaponType = weaponType;
        switch (_type)
        {
            case 0:
                _SizeX = 50;
                _SizeY = 50;
                _LengthMove = 4;
                _Life = 3;
                break;
            case 1:
                _SizeX = 50;
                _SizeY = 50;
                _LengthMove = 4;
                _Life = 3;
                break;
        }
    }
    
    public void setGuidPlayer(long guid)
    {
        _guidPlayer = guid;
    }
    
    public long getGuidPlayer()
    {
        return _guidPlayer;
    }
    
    public byte getWeaponType()
    {
        return _weaponType;
    }
    
    public Missile fire(Packet messagePacket)
    {
        int PositionXLauncher = messagePacket.getInt();
        int PositionYLauncher = messagePacket.getInt();
        setPosition(PositionXLauncher, PositionYLauncher);
        switch (getWeaponType())
        {
            case 0:
                int PositionY = getPositionY() + 25;
                int PositionX = getPositionX() + 20;
                byte orientation = _orientation;
                Missile missile = new Missile(_game, orientation, getGuid(), getTeam(), getWeaponType(), PositionX, PositionY);
                missile.setDelta(0, -5);
                return missile;
        }
        return null;
    }
}