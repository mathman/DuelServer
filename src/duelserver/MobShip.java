package duelserver;

public class MobShip extends ObjectMap
{
    private byte _weaponType;
    
    public  MobShip(DuelGame game, byte orientation, byte weaponType, byte Team, byte type, int PositionX, int PositionY)
    {
        super(game, orientation, Team, type, PositionX, PositionY);
        _objectType = ObjectMap.MOB;
        _weaponType = weaponType;
        switch (_type)
        {
            case 0:
                _SizeX = 100;
                _SizeY = 70;
                _LengthMove = 5;
                _Life = 3;
                break;
        }
    }
    
    public byte getWeaponType()
    {
        return _weaponType;
    }
    
    public void fire()
    {
        switch (getWeaponType())
        {
            case 0:
                int PositionY = getPositionY() + 25;
                int PositionX = getPositionX() + 20;
                byte orientation = _orientation;
                Missile missile = new Missile(_game, orientation, getGuid(), getTeam(), getWeaponType(), PositionX, PositionY);
                missile.setDelta(0, 5);
                if (_game != null)
                {
                    _game.mobFire(this, missile);
                }
                break;
        }
    }
    
    @Override
    public void setDelta(int DeltaX, int DeltaY)
    {
        _DeltaX = DeltaX;
        _DeltaY = DeltaY;
        if (_game != null)
             _game.deltaChange(this);
    }
    
    @Override
    public void setPosition(int PositionX, int PositionY)
    {
        _PositionX = PositionX;
        _PositionY = PositionY;
        if (_game != null)
        {
             _game.positionChange(this);
        }
    }
}
