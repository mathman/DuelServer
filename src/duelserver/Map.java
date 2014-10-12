package duelserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class Map
{
    private int _NbBlocsX;
    private int _NbBlocsY;
    private HashMap<Long, ObjectMap> _objectMap;
    private List<Long> _lastFreeGuidObject;
    private long _guidMaxObject;
    private DuelGame _game;
    private List<Long> _guidsDestroy;
    private List<ObjectMap> _guidsCreate;
    
    Map(DuelGame game)
    {
        _game = game;
        _NbBlocsX = 965;
        _NbBlocsY = 600;
        _guidMaxObject = 0;
        _lastFreeGuidObject = new ArrayList<>();
        _guidsDestroy = new ArrayList<>();
        _guidsCreate = new ArrayList<>();
        _objectMap = new HashMap<>();
    }
    
    public byte checkMove(ObjectMap object)
    {
        if (object.getDeltaX() < 0)
        {
            if (object.getPositionX() < 1)
            {
                return 1;
            }
        }
        else if (object.getDeltaX() > 0)
        {
            if (object.getPositionX() + object.getSizeX()  + object.getLengthMove() > _NbBlocsX)
            {
                return 1;
            }
        }
        else if (object.getDeltaY() < 0)
        {
            if (object.getPositionY() < 1)
            {
                return 1;
            }
        }
        else if (object.getDeltaY() > 0)
        {
            if (object.getPositionY() + object.getSizeY()  + object.getLengthMove() > _NbBlocsY)
            {
                return 1;
            }
        }
        
        for(Entry<Long, ObjectMap> entry : _objectMap.entrySet())
        {
            ObjectMap value = entry.getValue();
            if (object.getTeam() != value.getTeam())
            {
                if (inCollision(object, value))
                {
                    _game.onCollision(object, value);
                    return 2;
                }
            }
        }

        return 0;
    }
 
    private boolean inCollision(ObjectMap firstObject, ObjectMap secondObject)
    {
        if ((secondObject.getPositionX() >= firstObject.getPositionX() + firstObject.getSizeX())
        || (secondObject.getPositionX() + secondObject.getSizeX() <= firstObject.getPositionX())
        || (secondObject.getPositionY() >= firstObject.getPositionY() + firstObject.getSizeY())
        || (secondObject.getPositionY() + secondObject.getSizeY() <= firstObject.getPositionY()))
            return false;

        return true;
    }
    
    public void update(long diff)
    {
        for (int i = 0; i < _guidsDestroy.size(); i++)
        {
            _objectMap.remove(_guidsDestroy.get(i));
            _lastFreeGuidObject.add(_guidsDestroy.get(i));
        }
        _guidsDestroy.clear();
        
        for (int i = 0; i < _guidsCreate.size(); i++)
        {
            _objectMap.put(_guidsCreate.get(i).getGuid(), _guidsCreate.get(i));
        }
        _guidsCreate.clear();
            
        for(Entry<Long, ObjectMap> entry : _objectMap.entrySet())
        {
            ObjectMap value = entry.getValue();
            byte stateMove = checkMove(value);
            _game.onMove(diff, stateMove, value);
        }
        
        for(Entry<Long, ObjectMap> entry : _objectMap.entrySet())
        {
            ObjectMap value = entry.getValue();
            value.inCollision(false);
        }
    }
    
    public void addObject(ObjectMap object)
    {
        if (!_objectMap.containsValue(object))
        {
            long guid = 0;
            
            if (_lastFreeGuidObject.size() > 0)
            {
                guid = _lastFreeGuidObject.get(0);
                _lastFreeGuidObject.remove(0);
            }
            else
            {
                guid = _guidMaxObject;
                _guidMaxObject++;
            }
            object.setGuid(guid);
            _guidsCreate.add(object);
        }
    }
    
    public void removeObject(long guid)
    {
        if (_objectMap.containsKey(guid))
        {
            _guidsDestroy.add(guid);
        }
    }
    
    public ObjectMap getObjectMoveByGuid(long guid)
    {
        return _objectMap.get(guid);
    }
}