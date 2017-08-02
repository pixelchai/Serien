package com.slang;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SourceSlangFile extends SlangFile {
    public SourceSlangFile(String filename, String raw) throws Exception {
        super(filename, raw);
    }

    /**
     * Evaluates all the Home methods and then returns with values.
     * @deprecated use {@link #doHome()} instead and eval async.
     */
    @Deprecated
    public ArrayList<ArrayList<Object>> doEvalHome()throws Exception{
        ArrayList<ArrayList<Object>> ret = new ArrayList<ArrayList<Object>>();
        ArrayList<Object> l = (ArrayList<Object>)super.interpretMethod("home");
        for(Object o : l){
            if(o instanceof String){
                //methodname
                ret.add((ArrayList<Object>)super.interpretMethod((String)o,0)); //0 = home aka page 0
            }else{
                //expect array
                try {
                    ret.add((ArrayList<Object>) o);
                }catch (ClassCastException ex){
                    throw new SlangException("Expected list or string, found: "+String.valueOf(o));
                }
            }
        }
        return ret;
    }

    /**
     * @returns ArrayList with either String methodnames which point to methods which return ArrayList or an ArrayList - these ArrayLists define elements in UI
     */
    public ArrayList<Object> doHome()throws Exception{
        return (ArrayList<Object>)super.interpretMethod("home");
    }

    public ArrayList<ArrayList<String>> doSearch(String query) throws Exception {
        return (ArrayList<ArrayList<String>>)super.interpretMethod("search",query);
    }
}
