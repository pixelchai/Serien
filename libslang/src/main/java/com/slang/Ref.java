package com.slang;

/**
 * Created by syanochara on 06/08/2017.
 */

public class Ref {
    public String name;

    public Ref(String name) {
        this.name = name;
    }
}

class VarRef extends Ref{
    public boolean isParam;
    public boolean isGlobal;

    public VarRef(String name, boolean isParam, boolean isGlobal) {
        super(name);
        this.isParam = isParam;
        this.isGlobal = isGlobal;
    }
}

class MethodRef extends Ref{
    public boolean isEnv;

    public MethodRef(String name, boolean isEnv) {
        super(name);
        this.isEnv = isEnv;
    }
}