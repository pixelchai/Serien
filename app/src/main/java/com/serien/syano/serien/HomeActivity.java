package com.serien.syano.serien;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.slang.SlangEnv;
import com.slang.SlangMethod;

import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("serien","raa");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    Log.d("serien","doing thing");
//                    Document d = (Document)SlangEnv.methodDict.get("load").run("https://myanimelist.net/anime/season");
//                    Log.d("serien","did thing");
//                    String str = d.html();
//                    Log.d("serien",str);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

//TODO https://stackoverflow.com/questions/8288218/how-to-wait-for-a-thread-to-finish-before-another-thread-starts-in-java-android
        new Thread(new Runnable() {
            public void run() {
                String file = "-init\n" +
                        "@title \"MyAnimeList\"\n" +
                        "@by \"syanochara\"\n" +
                        "@type 1\n" +
                        "@base \"https://myanimelist.net/\"\n" +
                        "-\n" +
                        "\n" +
                        "-home\n" +
                        "-[\"hseasonal\"]\n" +
                        "\n" +
                        "-hseasonal\n" +
                        "#b ($load ($concat +base \"anime/season\"))\n" +
                        "#links ~($attr ($ b \".seasonal-anime .title-text a\") \"href\")\n" +
                        "#titles ~($text ($ b \".seasonal-anime .title-text\"))\n" +
                        "#images ~($attr ($ b \".seasonal-anime .image img\") \"src\")\n" +
                        "#info ~(seasonal_getstat ($ b \".seasonal-anime\"))\n" +
                        "-[0 +links +titles +images +info null]\n" +
                        "\n" +
                        "-seasonal_getstat\n" +
                        "#eps [($text ([ ($ %0 \".eps\"))) null]\n" +
                        "#score [($trim ($text ([ ($ %0 \".score\")))) \"stars\"]\n" +
                        "- [+eps +score]";
                try {
                    com.slang.SourceSlangFile f = new com.slang.SourceSlangFile(null, file);

                    for (Map.Entry<String,SlangMethod>e:f.methods.entrySet()) {
                        Log.d("serien",e.getKey()+":"+"("+f.posFromIndex(e.getValue().baseIndex).toString()+")"+e.getValue().raw);
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
        }).start();




    }
}
