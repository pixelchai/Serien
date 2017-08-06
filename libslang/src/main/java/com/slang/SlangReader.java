package com.slang;

/**
 * Created by syanochara on 27/07/2017.
 */

public class SlangReader {
    private int baseIndex = 0;
    private int index = 0;
    private String raw = null;

    public SlangReader(String str){
        this.raw=str;
    }
    public SlangReader(String str, int baseIndex) {
        this.raw=str;
        this.baseIndex = baseIndex;
    }

    public int getAbsIndex(){return baseIndex+index;}
    public String getRaw(){return raw;}

    public char getCur(){
        return look(0);
    }
    public char peek(){
        return look(0);
    }
    public char look(int off) {
        if(index+off<raw.length()) {
            return raw.charAt(index + off);
        }else{
            return Utils.NULL_CHAR;
        }
    }
    public char read(){
        try{
            return peek();
        }finally {
            this.increment();
        }
    }
    public int increment(){
        return increment(1);
    }
    public int increment(int x) {
        if(Utils.DEBUG_READER)System.out.println(this.getAbsIndex()+": "+look(0));
        index+=x;
        return index;
    }
    public boolean nextIsWhite() {
        char c = this.peek();
        return (c==' '||c=='\r' || c == '\n' || c == '\t');
    }
    public boolean nextIsWhiteLinear() {
        char c = this.peek();
        return (c==' '||c=='\r' || c == '\t');
    }
    public void skipWhitespace(){
        while(true){
            if(nextIsWhite())this.increment();
            else return;
        }
    }
    public void skipWhitespaceLinear(){
        while(true){
            if(nextIsWhiteLinear())this.increment();
            else return;
        }
    }

    public boolean isNext(String str){
        StringBuilder acc = new StringBuilder();
        for(int i = 0; i < str.length() && i < raw.length(); i++){
            acc.append(look(i));
        }
        return acc.toString().equals(str);
    }

    /**
     * reads until c found or EOF.
     */
    public String readUntil(char c, boolean include){
        StringBuilder sb = new StringBuilder();
        while(true){
            if(this.peek()==c){
                if(include)sb.append(c);
                this.increment();
                return sb.toString();
            }
            if(this.peek()==Utils.NULL_CHAR)return sb.toString();
            sb.append(read());
        }
    }
    /**
     * reads until str found or EOF. Includes.
     */
    public String readUntil(String str, boolean include){
        StringBuilder sb = new StringBuilder();
        while(true){
            if(this.peek()==Utils.NULL_CHAR)return sb.toString();
            if(this.isNext(str)) {
                if(include)sb.append(str);
                this.increment(str.length());
                return sb.toString();
            }
            sb.append(read());
        }
    }

    public boolean isWordChar(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' /*|| c == '-' *|| c == '+' || c == '.'*/;
    }
    public String readWord(){
        skipWhitespace();
        StringBuilder sb = new StringBuilder();
        if(!isWordChar(this.peek()))return null;
        while(true){
            char c = this.peek();
            if(!isWordChar(c))break;
            sb.append(this.read());
        }
        return sb.toString();
    }
//    public long readInt() throws SlangException {
//        String w = readWord();
//        if (w.equals("null")) return Utils.NULL_INT;
//        long v = Long.parseLong(w);
//        if (!(v >= Utils.MIN_INT && v <= Utils.MAX_INT))
//        {
//            throw new SlangException(this.getAbsIndex(),"Int out of bounds: " + v);
//        }
//        return v;
//    }
//    public double readDouble()
//    {
//        String w = readWord();
//        if (w.equals("null")) return Utils.NULL_DOUBLE;
//        return Double.parseDouble(w);
//    }
    public String readString() throws SlangException {
        skipWhitespace();
        char c = this.read();
        if (c != '"')
        {
            if (readWord() == "ull") return null;
            else throw new SlangException(this.getAbsIndex(),"String literal expected!");
        }
        StringBuilder sb = new StringBuilder();
        while (true)
        {
            switch (c = this.read())
            {
                case '"':return sb.toString();
                case '\\':
                    switch (c = this.read())
                    {
                        case 'n':sb.append('\n'); break;
                        case 'r':sb.append('\r'); break;
                        case 'x':
                            sb.append((char)Integer.parseInt((this.read() + ""+this.read()), 16));
                            break;
                        default: sb.append(c); break;
                    }
                    break;
                case Utils.NULL_CHAR:throw new SlangException(this.getAbsIndex(),"Unexpected null/terminator");
                default: sb.append(c); break;
            }
        }
    }


}
