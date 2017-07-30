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
                "@rgx \"(https?|ftp):\\\\/\\\\/[^\\\\s/$.?#].[^\\\\s]*\"\n" +
                "-\n" +
                "\n" +
                "-home-[\"hseasonal\" \"htop\"]\n" +
                "\n" +
                "-seasonal_getstat\n" +
                "\t#eps [($text \n" +
                "\t\t\t([ ($ %0 \".eps\") 0)\n" +
                "\t\t\t) null]\n" +
                "\t#score [($trim ($text \n" +
                "\t\t\t([ ($ %0 \".score\") 0)\n" +
                "\t\t\t)) \"stars\"]\n" +
                "-[+eps +score]\n" +
                "\n" +
                "-hseasonal\n" +
                "\t#elm \".seasonal-anime\"\n" +
                "\t#b ($load($concat +base \"anime/season\"))\n" +
                "\n" +
                "\t#links *($attr ($ +b($concat +elm \" .title-text a\")) \"href\")\n" +
                "\t#titles *($text ($ +b($concat +elm \" .title-text\")))\n" +
                "\n" +
                "\t#imgelems ($ +b($concat +elm \" .image img\"))\n" +
                "\t#images *([($concat \n" +
                "\t\t\t\t($trim *(~ *($attr +imgelems \"data-srcset\") +rgx))\n" +
                "\t\t\t\t($trim *(~ *($attr +imgelems \"srcset\") +rgx))\n" +
                "\t\t\t)+highres)\n" +
                "\t#info *(seasonal_getstat($ +b +elm))\n" +
                "-[0 \"Seasonal Anime\" +links +titles +images +info null]\n" +
                "\n" +
                "-top_getstat\n" +
                "\t#eps [($trim ([ (~ ($html \n" +
                "\t\t\t([ ($ %0 \".information\") 0)\n" +
                "\t\t\t) \"[^<>]+\") 0)) null]\n" +
                "\t#score [($text \n" +
                "\t\t\t([ ($ %0 \".score\") 0)\n" +
                "\t\t\t) \"stars\"]\n" +
                "-[+eps +score]\n" +
                "\n" +
                "-htop\n" +
                "\t#b ($load($concat +base \"/topanime.php?limit=0\")) ;TODO pagination\n" +
                "\n" +
                "\t#links *($attr ($ +b \".ranking-list .title .clearfix .hoverinfo_trigger\") \"href\")\n" +
                "\t#titles *($text ($ +b \".ranking-list .title .clearfix\"))\n" +
                "\n" +
                "\t#imgelems ($ +b \".ranking-list img\")\n" +
                "\t#images *([($concat \n" +
                "\t\t\t\t($trim *(~ *($attr +imgelems \"data-srcset\") +rgx))\n" +
                "\t\t\t\t($trim *(~ *($attr +imgelems \"srcset\") +rgx))\n" +
                "\t\t\t)+highres)\n" +
                "\t#info *(top_getstat($ +b \".ranking-list\"))\n" +
                "-[3 \"Top Anime\" +links +titles +images +info \"htop\"]";
        try {
            com.slang.SourceSlangFile f = new com.slang.SourceSlangFile(null, file);

            for (Map.Entry<String,SlangMethod>e:f.methods.entrySet()) {
                System.out.println(e.getKey()+":"+"("+f.posFromIndex(e.getValue().baseIndex).toString()+")"+e.getValue().raw);
            }

            f.init();
            //TODO DO THIS: java.net.URLEncoder.encode("Hello World", "UTF-8"));
            ArrayList<Object> l = (ArrayList<Object>)f.interpretMethod("htop");
            System.out.println(l);
        }catch (Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
