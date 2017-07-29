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
                String file = ";ra\n" +
                        "-init\n" +
                        ";reie\n" +
                        "@title \"animeheaven\"\n" +
                        "@by \"syanochara\"\n" +
                        "@type 1\n" +
                        "@base \"http://animeheaven.eu/\"\n" +
                        "-\n" +
                        "-yo-\"yo\"\n" +
                        "\n" +
                        "-search\n" +
                        "#e (yo)\n" +
                        "#b ($load ($concat \"http://animeheaven.eu/search.php?q=\" %0))\n" +
                        ";#names ([ 1 ($ +b \".iepdes\"))\n" +
                        "#names ~($text ($ +b \".iepdes\"))\n" +
                        "#links ~($precat ~($attr ($ +b \".ieppic .an\") \"href\") +base)\n" +
                        "#imgs ~($precat ~($attr ($ +b \".ieppic .an img\") \"src\") +base)\n" +
                        "- [+links +names +imgs]\n" +
                        "\n" +
                        ";-search- [(~ ($ ($load \"url\") \".iepdes\") innerText) (~ ($ ($load \"url\") \".iepdes\") innerText) (~ ($ ;($load \"url\") \".ieppic .an\") getAttr \"src\") (~ ($ ($load \"url\") \".ieppic .an img\") getAttr \"src\")]\n" +
                        "\n";
                try {
                    com.slang.SourceSlangFile f = new com.slang.SourceSlangFile(null, file);

                    for (Map.Entry<String,SlangMethod>e:f.methods.entrySet()) {
                        Log.d("serien",e.getKey()+":"+"("+f.posFromIndex(e.getValue().baseIndex).toString()+")"+e.getValue().raw);
                    }

                    f.init();
                    //TODO DO THIS: java.net.URLEncoder.encode("Hello World", "UTF-8"));
                    ArrayList<ArrayList<String>> l = f.doSearch("fullmetal");
                    System.out.println(l);
                }catch (Exception e){
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();




    }
}
