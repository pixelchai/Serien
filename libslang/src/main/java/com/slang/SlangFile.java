package com.slang;

import java.util.AbstractList;
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

    public SlangFile(String filename, String raw) throws Exception {
        this.raw = raw/*.replace('\u201C','"').replace('\u201D','"')*/;
        this.filename = filename;
        this.parseMethods();
    }
    public Pos posFromIndex(int index){
        String[] l = raw.substring(0,index).split("\n");
        return new Pos(l[l.length-1].length(),l.length);
    }

    private void throwExec(SlangReader sr, String message, Exception inner) throws SlangException {
        throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),message,inner);
    }
    private void throwExec(SlangReader sr, String message) throws SlangException {
        throwExec(sr,message,null);
    }
    private void throwExec(SlangReader sr, Character symbol) throws SlangException{
        throwExec(sr,"Unexpected character '"+symbol+"'");
    }
    private void throwExec(String message,Exception inner) throws SlangException{
        throw new SlangException(filename,null,message,inner);

    }
    private void throwExec(String message) throws SlangException{
        throwExec(message,null);
    }

    private void parseMethods() throws Exception {
        SlangReader sr = new SlangReader(this.raw);

        SlangMethod buf = new SlangMethod();
        buf.raw = "";
        boolean open = false;

        while(true){
            if(!open)sr.skipWhitespace();

            char c = sr.peek();
            if(c==Utils.NULL_CHAR){
                //ending
                break;
            }

            if(!open) {
                if (!(sr.isNext("--")||sr.isNext("/*")||sr.isNext("//")))throwExec(sr,c);
            }

            switch (c){
                case '/':
                    sr.increment();
                    if(open){
                        buf.raw+=c;
                        buf.raw+= readComment(sr,true);
                    }else{
                        readComment(sr,false); //skip comment
                    }
                    break;
                case '"':
                    buf.raw += "\""+sr.readString()+"\"";//read string
                    break;
                case '-':
                    if(sr.isNext("--")) {
                        sr.increment(2);

                        if(!open){
                            //opening
                            buf.name=sr.readWord();
                            buf.baseIndex = sr.getAbsIndex();
                            open = true;
                        }else{
                            buf.raw+="--";
                            //closing
                            buf.raw+=sr.readUntil("--",true);
                            methods.put(buf.name,buf);

                            //reset
                            buf = new SlangMethod();
                            buf.raw = "";
                            open = false;
                        }
                        break;
                    }
                    //else fall through to default
                default:
                    buf.raw+=c;
                    sr.increment();
                    break;
            }
        }

    }

    /**
     * no head
     */
    private String readComment(SlangReader sr, boolean incl) throws SlangException {
        char c2 = sr.peek();
        if(!incl)sr.increment(); //skip first char
        if(c2 == '/'){
            //linear comment, read to \n
            return sr.readUntil('\n',incl);

        }else if(c2=='*'){
            //multi comment, read to */ or EOF
            return sr.readUntil("*/",incl);
        }else{
            throwExec(sr,c2);
            return null;
        }
    }

    public void init() throws Exception {
        this.interpretMethod("init");
    }
    public String getName() throws Exception {
        return (String)getVar(globalContext,new Ref("name"));
    }

    public Object interpretMethod(SlangMethod body) throws Exception {
        SlangReader sr = body.getReader();
        while(true){
            sr.skipWhitespace();
            char c = sr.peek();
            switch (c) {
                case '/':
                    //comment
                    sr.increment();
                    String comment = readComment(sr,false); //read comment
                    //TODO maybe do stuff
                    break;
                case '-':
                    //return statement
                    if(sr.isNext("--")) {
                        sr.increment(2);
                        sr.skipWhitespace();
                        return this.interpretExpr( body.context,sr);
                        //next will be the unread -- but reading that is not necessary since nothing is after that.
                    }
                    //else fall through to default
                default:
                    //expr
                    interpretExpr(body.context,sr);
                    break;
            }
        }
    }

    public Object interpretMethod(String name, Object... args) throws Exception {
        SlangMethod method = methods.get(name);

        if(method == null){
            throwExec("Could not find method \""+name+"\"");
        }

        method.context = new SlangContext(globalContext);
        for (int i = 0; i < args.length; i++) {
            method.context.params.put(Integer.toString(i), args[i]);
        }

        return interpretMethod(method);
    }

    private Object interpretExpr(SlangCode expr) throws Exception {
        return interpretExpr(expr.context, expr.getReader());
    }
    private Object interpretExpr(SlangContext context, SlangReader sr) throws Exception{
        sr.skipWhitespace();
        if(sr.isNext("null"))return null;

        Object expr = ainterpretExpr(context,sr);
        if(expr == null)return null;
        else {
            //indexer
            ArrayList<Object> indexerArgs = new ArrayList<Object>();

            if (expr instanceof AbstractList || expr instanceof Map) {
                sr.skipWhitespace();
                if (sr.peek() == '[') {
                    sr.increment();
                }
                indexerArgs.add(this.interpretExpr(context, sr));
                while (true) {
                    if (sr.peek() == ',') {
                        sr.increment();
                        indexerArgs.add(this.interpretExpr(context, sr));
                    } else if (sr.peek() == ']') {
                        sr.increment();
                        break;
                    }
                }

                if (expr instanceof AbstractList) {
                    expr = ((AbstractList) expr).get((int) indexerArgs.get(0));
                } else {
                    expr = ((Map) expr).get(indexerArgs.get(0));
                }
            }
        }
        //operators
        //TODO

        return expr;
    }
    private Object callMethod(SlangContext context, Ref r, ArrayList<Object> args) throws Exception {
        boolean isEnv = false;
        if(r instanceof MethodRef){
            isEnv = ((MethodRef)r).isEnv;
        }
        if(isEnv){
            return SlangEnv.methodDict.get(r.name).run(args.toArray());
        }else{
            return this.interpretMethod(r.name,args.toArray());
        }
    }
    private Object getVar(SlangContext context, Ref r) throws SlangException {
        boolean isParam = false;
        if(r instanceof VarRef){
            isParam = ((VarRef)r).isParam;
        }
        return (isParam)?context.getParam(r.name):context.getVariable(r.name);
    }
    private void setVar(SlangContext context, Ref r, Object o){
        boolean isParam = false;
        boolean isGlobal = false;
        if(r instanceof VarRef){
            isParam = ((VarRef)r).isParam;
            isGlobal = ((VarRef)r).isGlobal;
        }
        SlangContext c = context;
        if(isGlobal){
            c = context;
            while(c.parentContext != null){
                c = c.parentContext;
            }
        }
        if(isParam){
            c.params.remove(r.name);
            c.params.put(r.name,o);
        }else{
            c.variables.remove(r.name);
            c.variables.put(r.name,o);
        }
    }
    private VarRef readVarRefBase(SlangReader sr){
        sr.skipWhitespace();
        VarRef ret = new VarRef(null,false,false);
        if(sr.peek()=='^'){
            sr.increment();
            ret.isGlobal=true;
        }else if(sr.peek()=='%'){
            sr.increment();
            ret.isParam=true;
        }
        else return null;
        return ret;
    }
    private MethodRef readMethodRefBase(SlangReader sr){
        sr.skipWhitespace();
        MethodRef ret = new MethodRef(null, false);
        if(sr.peek()=='$') {
            sr.increment();
            ret.isEnv = true;
        }else return null;
        return ret;
    }

    private Object ainterpretExpr(SlangContext context, SlangReader sr) throws Exception {
        //TODO @ stuff and ? stuff
        char c = sr.peek();
        switch (c) {
            case '(':
                //bracketed expr
                sr.increment();
                Object expr = this.interpretExpr(context, sr);
                //expect )
                if (sr.read() != ')') this.throwExec(sr, ')');
                return expr;
            case '[':
                //list literal
                sr.increment();
                ArrayList<Object> ret = new ArrayList<Object>();
                if (sr.peek() == ']') {
                    sr.increment();
                    return ret; //empty array
                } else {
                    ret.add(this.interpretExpr(context, sr));
                    while (true) {
                        if (sr.peek() == ',') {
                            sr.increment();
                            ret.add(this.interpretExpr(context, sr));
                        } else if (sr.peek() == ']') {
                            sr.increment();
                            return ret;
                        }
                    }
                }
            case '"':
                //string literal
                return sr.readString();
            default:
                if (sr.isNext("true")) return true;
                else if (sr.isNext("false")) return false;
                else {
                    Object n = sr.readNumber();
                    if (n != null) return n;
                    else {
                        //method call, var get, var set
                        Ref ref;
                        if ((ref = readVarRefBase(sr)) == null) {
                            if ((ref = readMethodRefBase(sr)) == null) {
                                //neither varrefbase nor methodrefbase
                                ref = new Ref(null);
                            }
                        }
                        sr.skipWhitespace();//just in case

                        String w = sr.readWord();
                        if (w == null) throwExec(sr, "Could not interpret expression");
                        ref.name = w;

                        sr.skipWhitespace();

                        //2nd part if any
                        if (sr.peek() == '(') {
                            //methodcall
                            sr.increment();

                            ArrayList<Object> args = new ArrayList<Object>();
                            if (sr.peek() == ')') {
                                sr.increment();
                                //empty args
                            } else {
                                args.add(this.interpretExpr(context, sr));
                                while (true) {
                                    if (sr.peek() == ',') {
                                        sr.increment();
                                        args.add(this.interpretExpr(context, sr));
                                    } else if (sr.peek() == ')') {
                                        sr.increment();
                                        //done
                                        break;
                                    }
                                }
                            }
                            return callMethod(context, ref, args);
                        } else if (sr.peek() == '=') {
                            //var set
                            sr.increment();
                            Object o = this.interpretExpr(context, sr);
                            setVar(context, ref, o);
                            return o;

                        } else {
                            //var get
                            return getVar(context, ref);
                        }
                    }
                }

        }
    }
}
