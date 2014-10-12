package duelserver;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class DuelServer
{
    private static DuelServer instance = null;
    private HashMap<Long, DuelPlayer> _tabClients;
    private HashMap<Long, DuelGame> _games;
    private List<Long> _lastFreeGuidClient;
    private long _guidMaxClient;
    private List<Long> _lastFreeGuidGame;
    private long _guidMaxGame;

    public static void main(String args[])
    {
        DuelServer duelserv = new DuelServer();
        instance = duelserv;
        
        try
        {
            int port;
            if(args.length<=0)
            	port = 20000;
            else
            	port = new Integer(args[0]);

            new Commandes();

            ServerSocket ss = new ServerSocket(port);
            duelserv.printWelcome(port);
            while (true)
            {
                new DuelThread(ss.accept());
            }
        }
        catch (Exception e) { }
    }
    
    private DuelServer()
    {
        _lastFreeGuidClient = new ArrayList<>();
        _guidMaxClient = 1;
        _lastFreeGuidGame = new ArrayList<>();
        _guidMaxGame = 1;
        _tabClients = new HashMap<>();
        _games = new HashMap<>();
    }
    
    public static DuelServer getInstance()
    { 
        return instance;
    }

    private void printWelcome(Integer port)
    {
        System.out.println("--------");
        System.out.println("Space Attack : Par Mathman");
        System.out.println("--------");
        System.out.println("Demarre sur le port : "+port.toString());
        System.out.println("--------");
        System.out.println("Quitter : tapez \"quit\"");
        System.out.println("Nombre de connectes : tapez \"total\"");
        System.out.println("Nombre de parties : \"parties\"");
        System.out.println("--------");
    }

    synchronized public void delClient(long guid)
    {
        if (_tabClients.containsKey(guid))
        {
            _tabClients.remove(guid);
            _lastFreeGuidClient.add(guid);
        }
    }

    synchronized public long addClient(DuelPlayer player)
    {
        if (!_tabClients.containsValue(player))
        {
            long guid = 0;
            
            if (_lastFreeGuidClient.size() > 0)
            {
                guid = _lastFreeGuidClient.get(0);
                _lastFreeGuidClient.remove(0);
            }
            else
            {
                guid = _guidMaxClient;
                _guidMaxClient++;
            }
            
            player.setGuid(guid);
            _tabClients.put(guid, player);
            return guid;
        }
        return 0;
    }

    synchronized public int getNbClients()
    {
        return _tabClients.size();
    }

    synchronized public void delEncounter(long guid)
    {
        if (_games.containsKey(guid))
        {
            _games.remove(guid);
            _lastFreeGuidGame.add(guid);
        }
    }

    synchronized public long addEncounter(DuelGame game)
    {
        if (!_games.containsValue(game))
        {
            long guid = 0;
            
            if (_lastFreeGuidGame.size() > 0)
            {
                guid = _lastFreeGuidGame.get(0);
                _lastFreeGuidGame.remove(0);
            }
            else
            {
                guid = _guidMaxGame;
                _guidMaxGame++;
            }
            
            game.setGuid(guid);
            _games.put(guid, game);
            return guid;
        }
        return 0;
    }
    
    synchronized public long getNbGames()
    {
        return _games.size();
    }
    
    synchronized public void sendGamesFormatted(DuelPlayer player)
    {
        Packet packet = new Packet(2, 8);
        packet.putLong(getNbGames());
        player.sendPacket(packet);

        if (getNbGames() > 0)
        {
            for(Entry<Long, DuelGame> entry : _games.entrySet())
            {
                DuelGame value = entry.getValue();
                packet = new Packet(3, 1 + 4 + 1 + 4 + value.getName().toCharArray().length*2);
                packet.putByte(value.getType());
                packet.putInt(value.getPlayersCount());
                packet.putByte(value.getState());
                packet.putString(value.getName());
                player.sendPacket(packet);
            }
        }
    }

    synchronized public DuelGame getGameByName(String name)
    {
        for(Entry<Long, DuelGame> entry : _games.entrySet())
        {
            DuelGame value = entry.getValue();
            if (value.getName().equals(name))
            {
                return value;
            }
        }
        return null;
    }
    
    synchronized public long createGame(String name)
    {
        long guidServer = 0;
        DuelGame game = new DuelGame(name);
        guidServer = addEncounter(game);
        return guidServer;
    }
    
    synchronized public DuelGame getGameByGuid(long guid)
    {
        return _games.get(guid);
    }
    
    synchronized public DuelPlayer getPlayerByGuid(long guid)
    {
        return _tabClients.get(guid);
    }
}