package duelserver;

import java.io.*;

public class DuelPlayer
{
    long _guid;
    long _guidServer;
    DuelThread _thread;
    String _name;
    Boolean _isOccuped;
    private byte _class;
    private byte _ready;
    private byte _onPartie;
    private int _score;
    PlayerShip _ship;

    DuelPlayer(DuelThread thread)
    {
        _score = 0;
        _ship = null;
        _guid = 0;
        _guidServer = 0;
        _thread = thread;
        _isOccuped = false;
    }
    
    public void setScore(int score)
    {
        _score = score;
    }
    
    public int getScore()
    {
        return _score;
    }
    
    public void setShip(PlayerShip ship)
    {
        _ship = ship;
    }
    
    public PlayerShip getShip()
    {
        return _ship;
    }

    public void setGuid(long guid)
    {
        _guid = guid;
    }

    public long getGuid()
    {
        return _guid;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }
    
    public Boolean isOccuped()
    {
        return _isOccuped;
    }
    
    public void setOnPartie(byte onPartie)
    {
        _onPartie = onPartie;
    }

    public byte getOnPartie()
    {
        return _onPartie;
    }
    
    public void setGame(long guidServer)
    {
        _isOccuped = true;
        _guidServer = guidServer;
    }
    
    public void removeGame()
    {
        _isOccuped = false;
        _ready = 0;
        _guidServer = 0;
        _class = 0;
        _onPartie = 0;
    }
     
    public long getServerGuid()
    {
        return _guidServer;
    }
    
    public void setClass(byte classGame)
    {
    	_class = classGame;
    }
    
    public byte getTypeShip()
    {
    	return _class;
    }
    
    public void setReady(byte ready)
    {
    	_ready = ready;
    }
    
    public byte getReady()
    {
    	return _ready;
    }
    
    public void sendPacket(Packet packet)
    {
        try
        {
            _thread.getOut().write(packet.getByteArray());
        }
        catch (IOException e){ }
    }
}