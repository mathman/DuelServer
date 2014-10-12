package duelserver;

public abstract class ScriptMob
{
    MobShip _me;
    
    ScriptMob(MobShip mob)
    {
        _me = mob;
    }
    
    public MobShip getMe()
    {
        return _me;
    }
    
    public abstract void onUpdate(long diff);

    public abstract void onDie();
    
    public abstract void onCanNotMove();
}
