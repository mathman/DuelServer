package duelserver;

public class GameScriptJceFirst extends ScriptGame
{
    private int _spawnTimer;
    private boolean _isSpawn;
    
    GameScriptJceFirst(DuelGame game)
    {
        super(game);
        _spawnTimer = 500;
        _isSpawn = false;
    }

    @Override
    public void onUpdate(long diff)
    {
        if (!_isSpawn && _spawnTimer <= diff)
        {
            MobShip mob = _me.createMob((byte) 2, (byte) 0,(byte) 2,(byte) 0, 500, 20);
            MobScriptFirst mobScriptFirst = new MobScriptFirst(mob);
            ScriptMgr.getInstance().setScript(mob, mobScriptFirst);
            mob.setDelta(2, 0);
            _spawnTimer = 500;
            _isSpawn = true;
        }
        else
            _spawnTimer -= diff;
    }

    @Override
    public void onMobDie(MobShip mob)
    {
        MobShip otherMob = _me.createMob((byte) 2, (byte) 0,(byte) 2,(byte) 0, 500, 20);
        MobScriptFirst mobScriptFirst = new MobScriptFirst(otherMob);
        ScriptMgr.getInstance().setScript(otherMob, mobScriptFirst);
        otherMob.setDelta(2, 0);
    }

    @Override
    public void onMobAppear(MobShip mob)
    {
    }

    @Override
    public void onPlayerDie(PlayerShip player)
    {
    }

    @Override
    public void onPlayerAppear(PlayerShip player)
    {
    }
}