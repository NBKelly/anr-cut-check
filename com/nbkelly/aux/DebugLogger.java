package com.nbkelly.aux;

public interface DebugLogger {
    //expected commands:
    //DEBUG(level, message)
    //DEBUGF(level, message, args)
    public int DEBUG(String message); //default: 1
    public int DEBUG(int level, Object message);
    public int DEBUGF(int level, String message, Object... args);
    
    //print(value)
    //printf(value, args)
    //println(value)
    public int print(Object value);
    public int printf(String value, Object... args);
    public int println(Object value);
}
