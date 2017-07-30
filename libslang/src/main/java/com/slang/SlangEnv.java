package com.slang;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
        //TODO more arithmetic
        methodDict.put("mult",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return ((Number)args[0]).doubleValue()*((Number)args[1]).doubleValue();
            }
        });
        methodDict.put("div",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return ((Number)args[0]).doubleValue()/((Number)args[1]).doubleValue();
            }
        });
        methodDict.put("add",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return ((Number)args[0]).doubleValue()+((Number)args[1]).doubleValue();
            }
        });
        methodDict.put("sub",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return ((Number)args[0]).doubleValue()-((Number)args[1]).doubleValue();
            }
        });
        methodDict.put("idiv",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return (((Number)args[0]).intValue())/(((Number)args[1]).intValue());
            }
        });
        methodDict.put("imult",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return (((Number)args[0]).intValue())*(((Number)args[1]).intValue());
            }
        });
        methodDict.put("iadd",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return (((Number)args[0]).intValue())+(((Number)args[1]).intValue());
            }
        });
        methodDict.put("isub",new EnvMethod() {
            @Override public Object run(Object... args) throws IOException {
                return (((Number)args[0]).intValue())-(((Number)args[1]).intValue());
            }
        });

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
        methodDict.put("html",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((Element)args[0]).html();
            }
        });
        methodDict.put("attr",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((Element)args[0]).attr((String)args[1]);
            }
        });
        methodDict.put("trim",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                try {
                    //string trim
                    return ((String) args[0]).trim();
                }catch (ClassCastException e){
                    //trim array list of "empty" items
                    ArrayList<Object> l =((ArrayList<Object>)args[0]);
                    ArrayList<Object> ret = new ArrayList<Object>();
                    //trim start
                    boolean done=false;
                    for(int i = 0; i<l.size();i++){
                        Object o = l.get(i);
                        if(!done) {
                            if (!isEmpty(o)) {
                                done=true;
                                ret.add(o);
                            }
                            //else ignore
                        }else{
                            ret.add(o);
                        }
                    }
                    //trim end
                    for (int i = ret.size() - 1; i >= 0; i--)
                    {
                        if(isEmpty(ret.get(i))){
                            ret.remove(i);
                        }else{
                            break;
                        }
                    }
                    return ret;
                }
            }
        });
        methodDict.put("concat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                try{
                    //array concatenation
                    ArrayList<Object> ret = new ArrayList<Object>();
                    for(Object arg:args){
                        ret.addAll((ArrayList<Object>)arg);
                    }
                    return ret;
                }catch (ClassCastException ex){
                    //string concatenation
                    String ret = "";
                    for (Object str : args) {
                        ret += String.valueOf(str);
                    }
                    return ret;
                }
            }
        });
        methodDict.put("precat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return args[1]+(String)args[0];
            }
        });
        methodDict.put("getat",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                return ((AbstractList)args[0]).get((int)args[1]);
            }
        });
        methodDict.put("regex",new EnvMethod() {
            @Override public Object run(Object... args) throws Exception {
                List<String> allMatches = new ArrayList<String>();
                Matcher m = Pattern.compile((String)args[1])
                        .matcher((String)args[0]);
                while (m.find()) {
                    allMatches.add(m.group());
                }
                return allMatches;
            }
        });
        //TODO more regex stuff
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

    private static boolean isEmpty(Object o){
        //TODO more types
        boolean isempty = false;
        try {
            isempty = ((AbstractList)o).size()<=0;
        }catch (ClassCastException ex)
        {
            isempty=((int)o)==0;
            //let it throw ClassCastException if needed
        }
        return isempty;
    }
}
