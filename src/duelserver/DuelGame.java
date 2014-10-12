package duelserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DuelGame implements Runnable
{
    private Thread _t;
    private byte _type;
    private int _nbPlayers;
    private long _guid;
    private String _name;
    private byte _state;
    private HashMap<Long, DuelPlayer> _clients;
    private List<Long> _clientGuids;
    private int _numStart;
    private long _diff;
    private long _oldTime;
    private boolean _running;
    private long _sleepTimer;
    private Map _map;
    private HashMap<Long, PlayerShip> _playersShip;
    private HashMap<Long, Missile> _missiles;
    private HashMap<Long, MobShip> _mobShip;
    
    public static final int WAITING = 1;
    public static final int LOADING = 2;
    public static final int PLAYERINGAME = 3;
    public static final int INPROGRESS = 4;
    public static final int FINISH = 5;
    
    public static final byte JCE = 0;
    public static final byte JCJ = 1;

    DuelGame(String name)
    {
        _running = false;
        _sleepTimer = 1000;
        _diff = 0;
        _oldTime = System.currentTimeMillis();
        _map = null;
        _clients = new HashMap<>();
        _clientGuids = new ArrayList<>();
        _playersShip = new HashMap<>();
        _missiles = new HashMap<>();
        _mobShip = new HashMap<>();
        _state = 0;
        _numStart = 0;
        _nbPlayers = 0;
        _guid = 0;
        _name = name;
        
        _t = new Thread(this);
        _t.start();
    }
    
    @Override
    public void run()
    {
        while (!_t.isInterrupted())
        {
            try
            {
                _t.sleep(_sleepTimer);
                
                long currentTime = System.currentTimeMillis();
                _diff = currentTime - _oldTime;

                _oldTime = currentTime;

                if (_running)
                    update(_diff);
            }
            catch (InterruptedException ex) {}
        }
    }
    
    private void update(long diff)
    {
        if (_state == LOADING)
        {
            _numStart++;
            if (_numStart >= 6)
            {
                _numStart = 0;
                
                Packet packet = new Packet(13, 1);
                packet.putByte((byte) 0);
                sendToClients(packet);

                System.out.println("Le serveur " + getName() + " est démarré");
                
                ScriptGame script = null;
                switch (_type)
                {
                    case JCE:
                        script = new GameScriptJceFirst(this);
                        break;
                    case JCJ:
                        script = new GameScriptJcjFirst(this);
                        break;
                }
                if (script != null)
                    ScriptMgr.getInstance().addScript(this, script);
                
                _state = PLAYERINGAME;
                 _running = false;
            }
        }
        else if (_state == INPROGRESS)
        {
            _map.update(_diff);
            ScriptMgr.getInstance().onUpdate(_diff,this);
        }
    }
    
    public void onMove(long diff, byte stateMove, ObjectMap object)
    {
        switch (object.getObjectType())
        {
            case ObjectMap.MISSILE:
                switch (stateMove)
                {
                    case 0:
                        object.move();
                        break;
                    case 1:
                    case 2:
                        removeMissile(object.getGuid());
                        _map.removeObject(object.getGuid());
                        break;
                }
                break;
            case ObjectMap.PLAYER:
                PlayerShip player = getPlayerShipByGuid(object.getGuid());
                if (player != null)
                {
                    switch (stateMove)
                    {
                        case 0:
                            object.move();
                            break;
                        case 2:
                            if (object.getLife() <= 0)
                            {
                                Packet packet = new Packet(19, 8);
                                packet.putLong(object.getGuid());
                                sendToClients(packet);

                                ScriptMgr.getInstance().onPlayerDie(this, player);
                                removePlayer(object.getGuid());
                                _map.removeObject(object.getGuid());
                                if (_type == JCJ)
                                    updateScore();
                            }
                            break;
                    }
                }
                break;
            case ObjectMap.MOB:
                MobShip mob = getMobShipByGuid(object.getGuid());
                if (mob != null)
                {
                    switch (stateMove)
                    {
                        case 0:
                            object.move();
                            ScriptMgr.getInstance().onUpdate(diff, mob);
                            break;
                        case 1:
                            ScriptMgr.getInstance().onCanNotMove(mob);
                            ScriptMgr.getInstance().onUpdate(diff, mob);
                            break;
                        case 2:
                            if (object.getLife() <= 0)
                            {
                                DuelPlayer playerKiller = getPlayerByShipGuid(object.getKiller());
                                if (playerKiller != null)
                                    playerKiller.setScore(playerKiller.getScore() + 10);
 
                                Packet packet = new Packet(19, 8);
                                packet.putLong(object.getGuid());
                                sendToClients(packet);

                                ScriptMgr.getInstance().onMobDie(this, mob);
                                removeMob(object.getGuid());
                                _map.removeObject(object.getGuid());
                                updateScore();
                            }
                            break;
                    }
                }
                break;
        }
    }
    
    public void onCollision(ObjectMap firstObject, ObjectMap secondObject)
    {
        if (firstObject.getObjectType() == ObjectMap.MISSILE && secondObject.getObjectType() != ObjectMap.MISSILE)
        {
            if (!secondObject.isInCollision())
            {
                secondObject.inCollision(true);
                secondObject.setLife(secondObject.getLife() - 1);
                
                if (secondObject.getLife() <= 0)
                {
                    Missile missile = getMissileByGuid(firstObject.getGuid());
                    if (missile != null)
                    {
                        long shipGuid = missile.getShipLauncher();
                        ObjectMap object = _map.getObjectMoveByGuid(shipGuid);
                        if (object != null)
                            secondObject.setKiller(object.getGuid());
                    }
                }
            }
        }
        else if (secondObject.getObjectType() == ObjectMap.MISSILE && firstObject.getObjectType() != ObjectMap.MISSILE)
        {
            if (!firstObject.isInCollision())
            {
                firstObject.inCollision(true);
                firstObject.setLife(firstObject.getLife() - 1);
                
                if (firstObject.getLife() <= 0)
                {
                    Missile missile = getMissileByGuid(secondObject.getGuid());
                    if (missile != null)
                    {
                        long shipGuid = missile.getShipLauncher();
                        ObjectMap object = _map.getObjectMoveByGuid(shipGuid);
                        if (object != null)
                            firstObject.setKiller(object.getGuid());
                    }
                }
            }
        }
    }
    
    public void startGame()
    {
        _map = new Map(this);

        DuelPlayer client1 = getClient(_clientGuids.get(0));
        DuelPlayer client2 = getClient(_clientGuids.get(1));
        if (client1 != null && client2 != null)
        {
            switch (_type)
            {
                case 0:
                    createPlayer(client1.getGuid(),(byte) 0, (byte) 0, (byte) 1, client1.getTypeShip(), 100, 550);

                    createPlayer(client2.getGuid(), (byte) 0, (byte) 0, (byte) 1, client2.getTypeShip(), 800, 550);
                    break;
                case 1:
                    createPlayer(client1.getGuid(), (byte) 0, (byte) 0, (byte) 1, client1.getTypeShip(), 200, 550);

                    createPlayer(client2.getGuid(), (byte) 2, (byte) 0, (byte) 2, client2.getTypeShip(), 600, 0);
                    break;
            }
            
            Packet packet = new Packet(15,8 + 4 + 8 + 4);
            packet.putLong(client1.getGuid());
            packet.putInt(client1.getScore());

            packet.putLong(client2.getGuid());
            packet.putInt(client2.getScore());

            sendToClients(packet);
        }

        _sleepTimer = 10;
        _state = INPROGRESS;
        _running = true;
    }

    public PlayerShip createPlayer(long guidPlayer, byte orientation, byte weaponType, byte team, byte typeImage, int positionX, int positionY)
    {
        PlayerShip ship = new PlayerShip(this, guidPlayer, orientation, weaponType, team, typeImage, positionX, positionY);
        DuelPlayer client = getClient(guidPlayer);
        if (client != null)
            client.setShip(ship);
        _map.addObject(ship);
        addPlayer(ship);
        ScriptMgr.getInstance().onPlayerAppear(this, ship);
        
        Packet packet = new Packet(22,8 + 8 + 4 + 1 + 1 + 1 + 1 + 1 + 4 + 4);
        packet.putLong(ship.getGuidPlayer());
        packet.putLong(ship.getGuid());
        packet.putInt(ship.getLife());
        packet.putByte(ship.getOrientation());
        packet.putByte(ship.getLengthMove());
        packet.putByte(ship.getWeaponType());
        packet.putByte(ship.getTeam());
        packet.putByte(ship.getType());
        packet.putInt(ship.getPositionX());
        packet.putInt(ship.getPositionY());
        sendToClients(packet);

        return ship;
    }
    
    private void addPlayer(PlayerShip player)
    {
        if (!_playersShip.containsValue(player))
        {
            _playersShip.put(player.getGuid(), player);
        }
    }
    
    private void removePlayer(long guid)
    {
        for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
        {
            DuelPlayer value = entry.getValue();
            PlayerShip ship = value.getShip();
            if (ship != null)
            {
                if (ship.getGuid() == guid)
                    value.setShip(null);
            }
        }

        if (_playersShip.containsKey(guid))
        {
            _playersShip.remove(guid);
        }
    }
    
    private void addMissile(Missile missile)
    {
        if (!_missiles.containsValue(missile))
        {
            _missiles.put(missile.getGuid(), missile);
        }
    }
    
    private void removeMissile(long guid)
    {
        if (_missiles.containsKey(guid))
        {
            _missiles.remove(guid);
        }
    }
    
    private void addMob(MobShip mob)
    {
        if (!_mobShip.containsValue(mob))
        {
            _mobShip.put(mob.getGuid(), mob);
        }
    }
    
    private void removeMob(long guid)
    {
        if (_mobShip.containsKey(guid))
        {
            _mobShip.remove(guid);
        }
    }
    
    private PlayerShip getPlayerShipByGuid(long guid)
    {
        return _playersShip.get(guid);
    } 
    
    private Missile getMissileByGuid(long guid)
    {
        return _missiles.get(guid);
    }
    
    private MobShip getMobShipByGuid(long guid)
    {
        return _mobShip.get(guid);
    }
    
    private DuelPlayer getPlayerByShipGuid(long shipGuid)
    {
        for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
        {
            DuelPlayer value = entry.getValue();
            if (value.getShip() != null)
            {
                if (value.getShip().getGuid() == shipGuid)
                    return value;
            }
        }
        return null;
    }
    
    public void updateDelta(DuelPlayer client, Packet messagePacket)
    {
        if (getState() == DuelGame.INPROGRESS)
        {
            long Guid = messagePacket.getLong();
            int PositionX = messagePacket.getInt();
            int PositionY = messagePacket.getInt();
            int DeltaX = messagePacket.getInt();
            int DeltaY = messagePacket.getInt();
            ObjectMap Object = _map.getObjectMoveByGuid(Guid);
            if (Object != null)
            {
                Object.setDelta(DeltaX, DeltaY);
                Object.setPosition(PositionX, PositionY);
                Packet packet = new Packet(16, 8 + 4 + 4 + 4 + 4);
                packet.putLong(Guid);
                packet.putInt(PositionX);
                packet.putInt(PositionY);
                packet.putInt(DeltaX);
                packet.putInt(DeltaY);
            
                for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
                {
                    DuelPlayer value = entry.getValue();
                    if (value.getGuid() != client.getGuid())
                        value.sendPacket(packet);
                }
            }
        }
    }
    
    public void playerFire(DuelPlayer client, Packet messagePacket)
    {
        if (getState() == DuelGame.INPROGRESS)
        {
            PlayerShip ship = client.getShip();
            if (ship != null)
            {
                Missile missile = ship.fire(messagePacket);
                if (missile != null)
                {
                    _map.addObject(missile);
                    addMissile(missile);
                    Packet packet = new Packet(17, 8 + 4 + 4);
                    packet.putLong(ship.getGuid());
                    packet.putInt(ship.getPositionX());
                    packet.putInt(ship.getPositionY());
                    
                    for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
                    {
                        DuelPlayer value = entry.getValue();
                        if (value.getGuid() != client.getGuid())
                            value.sendPacket(packet);
                    }
                }
            }
        }
    }
    
    public MobShip createMob(byte orientation, byte weaponType, byte Team, byte type, int PositionX, int PositionY)
    {
        MobShip mob = new MobShip(this, orientation, weaponType, Team, type, PositionX, PositionY);

        _map.addObject(mob);
        addMob(mob);
        ScriptMgr.getInstance().onMobAppear(this, mob);
        
        Packet packet = new Packet(18, 8 + 4 + 1 + 1 + 1 + 1 + 1 + 4 + 4);
        packet.putLong(mob.getGuid());
        packet.putInt(mob.getLife());
        packet.putByte(mob.getOrientation());
        packet.putByte(mob.getLengthMove());
        packet.putByte(mob.getWeaponType());
        packet.putByte(mob.getTeam());
        packet.putByte(mob.getType());
        packet.putInt(mob.getPositionX());
        packet.putInt(mob.getPositionY());
        sendToClients(packet);

        return mob;
    }
    
    public void mobFire(MobShip mob, Missile missile)
    {
        _map.addObject(missile);
        addMissile(missile);
        Packet packet = new Packet(17, 8 + 4 + 4);
        packet.putLong(mob.getGuid());
        packet.putInt(mob.getPositionX());
        packet.putInt(mob.getPositionY());
        sendToClients(packet);
    }
    
    public void deltaChange(MobShip mob)
    {
        Packet packet = new Packet(16, 8 + 4 + 4 + 4 + 4);
        packet.putLong(mob.getGuid());
        packet.putInt(mob.getPositionX());
        packet.putInt(mob.getPositionY());
        packet.putInt(mob.getDeltaX());
        packet.putInt(mob.getDeltaY());
        sendToClients(packet);
    }
    
    public void positionChange(MobShip mob)
    {
        Packet packet = new Packet(20, 8 + 4 + 4);
        packet.putLong(mob.getGuid());
        packet.putInt(mob.getPositionX());
        packet.putInt(mob.getPositionY());
        sendToClients(packet);
    }
    
    public void updateScore()
    {
        DuelPlayer player1 = getClient(_clientGuids.get(0));
        DuelPlayer player2 = getClient(_clientGuids.get(1));
        if (player1 != null && player2 != null)
        {
            Packet packet = new Packet(21, 8 + 4 + 8 + 4);
            packet.putLong(player1.getGuid());
            packet.putInt(player1.getScore());
            packet.putLong(player2.getGuid());
            packet.putInt(player2.getScore());
            sendToClients(packet);
        }
    }

    public int getPlayersCount()
    {
        return _clientGuids.size();
    }
    
    public void setGuid(long guid)
    {
        _guid = guid;
    }
    
    public long getGuid()
    {
        return _guid;
    }
    
    public void setState(byte state)
    {
        _state = state;
    }
    
    public byte getState()
    {
        return _state;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setType(byte type)
    {
        _type = type;
    }
    
    public byte getType()
    {
        return _type;
    }
    
    public void addClient(DuelPlayer client)
    {
        if (!_clients.containsValue(client))
        {
            int index = _clientGuids.indexOf(client.getGuid());
            if (index == -1)
                _clientGuids.add(client.getGuid());
            _clients.put(client.getGuid(), client);
        }
        client.setGame(getGuid());
    }
    
    public void removeClient(DuelPlayer client)
    {
        client.removeGame();
        if (_clients.containsKey(client.getGuid()))
        {
            int index = _clientGuids.indexOf(client.getGuid());
            if (index > -1)
                _clientGuids.remove(client.getGuid());
            _clients.remove(client.getGuid());
        }

        if (getState() == DuelGame.WAITING || getState() == DuelGame.LOADING)
        {
            if (getState() == DuelGame.LOADING)
            {
                reset();
                setState((byte) 1);
            }

            for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
            {
                DuelPlayer value = entry.getValue();
                if (value.getReady() == 1)
                    value.setReady((byte) 0);
                    
                sendGameFormatted(value);
            }
            
            String msg = "Le joueur " + client.getName() + " a quitté la partie";
            Packet packet = new Packet(8, 1 + 4 + msg.toCharArray().length*2);
            packet.putByte((byte) 0);
            packet.putString(msg);
            sendToClients(packet);
        }
        else if (getState() == DuelGame.INPROGRESS)
        {
            reset();
            setState((byte) 1);

            for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
            {
                DuelPlayer value = entry.getValue();
                if (value.getOnPartie() == 1)
                    value.setOnPartie((byte) 0);

                value.setReady((byte) 0);
                if (value.getGuid() != client.getGuid())
                {
                    String msg = "Le joueur " + client.getName() + " a quitté la partie";
                    Packet packet = new Packet(8, 1 + 4 + msg.toCharArray().length*2);
                    packet.putByte((byte) 1);
                    packet.putString(msg);
                    value.sendPacket(packet);
                }
            }
            
            Packet packet = new Packet(14, 1);
            packet.putByte((byte) 0);
            sendToClients(packet);
        }
    }
    
    public void enterGame(DuelPlayer client)
    {
        String msg = "Le joueur " + client.getName() + " a rejoint la partie";
        Packet packet = new Packet(8, 1 + 4 + msg.toCharArray().length*2);
        packet.putByte((byte) 0);
        packet.putString(msg);
        for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
        {
            DuelPlayer value = entry.getValue();
            if (value.getGuid() != client.getGuid())
                value.sendPacket(packet);
            sendGameFormatted(value);
        }
    }
    
    public void sendChat(DuelPlayer sender, String msg)
    {
        if (getState() == DuelGame.WAITING || getState() == DuelGame.LOADING || getState() == DuelGame.INPROGRESS)
        {
            Packet packet = new Packet(9, 8 + 4 + msg.toCharArray().length*2);
            packet.putLong(sender.getGuid());
            packet.putString(msg);
            sendToClients(packet);
        }
    }
    
    public void sendReady(DuelPlayer client)
    {
        if (getState() == DuelGame.WAITING)
        {
            int loopReady = 0;
            if (client.getReady() == 0)
            {
                System.out.println(client.getName() + " est pret pour le serveur " + getName());
                client.setReady((byte) 1);
                Packet packet = new Packet(10, 8);
                packet.putLong(client.getGuid());

                for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
                {
                    DuelPlayer value = entry.getValue();
                    value.sendPacket(packet);
                    
                    if (value.getReady() == 1)
                        loopReady++;
                }
            }

            if (loopReady == 2)
            {
                System.out.println("Le serveur " + getName() + " est en cours de démarrage");
                Packet packet = new Packet(12, 1);
                packet.putByte((byte) 0);
                sendToClients(packet);

                setState((byte) 2);
                start();
            }
        }
    }
    
    public void sendOnPartie(DuelPlayer client)
    {
        if (getState() == DuelGame.PLAYERINGAME)
        {
            if (client.getOnPartie() == 0)
            {
                client.setOnPartie((byte) 1);
                
                boolean waitPlayer = false;
                for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
                {
                    DuelPlayer value = entry.getValue();
                    if (value.getGuid() != client.getGuid())
                    {
                        if (value.getOnPartie() == 0)
                        {
                            if (waitPlayer == false)
                                waitPlayer = true;
                        }
                        else
                        {
                            String msg = "Le joueur " + client.getName() + " est arrivé en jeu";
                            Packet packet = new Packet(8, 1 + 4 + msg.toCharArray().length*2);
                            packet.putByte((byte) 2);
                            packet.putString(msg);
                            value.sendPacket(packet);
                        }
                    }
                }
                if (waitPlayer)
                {
                    String msg = "Attente des autres joueurs...";
                    Packet packet = new Packet(8, 1 + 4 + msg.toCharArray().length*2);
                    packet.putByte((byte) 2);
                    packet.putString(msg);
                    client.sendPacket(packet);
                }
                else
                    startGame();
            }
        }
    }
    
    public void sendToClients(Packet packet)
    {
        for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
        {
            DuelPlayer value = entry.getValue();
            value.sendPacket(packet);
        }
    }
    
    public DuelPlayer getClient(long client)
    {
        return _clients.get(client);
    }
    
    public int canEnter()
    {
        if (getPlayersCount() >= 2)
            return 1;

        if (getState() != 1)
            return 2;

        return 3;
    }
    
    public void sendGameFormatted(DuelPlayer player)
    {
        Packet packet = new Packet(5, 1 + 4 + 4 + getName().toCharArray().length*2);
        packet.putByte(_type);
        packet.putInt(getPlayersCount());
        packet.putString(getName());
        player.sendPacket(packet);

        for(Entry<Long, DuelPlayer> entry : _clients.entrySet())
        {
            DuelPlayer value = entry.getValue();
            packet = new Packet(6, 8 + 1 + 1 + 4 + value.getName().toCharArray().length*2);
            packet.putLong(value.getGuid());
            packet.putByte(value.getTypeShip());
            packet.putByte(value.getReady());
            packet.putString(value.getName());
            player.sendPacket(packet);
        }
    }

    public void start()
    {
        _running = true;
    }
    
    public void reset()
    {
        _running = false;
        _sleepTimer = 1000;
    }
    
    public void killThread()
    {
        _t.interrupt();
    }
}