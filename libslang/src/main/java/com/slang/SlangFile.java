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
                        buf.raw+= readComment(sr);
                    }else{
                        readComment(sr); //skip comment
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
                            buf.raw+=sr.readUntil("--");
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
    private String readComment(SlangReader sr) throws SlangException {
        char c2 = sr.peek();
        if(c2 == '/'){
            //linear comment, read to \n
            return sr.readUntil('\n');

        }else if(c2=='*'){
            //multi comment, read to */ or EOF
            return sr.readUntil("*/");
        }else{
            throwExec(sr,c2);
            return null;
        }
    }

    public void init() throws Exception {
        this.interpretMethod("init");
    }
    public String getName() throws Exception {
        return (String)globalContext.getVariable("name");
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
            case '*':
                //lambda
                sr.increment();
                return interpretLambda(context,sr);
            case '[':
                //list
                List<Object> l = new ArrayList<Object>();
                sr.increment();
                while(sr.peek()!=']'){
                    sr.skipWhitespace();
                    l.add(this.interpretExpression(context,sr));
                    sr.skipWhitespace();
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
    private Tuple<Boolean,String> readMethodName(SlangContext context, SlangReader sr) throws SlangException {
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
            case '~':
                if(isEnv){
                    throw new SlangException(filename,posFromIndex(sr.getAbsIndex()),"Unexpected symbol '"+c+"' - the $ is probably not necessary",null);
                }
                isEnv = true;
                switch (c) {
                    case '[':
                        //indexer
                        name = "getat";
                        sr.increment();
                        break;
                    case '~':
                        //regex stuff
                        name="regex";
                        sr.increment();
                        if(sr.isWordChar(sr.peek())) {
                            name += sr.read();
                        }
                        break;
                }
                break;
            default:
                name=sr.readWord();
                break;
        }

        return new Tuple<Boolean,String>(isEnv,name);
    }

    private Object interpretCall(SlangContext context, SlangReader sr) throws Exception {
        Tuple<Boolean,String> mname = readMethodName(context,sr);
        boolean isEnv = mname.x;
        String name = mname.y;

        List<Object> args = new ArrayList<Object>();
        while(sr.peek()!=')'){
            sr.skipWhitespace();
            args.add(this.interpretExpression(context,sr));
            sr.skipWhitespace();
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

        Tuple<Boolean,String> mname = readMethodName(context,sr);
        boolean isEnv = mname.x;
        String name = mname.y;

        sr.skipWhitespace();
        List l = (List)this.interpretExpression(context,sr);

        List<Object> args = new ArrayList<Object>();
        while(sr.peek()!=')'){
            sr.skipWhitespace();
            if(sr.peek()==')')break;
            args.add(this.interpretExpression(context,sr));
            sr.skipWhitespace();
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
