package com.slang;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;


/**
 * Created by syanochara on 27/07/2017.
 */

public class SlangEnv {
    private enum FileType{
        Base,
        Source,
    }
    public FileType[] FileTypes = FileType.values();

    public static Map<String, EnvMethod> methodDict = new HashMap<String, EnvMethod>();

    static{
        methodDict.put("load",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return Jsoup.connect((String)args[0]).timeout(20000).get();
            }
        });
        methodDict.put("sel",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((Element)args[0]).select((String)args[1]);
            }
        });
        methodDict.put("text",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((Element)args[0]).text();
            }
        });
        methodDict.put("attr",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((Element)args[0]).attr((String)args[1]);
            }
        });
        methodDict.put("trim",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((String)args[0]).trim();
            }
        });
        methodDict.put("concat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                String ret = "";
                for(Object str : args){
                    ret+=(String)str;
                }
                return ret;
            }
        });
        methodDict.put("precat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return args[1]+(String)args[0];
            }
        });
        methodDict.put("getat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((List)args[1]).get((int)args[0]);
            }
        });
//        methodDict.put("each",new EnvMethod() {
//            @Override public Object run(Object... args) throws Exception {
//                List l = (List)args[0];
//                List ret = new ArrayList();
//                for(Object o : l){
//                    ret.add(o);
//                }
//                return ret;
//            }
//        });
    }
}
