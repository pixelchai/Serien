package com.slang;

import java.io.*;
import java.util.*;

/**
 * Created by syanochara on 29/07/2017.
 */

public class Slang {
    public static SourceSlangFile getSl(){
        File file = new File(new File("").getAbsolutePath().concat("\\concept\\slang\\serienMAL2.sl"));
        StringBuilder sb = new StringBuilder();
        try {
            Scanner in = new Scanner(new FileReader(file));

            while (in.hasNext()) {
                sb.append(in.nextLine()+"\n");
            }
            in.close();
        }catch (FileNotFoundException ex){
            ex.printStackTrace();
            return null;
        }
        String raw = sb.toString();

        try {
            com.slang.SourceSlangFile f = new com.slang.SourceSlangFile(file.getName(), raw);

            for (Map.Entry<String,SlangMethod>e:f.methods.entrySet()) {
                System.out.println(e.getKey()+":"+"("+f.posFromIndex(e.getValue().baseIndex).toString()+")"+e.getValue().raw);
            }

            //f.init();
            //TODO DO THIS: java.net.URLEncoder.encode("Hello World", "UTF-8"));
            return f;
        }catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    public static void main(final String[] args){
        com.slang.SourceSlangFile f = getSl();
        try{
            //TODO
        }catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
            return;
        }
    }
}
