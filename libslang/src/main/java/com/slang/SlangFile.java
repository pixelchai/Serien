package com.slang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SlangFile {
    public enum BaseMethods{
        gethome,
    }
    public enum SourceMethods{
        search,
    }

    private String raw = null;
    public String filename = null;
    public Map<String, SlangMethod> methods = new HashMap<String, SlangMethod>();

    public SlangContext globalContext = new SlangContext();

    public SlangFile(String filename, String raw){
        this.raw = raw/*.replace('\u201C','"').replace('\u201D','"')*/;
        this.filename = filename;
        this.parseMethods();
    }
    public Pos posFromIndex(int index){
        String[] l = raw.substring(0,index).split("\n");
        return new Pos(l[l.length-1].length(),l.length);
    }
    private void parseMethods(){
        SlangMethod buf = new SlangMethod();
        buf.raw = "";
        boolean open = false;
        boolean finnext = false;
        String name = null;

        for(int i = 0; i < raw.length(); i++){
            char cur = raw.charAt(i);
            if(cur==';'){
                for(i=i;i<raw.length();i++){
                    char c = raw.charAt(i);
                    if(open) {
                        buf.raw += c;
                    }
                    if(c=='\n'){
                        break;
                    }
                }
            }
            else if(cur=='\"'){
                buf.raw+=cur;
                i+=1;
                //skip to end of quote
                for(i=i;i<raw.length();i++){
                    char c = raw.charAt(i);
                    buf.raw+=c;
                    if(c=='\\'){
                        i+=1;
                        buf.raw+=raw.charAt(i);//if this is a " it will be added and skipped (since it is not the end of string)
                    }else if(c=='"'){
                        break;
                    }
                }
            }
            else if(cur=='-'){
                if(!open){
                    open = true;
                    name = "";
                }else{
                    buf.raw+='-';
                    if(name != null) {
                        buf.name = name.trim();
                        name = null;
                        buf.baseIndex=i;
                    }
                    open = false;
                    finnext=true;
                }
            }
            else if((cur=='\n'||i==raw.length()-1)&&finnext){
                if(i==raw.length()-1)buf.raw+=cur;
                //fin
                finnext=false;
                methods.put(buf.name,buf);
                buf = new SlangMethod();
                buf.raw = "";
            }
            else if(cur=='\n'){
                if(name != null) {
                    buf.name = name.trim();
                    name = null;
                    buf.baseIndex=i;
                }else{
                    buf.raw+='\n';
                }
            }
            else if(name != null){
                name += cur;
            }else{
                buf.raw+=cur;
            }
        }
    }

    public void init() throws Exception {
        this.interpretMethod("init");
    }
    public Object interpretMethod(String name, Object... args) throws Exception {
        SlangMethod method = methods.get(name);
        if(method == null){
            throw new SlangException(filename,null,"Could not find method \""+name+"\"",null);
        }
        //load method
            SlangReader sr = new SlangReader(method.raw, method.baseIndex);
        try {
            SlangContext context = new SlangContext(globalContext);
            for (int i = 0; i < args.length; i++) {
                context.params.put(Integer.toString(i), args[i]);
            }
            while (true) {
                sr.skipWhitespace();
                char c = sr.peek();
                switch (c) {
                    case ';':
                        //comment
                        sr.increment();
                        while (sr.read() != '\n') {
                        } //skip to \n
                        break;
                    case '@':
                        //global variable definition
                        sr.increment();
                        this.interpretVariable(globalContext, sr);
                        break;
                    case '#':
                        //local variable definition
                        sr.increment();
                        this.interpretVariable(context, sr);
                        break;
                    case '-':
                        //return statement
                        sr.increment();
                        sr.skipWhitespaceLinear();
                        char a = sr.peek();
                        if (a == '\n' || a == Utils.NULL_CHAR) {
                            return null;
                        } else {
                            //not void
                            return this.interpretExpression(context, sr);
                        }
                    default:
                        //method call
//                    sr.increment();
//                    interpretCall(context,sr);
                        this.interpretExpression(context, sr);
//                    break;
//                case '~':
//                    sr.increment();
                        //lambda
//                    this.interpretExpression(context,sr);
                        break;
                }
            }
        }catch (Exception e){
            throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),null,e);
        }
    }
    private Object interpretExpression(SlangContext context, SlangReader sr) throws Exception {
        sr.skipWhitespace();
        char c = sr.peek();
        switch (c) {
            case '(':
                sr.increment();
                return interpretCall(context, sr);
            case '~':
                //lambda
                sr.increment();
                return interpretLambda(context,sr);
            case '[':
                //list
                List<Object> l = new ArrayList<Object>();
                sr.increment();
                while(sr.peek()!=']'){
                    sr.skipWhitespaceLinear();
                    l.add(this.interpretExpression(context,sr));
                }
                //expect close bracket
                if(sr.read()!=']')throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected ']'",null);
                return l;
            case '%':
                //parameter access
                sr.increment();
                return context.getParam(sr.readWord());
            case '+':
                //local variable access
                sr.increment();
                //System.out.println(context.variables);
                return context.getVariable(sr.readWord());
            case '"':
                //string literal
                return sr.readString();
            case Utils.NULL_CHAR:
                //unexpected EOF
                throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Unexpected EOF",null);
            default:
                String w = sr.readWord();
                if(w.equals("null"))return null;//null
                //int
                long v;
                try {
                    v = Long.parseLong(w);
                    if ((v >= Utils.MIN_INT && v <= Utils.MAX_INT))
                    {
                        return (int)v;
                    }
                }catch (Exception e){}
                //double
                try {
                    return Double.parseDouble(w);
                }catch (Exception e){}

                throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Unknown symbol",null);
        }
    }

    private Object interpretCall(SlangContext context, SlangReader sr) throws Exception {
        boolean isEnv = false;
        if(sr.peek()=='$'){
            isEnv=true;
            sr.increment();
        }

        String name = "";
        char c = sr.peek();
        switch (c){
            case ' ':
                //short form for sel
                if(isEnv) {
                    name = "sel";
                }else{
                    throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected method name",null);
                }
                break;
            case '[':
                //indexer
                if(isEnv){
                    throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Unexpected symbol '[' - when doing an indexer call, you don't need the $ before the [",null);
                }
                isEnv = true;
                name = "getat";
                sr.increment();
                break;
            default:
                name=sr.readWord();
                break;
        }

        List<Object> args = new ArrayList<Object>();
        while(sr.peek()!=')'){
            sr.skipWhitespaceLinear();
            args.add(this.interpretExpression(context,sr));
        }
        //expect close bracket
        if(sr.read()!=')')throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected ')'",null);

        if(isEnv){
            return SlangEnv.methodDict.get(name).run(args.toArray());
        }else{
            return interpretMethod(name,args);
        }
    }
    private Object interpretLambda(SlangContext context, SlangReader sr)throws Exception{
        //expect open bracket
        if(sr.read()!='(')throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected '('",null);

        boolean isEnv = false;
        if(sr.peek()=='$'){
            isEnv=true;
            sr.increment();
        }

        String name = "";
        char c = sr.peek();
        switch (c){
            case ' ':
                //short form for sel
                if(isEnv) {
                    name = "sel";
                    sr.increment();
                }else{
                    throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected method name",null);
                }
                break;
            case '[':
                //indexer
                name = "getat";
                sr.increment();
                break;
            default:
                name=sr.readWord();
                break;
        }
        sr.skipWhitespace();
        List l = (List)this.interpretExpression(context,sr);

        List<Object> args = new ArrayList<Object>();
        while(sr.peek()!=')'){
            sr.skipWhitespaceLinear();
            if(sr.peek()==')')break;
            args.add(this.interpretExpression(context,sr));
        }
        //expect closed bracket
        if(sr.read()!=')')throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Expected ')'",null);

        List<Object> ret = new ArrayList<Object>();
        for(Object x:l){
            Object[] arr = new Object[args.size()+1];
            arr[0]=x;
            for(int i = 0; i < args.size(); i++){
                arr[i+1] = args.get(i);
            }//init args
            if(isEnv){
                ret.add(SlangEnv.methodDict.get(name).run(arr));
            }else{
                ret.add(interpretMethod(name,arr));
            }
        }
        return ret;
    }
    private void interpretVariable(SlangContext context, SlangReader sr) throws Exception {
        context.variables.put(sr.readWord(), this.interpretExpression(context, sr));
    }
}
