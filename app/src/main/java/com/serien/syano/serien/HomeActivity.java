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
//TODO https://stackoverflow.com/questions/8288218/how-to-wait-for-a-thread-to-finish-before-another-thread-starts-in-java-android
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("serien","raa");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        new Thread(new Runnable() {
            public void run() {
                //TODO
            }
        }).start();




    }
}
