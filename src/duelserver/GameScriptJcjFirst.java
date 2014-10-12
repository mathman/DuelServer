package duelserver;

public class GameScriptJcjFirst extends ScriptGame
{
    GameScriptJcjFirst(DuelGame game)
    {
        super(game);
    }

    @Override
    public void onUpdate(long diff)
    {
    }

    @Override
    public void onMobDie(MobShip mob)
    {
    }

    @Override
    public void onMobAppear(MobShip mob)
    {
    }

    @Override
    public void onPlayerDie(PlayerShip player)
    {
        long guidClient = player.getGuidPlayer();
        byte orientation = player.getOrientation();
        byte weaponType = player.getWeaponType();
        byte team = player.getTeam();
        byte typeImage = player.getType();
        int positionX = 0;
        int positionY = 0;
        switch (team)
        {
            case 1:
                positionX = 200;
                positionY = 550;
                break;
            case 2:
                positionX = 600;
                positionY = 0;
                break;
        }
        PlayerShip ship = _me.createPlayer(guidClient, orientation, weaponType, team, typeImage, positionX, positionY);
    }

    @Override
    public void onPlayerAppear(PlayerShip player)
    {
    }
}