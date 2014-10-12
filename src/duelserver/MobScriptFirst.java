package duelserver;

public class MobScriptFirst extends ScriptMob
{
    private int _fireTimer;
    
    MobScriptFirst(MobShip mob)
    {
        super(mob);
        _fireTimer = 1000;
    }

    @Override
    public void onUpdate(long diff)
    {
        if (_fireTimer <= diff)
        {
            //_me.fire();
            _fireTimer = 1000;
        }
        else
            _fireTimer -= diff;
    }

    @Override
    public void onDie()
    {
    }

    @Override
    public void onCanNotMove()
    {
        _me.setDelta(-_me.getDeltaX(), 0);
    }
}