package com.slang;

import java.util.*;

/**
 * Created by syanochara on 29/07/2017.
 */

public class Slang {
    public static void main(final String[] args){
        String file = "-init\n" +
                "@title \"MyAnimeList\"\n" +
                "@by \"syanochara\"\n" +
                "@type 1\n" +
                "@base \"https://myanimelist.net/\"\n" +
                "@highres 1\n" +
                "-\n" +
                "\n" +
                "-home-[\"hseasonal\"]\n" +
                "\n" +
                "-hseasonal\n" +
                "\t#rgx \"(https?|ftp):\\\\/\\\\/[^\\\\s/$.?#].[^\\\\s]*\"\n" +
                "\t#elm \".seasonal-anime\"\n" +
                "\t#b ($load($concat +base \"anime/season\"))\n" +
                "\n" +
                "\t#info *(seasonal_getstat($ +b +elm))\n" +
                "\t#links *($attr ($ +b($concat +elm \" .title-text a\")) \"href\")\n" +
                "\t#titles *($text ($ +b($concat +elm \" .title-text\")))\n" +
                "\n" +
                "\t#imgelems ($ +b($concat +elm \" .image img\"))\n" +
                "\t#images *([($concat \n" +
                "\t\t\t($trim *(~ *($attr +imgelems \"data-srcset\") +rgx))\n" +
                "\t\t\t($trim *(~ *($attr +imgelems \"srcset\") +rgx))\n" +
                "\t\t\t)+highres)\n" +
                "-[0 \"Seasonal Anime\" +links +titles +images +info null]\n" +
                "\n" +
                "-seasonal_getstat\n" +
                "\t#eps [($text \n" +
                "\t\t([ ($ %0 \".eps\") 0)\n" +
                "\t\t) null]\n" +
                "\t#score [($trim ($text \n" +
                "\t\t([ ($ %0 \".score\") 0)\n" +
                "\t\t)) \"stars\"]\n" +
                "- [+eps +score]";
        try {
            com.slang.SourceSlangFile f = new com.slang.SourceSlangFile(null, file);

            for (Map.Entry<String,SlangMethod>e:f.methods.entrySet()) {
                System.out.println(e.getKey()+":"+"("+f.posFromIndex(e.getValue().baseIndex).toString()+")"+e.getValue().raw);
            }

            f.init();
            //TODO DO THIS: java.net.URLEncoder.encode("Hello World", "UTF-8"));
            ArrayList<Object> l = (ArrayList<Object>)f.interpretMethod("hseasonal");
            System.out.println(l);
        }catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
