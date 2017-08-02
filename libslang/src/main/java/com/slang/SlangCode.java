package com.slang;

/**
 * Created by syanochara on 02/08/2017.
 */

public class SlangCode {
    public int baseIndex=0;
    public String raw=null;
    public SlangContext context = null;

    public SlangCode(SlangContext context,String raw,int baseIndex) {
        this.baseIndex = baseIndex;
        this.raw = raw;
        this.context = context;
    }

    public SlangCode(){}
    public SlangCode(String raw, int baseIndex) {
        this.baseIndex = baseIndex;
        this.raw = raw;
    }

    public SlangReader getReader(){
        return new SlangReader(this.raw,this.baseIndex);
    }
}
