package com.slang;

/**
 * Created by syanochara on 27/07/2017.
 */

public class Pos {
    public int x =0;
    public int y =0;

    public Pos(){}
    public Pos(int x, int y){
        this.x=x;
        this.y=y;
    }
    @Override
    public String toString(){
        return "("+x+","+y+")";
    }
}
