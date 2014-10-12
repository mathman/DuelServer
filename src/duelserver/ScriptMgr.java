package duelserver;

import java.util.HashMap;

public class ScriptMgr
{
    private HashMap<MobShip, ScriptMob> _scriptsMob;
    private HashMap<DuelGame, ScriptGame> _scriptsGame;
    private static ScriptMgr instance = null;
    
    private ScriptMgr()
    {
        _scriptsMob = new HashMap<MobShip, ScriptMob>();
        _scriptsGame = new HashMap<DuelGame, ScriptGame>();
    }
    
    public static ScriptMgr getInstance()
    {
        if (instance == null)
            instance = new ScriptMgr();
           
        return instance;
    }
    
    public void addScript(MobShip mob, ScriptMob script)
    {
        _scriptsMob.put(mob, script);
    }
    
    public void removeScript(ScriptMob script)
    {
        _scriptsMob.remove(script);
    }
    
    public void addScript(DuelGame game, ScriptGame script)
    {
        _scriptsGame.put(game, script);
    }
    
    public void removeScript(ScriptGame script)
    {
        _scriptsGame.remove(script);
    }
    
    private ScriptMob getScriptByMob(MobShip mob)
    {
        return _scriptsMob.get(mob);
    }
    
    private ScriptGame getScriptByGame(DuelGame game)
    {
        return _scriptsGame.get(game);
    }
    
    public void setScript(MobShip mob, ScriptMob newScript)
    {
        ScriptMob oldScript = getScriptByMob(mob);
        if (oldScript != null)
        {
            removeScript(oldScript);
            addScript(mob, newScript);
        }
        else
            addScript(mob, newScript);
    }
    
    public void setScript(DuelGame game, ScriptGame newScript)
    {
        ScriptGame oldScript = getScriptByGame(game);
        if (oldScript != null)
        {
            removeScript(oldScript);
            addScript(game, newScript);
        }
        else
            addScript(game, newScript);
    }
    
    public void onUpdate(long diff, MobShip mob)
    {
        ScriptMob script = getScriptByMob(mob);
        if (script != null)
        {
            script.onUpdate(diff);
        }
    }

    public void onDie(MobShip mob)
    {
        ScriptMob script = getScriptByMob(mob);
        if (script != null)
        {
            script.onDie();
        }
    }
    
    public void onCanNotMove(MobShip mob)
    {
        ScriptMob script = getScriptByMob(mob);
        if (script != null)
        {
            script.onCanNotMove();
        }
    }
    
    public void onUpdate(long diff, DuelGame game)
    {
        ScriptGame script = getScriptByGame(game);
        if (script != null)
        {
            script.onUpdate(diff);
        }
    }
    
    public void onMobDie(DuelGame game, MobShip mob)
    {
        ScriptGame script = getScriptByGame(game);
        if (script != null)
        {
            script.onMobDie(mob);
        }
    }

    public void onMobAppear(DuelGame game, MobShip mob)
    {
        ScriptGame script = getScriptByGame(game);
        if (script != null)
        {
            script.onMobAppear(mob);
        }
    }
    
    public void onPlayerDie(DuelGame game, PlayerShip player)
    {
        ScriptGame script = getScriptByGame(game);
        if (script != null)
        {
            script.onPlayerDie(player);
        }
    }

    public void onPlayerAppear(DuelGame game, PlayerShip player)
    {
        ScriptGame script = getScriptByGame(game);
        if (script != null)
        {
            script.onPlayerAppear(player);
        }
    }
}