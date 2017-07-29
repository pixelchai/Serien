package com.slang;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SlangException extends Exception {
    public SlangException(String file, Pos pos, String message, Exception inner){
        super(
                ((inner==null)? "Exception": inner.getClass().getSimpleName()) +
                        ((file!=null||pos!=null)?"@":"")+
                        ((file!=null)?file:"")+
                        ((pos!=null)?"(ln "+pos.y+", chr"+pos.x+")":"")+
                        ((message!=null)?" - "+message:"")+
                        ((inner instanceof SlangException)?" - "+inner.getMessage():""));
    }
    public SlangException(String message){
        super(message);
    }
    public SlangException(int x,String message){
        super("(chr "+x+") - "+message);
    }
}
