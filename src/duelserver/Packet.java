package duelserver;
import java.nio.ByteBuffer;

public class Packet
{
    ByteBuffer _buffer;
    private int _position;
    private int _totalLength;
    
    Packet(int opcode, int length)
    {
        _totalLength = 4 + 4 + length;
        _buffer = ByteBuffer.allocate(_totalLength);
        putInt(length);
        putInt(opcode);
    }
    
    Packet(byte[] byteValues)
    {
        _totalLength = byteValues.length;
        _position = 0;
        _buffer = ByteBuffer.allocate(_totalLength);
        _buffer = ByteBuffer.wrap(byteValues);
    }
    
    public void putInt(int value)
    {
        if (_position < _totalLength - 3)
        {
            _buffer.putInt(_position, value);
            _position = _position + 4;
        }
    }
    
    public void putByte(byte value)
    {
        if (_position < _totalLength)
        {
            _buffer.put(_position, value);
            _position = _position + 1;
        }
    }
    
    public void putString(String value)
    {
        if (_position < _totalLength)
        {
            int lengthChar = value.toCharArray().length;
            putInt(lengthChar);
            for(int i = 0; i < lengthChar; i++)
            {
                if (_position < _totalLength + 1)
                {
                    char charValue = value.charAt(i);
                    _buffer.putChar(_position, charValue);
                    _position = _position + 2;
                }
            }
        }
    }
    
    public void putLong(long value)
    {
        if (_position < _totalLength - 7)
        {
            _buffer.putLong(_position, value);
            _position = _position + 8;
        }
    }
    
    public int getInt()
    {
        if (_position < _totalLength - 3)
        {
            int value = _buffer.getInt(_position);
            _position = _position + 4;
            return value;
        }
        return 0;
    }
    
    public byte getByte()
    {
        if (_position < _totalLength)
        {
            
            byte value =  _buffer.get(_position);
            _position = _position + 1;
            return value;
        }
        return (byte) 0;
    }
    
    public String getString()
    {
        if (_position < _totalLength)
        {
            int charLength = getInt();
            char[] charValues = new char[charLength];
            for (int i = 0; i < charLength; i++)
            {
                if (_position < _totalLength - 1)
                {
                    charValues[i] = _buffer.getChar(_position);
                    _position = _position + 2;
                }
            }
            return new String(charValues);
        }
        return "";
    }
    
    public long getLong()
    {
        if (_position < _totalLength - 7)
        {
            long value = _buffer.getLong(_position);
            _position = _position + 8;
            return value;
        }
        return 0;
    }
    
    public byte[] getByteArray()
    {
        return _buffer.array();
    }
}
