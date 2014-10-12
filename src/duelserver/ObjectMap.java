package duelserver;

public abstract class ObjectMap
{
    protected int _PositionX;
    protected int _PositionY;
    protected byte _LengthMove;
    protected int _DeltaX;
    protected int _DeltaY;
    protected byte _type;
    protected long _Guid;
    protected int _objectType;
    protected byte _team;
    protected int _SizeX;
    protected int _SizeY;
    protected int _Life;
    protected DuelGame _game;
    protected boolean _inCollision;
    protected long _killerGuid;
    protected byte _orientation;
    
    public static final int PLAYER = 1;
    public static final int MOB = 2;
    public static final int MISSILE = 3;
    
    ObjectMap(DuelGame game, byte orientation, byte team, byte type, int PositionX, int PositionY)
    {
        _orientation = orientation;
        _killerGuid = 0;
        _inCollision = false;
        _game = game;
        _Guid = 0;
        _type = type;
        _DeltaX = 0;
        _DeltaY = 0;
        _PositionX = PositionX;
        _PositionY = PositionY;
        _team = team;
    }

    public void inCollision(boolean value)
    {
        _inCollision = value;
    }
    
    public boolean isInCollision()
    {
        return _inCollision;
    }
    
    public void setKiller(long guid)
    {
        _killerGuid = guid;
    }
    
    public long getKiller()
    {
        return _killerGuid;
    }
    
    public void setOrientation(byte orientation)
    {
        _orientation = orientation;
    }
    
    public byte getOrientation()
    {
        return _orientation;
    }
    
    public void setGuid(long guid)
    {
        _Guid = guid;
    }
    
    public long getGuid()
    {
        return _Guid;
    }
    
    public byte getType()
    {
        return _type;
    }
    
    public byte getLengthMove()
    {
        return _LengthMove;
    }
    
    public void setDelta(int DeltaX, int DeltaY)
    {
        _DeltaX = DeltaX;
        _DeltaY = DeltaY;
    }
    
    public int getDeltaX()
    {
        return _DeltaX;
    }
    
    public int getDeltaY()
    {
        return _DeltaY;
    }
    
    public int getPositionX()
    {
        return _PositionX;
    }
    
    public int getPositionY()
    {
        return _PositionY;
    }
    
    public int getObjectType()
    {
        return _objectType;
    }
    
    public byte getTeam()
    {
        return _team;
    }
    
    public void setPosition(int PositionX, int PositionY)
    {
        _PositionX = PositionX;
        _PositionY = PositionY;
    }
    
    public void move()
    {
        _PositionX = _PositionX + _DeltaX;
        _PositionY = _PositionY + _DeltaY;
    }
    
    public int getSizeX()
    {
        return _SizeX;
    }
    
    public int getSizeY()
    {
        return _SizeY;
    }
    
    public int getLife()
    {
        return _Life;
    }
    
    public void setLife(int Life)
    {
        _Life = Life;
    }
}
