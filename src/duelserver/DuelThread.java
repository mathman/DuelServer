package duelserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class DuelThread implements Runnable
{
    private Thread _t;
    private Socket _s;
    private DuelPlayer _player;

    DuelThread(Socket s)
    {
        _s = s;
        
        _player = new DuelPlayer(this);
        DuelServer.getInstance().addClient(_player);

        _t = new Thread(this);
        _t.start();
    }
    
    public OutputStream getOut()
    {
        try {
            return _s.getOutputStream();
        } catch (IOException ex) {
            return null;
        }
    }
    
    public InputStream getIn()
    {
        try {
            return _s.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public void run()
    {
        boolean loop = true;
        try
        {
            while (loop)
            {
                byte[] dataBytes = new byte[8];
                int data = _s.getInputStream().read(dataBytes, 0, 8);
                if (data == -1)
                    loop = false;
                else
                {
                    Packet firstPacket = new Packet(dataBytes);
                    int len = firstPacket.getInt();
                    int opcode = firstPacket.getInt();
                    byte[] messageArray = new byte[len];
                    if (len > 0)
                    {
                        int result = _s.getInputStream().read(messageArray);
                        if (result == -1)
                            loop = false;
                        else
                        {
                            Packet messagePacket = new Packet(messageArray);
                	    switch (opcode)
                	    {
                	        case 0:
                                    handleConnectPlayer(messagePacket);
                	    	    break;
                                case 1:
                                    handleRefreshGames(messagePacket);
                	    	    break;
                                case 2:
                                    handleCreateGame(messagePacket);
                                    break;
                                case 3:
                                    handleLeaveGame(messagePacket);
                                    break;
                                case 4:
                                    handlePlayerEnterGame(messagePacket);
                                    break;
                                case 5:
                                    handlePlayerAskEnter(messagePacket);
                                    break;
                	        case 6:
                                    handlePlayerDisconnect(messagePacket);
                	    	    break;
                                case 7:
                                    handleReceiveChat(messagePacket);
                                    break;
                                case 8:
                                    handleReceiveReady(messagePacket);
                                    break;
                                case 9:
                                    handleChangeType(messagePacket);
                                    break;
                                case 10:
                                    handleSendPlayers(messagePacket);
                                    break;
                                case 12:
                                    handlePlayerOnPartie(messagePacket);
                                    break;
                                case 13:
                                    handleUpdateDelta(messagePacket);
                                    break;
                                case 14:
                                    handleRequestFire(messagePacket);
                                    break;
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                System.out.println("Le client " + _player.getName() + " s'est deconnecte");
                if (_player.isOccuped())
                {
                    DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
                    if (game != null)
                    {
                        game.removeClient(_player);
                        System.out.println("le joueur " + _player.getName() + " a quitté le serveur " + game.getName());
                        if (game.getPlayersCount() == 0)
                        {
                            System.out.println("le serveur " + game.getName() + " a été supprimé");
                            game.killThread();
                            DuelServer.getInstance().delEncounter(game.getGuid());
                        }
                    }
                }
                DuelServer.getInstance().delClient(_player.getGuid());
                _s.close();
            }
            catch (IOException e){ }
        }
    }
    
    private void handleConnectPlayer(Packet messagePacket)
    {
        String name = messagePacket.getString();
        _player.setName(name);
        System.out.println("Un nouveau client s'est connecte " + _player.getName());
        Packet packet = new Packet(0, 8 + 4 + _player.getName().toCharArray().length*2);
        packet.putLong(_player.getGuid());
        packet.putString(_player.getName());
        _player.sendPacket(packet);
    }
    
    private void handleRefreshGames(Packet messagePacket)
    {
        System.out.println(_player.getName() + " demande un refresh");
        Packet packet = new Packet(1, 8);
        packet.putLong(DuelServer.getInstance().getNbClients());
        _player.sendPacket(packet);
        DuelServer.getInstance().sendGamesFormatted(_player);
    }
    
    private void handleCreateGame(Packet messagePacket)
    {
        byte type = messagePacket.getByte();
        String name = messagePacket.getString();
        if (_player.isOccuped())
        {
            System.out.println(_player.getName() + " est déja occupé");
            Packet packet = new Packet(4, 1);
            packet.putByte((byte) 1);
            _player.sendPacket(packet);
            return;
        }
        DuelGame game = DuelServer.getInstance().getGameByName(name);
        if (game != null)
        {
            System.out.println("le serveur " + name + " existe déja");
            Packet packet = new Packet(4, 1);
            packet.putByte((byte) 2);
            _player.sendPacket(packet);
            return;
        }
        long guidServer = DuelServer.getInstance().createGame(name);
        System.out.println("le serveur " + name + " est crée");
        game = DuelServer.getInstance().getGameByGuid(guidServer);
        game.setState((byte) 1);
        game.setType(type);
        game.addClient(_player);
        Packet packet = new Packet(4, 1);
        packet.putByte((byte) 3);
        _player.sendPacket(packet);
    }
    
    private void handleLeaveGame(Packet messagePacket)
    {
        if (_player.isOccuped())
        {
            DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
            if (game != null)
            {
                game.removeClient(_player);
                System.out.println("le joueur " + _player.getName() + " a quitté le serveur " + game.getName());
                if (game.getPlayersCount() == 0)
                {
                    System.out.println("le serveur " + game.getName() + " a été supprimé");
                    game.killThread();
                    DuelServer.getInstance().delEncounter(game.getGuid());
                }
            }
        }
    }
    
    private void handlePlayerEnterGame(Packet messagePacket)
    {
        if (_player.isOccuped())
        {
            DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
            if (game != null)
                game.enterGame(_player);

            System.out.println(_player.getName() + " rejoint " + game.getName());
        }
    }
    
    private void handlePlayerAskEnter(Packet messagePacket)
    {
        String name = messagePacket.getString();
        if (_player.isOccuped())
        {
            System.out.println(_player.getName() + " est déja occupé");
            Packet packet = new Packet(7, 1);
            packet.putByte((byte) 7);
            _player.sendPacket(packet);
            return;
        }
        int state = 0;
        DuelGame game = DuelServer.getInstance().getGameByName(name);
        if (game != null)
        {
            state = game.canEnter();

            Packet packet;
            switch (state)
            {
                case 0:
                    System.out.println(name + " n'existe pas");
                    packet = new Packet(7, 1);
                    packet.putByte((byte) 2);
                    _player.sendPacket(packet);
                    break;
                case 1:
                    System.out.println(name + " est déjà plein");
                    packet = new Packet(7, 1);
                    packet.putByte((byte) 3);
                    _player.sendPacket(packet);
                    break;
                case 2:
                    System.out.println(name + " est déjà en cours");
                    packet = new Packet(7, 1);
                    packet.putByte((byte) 4);
                    _player.sendPacket(packet);
                    break;
                case 3:
                    game.addClient(_player);
                    packet = new Packet(7, 1);
                    packet.putByte((byte) 5);
                    _player.sendPacket(packet);
                    break;
            }
        }
    }
    
    private void handlePlayerDisconnect(Packet messagePacket)
    {
        try
        {
            _s.close();
        }
        catch (IOException e){ }
    }
    
    private void handleReceiveChat(Packet messagePacket)
    {
        String msg = messagePacket.getString();
        DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
        if (game != null)
            game.sendChat(_player, msg);
    }
    
    private void handleReceiveReady(Packet messagePacket)
    {
        DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
        if (game != null)
            game.sendReady(_player);
    }
    
    private void handleChangeType(Packet messagePacket)
    {
        byte type = messagePacket.getByte();
        if (_player.getTypeShip() != type)
        {
            _player.setClass(type);
        }
        
        Packet packet = new Packet(11, 1 + 8);
        packet.putByte(type);
        packet.putLong(_player.getGuid());
        
        DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
        if (game != null)
            game.sendToClients(packet);
    }
    
    private void handleSendPlayers(Packet messagePacket)
    {
        if (_player.isOccuped())
        {
            DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
            if (game.getState() == DuelGame.WAITING)
                game.sendGameFormatted(_player);
        }
    }
    
    private void handlePlayerOnPartie(Packet messagePacket)
    {
        if (_player.isOccuped())
        {
            DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
            if (game != null)
                game.sendOnPartie(_player);
        }
    }
    
    private void handleUpdateDelta(Packet messagePacket)
    {
        DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
        if (game != null)
            game.updateDelta(_player, messagePacket);
    }
    
    private void handleRequestFire(Packet messagePacket)
    {
        DuelGame game = DuelServer.getInstance().getGameByGuid(_player.getServerGuid());
        if (game != null)
            game.playerFire(_player, messagePacket);
    }
}