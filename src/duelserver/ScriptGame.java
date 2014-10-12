package duelserver;

public abstract class ScriptGame
{
    DuelGame _me;
    
    ScriptGame(DuelGame game)
    {
        _me = game;
    }
    
    public DuelGame getMe()
    {
        return _me;
    }
    
    public abstract void onUpdate(long diff);
    
    public abstract void onMobDie(MobShip mob);

    public abstract void onMobAppear(MobShip mob);
    
    public abstract void onPlayerDie(PlayerShip player);

    public abstract void onPlayerAppear(PlayerShip player);
}
