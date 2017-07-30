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
                "-home\n" +
                "-[\"hseasonal\"]\n" +
                "\n" +
                "-hseasonal\n" +
                "#b ($load ($concat +base \"anime/season\"))\n" +
                "#info *(seasonal_getstat ($ +b \".seasonal-anime\"))\n" +
                "#links *($attr ($ +b \".seasonal-anime .title-text a\") \"href\")\n" +
                "#titles *($text ($ +b \".seasonal-anime .title-text\"))\n" +
                "#imgelems ($ +b \".seasonal-anime .image img\")\n" +
                "#imgl1 ($trim *(~ *($attr +imgelems \"data-srcset\") \"(https?|ftp):\\\\/\\\\/[^\\\\s/$.?#].[^\\\\s]*\"))\n" +
                "#imgl2 ($trim *(~ *($attr +imgelems \"srcset\") \"(https?|ftp):\\\\/\\\\/[^\\\\s/$.?#].[^\\\\s]*\"))\n" +
                "#images *([ ($concat +imgl1 +imgl2) +highres)\n" +
                "-[0 \"Seasonal Anime\" +links +titles +images +info null]\n" +
                "\n" +
                "-seasonal_getstat\n" +
                "#eps [($text ([ ($ %0 \".eps\") 0)) null]\n" +
                "#score [($trim ($text ([ ($ %0 \".score\") 0))) \"stars\"]\n" +
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
