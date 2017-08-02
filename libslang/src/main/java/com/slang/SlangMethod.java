package com.slang;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SlangMethod extends SlangCode {
    public String name=null;

    public SlangMethod(){}
    public SlangMethod(String name, String raw, int baseIndex) {
        super(raw, baseIndex);
        this.name = name;
    }
}
