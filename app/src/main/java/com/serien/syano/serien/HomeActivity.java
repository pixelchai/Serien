package com.serien.syano.serien;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slang.Slang;
import com.slang.SourceSlangFile;
import com.slang.Tuple;

import java.util.ArrayList;

//TODO https://stackoverflow.com/questions/8288218/how-to-wait-for-a-thread-to-finish-before-another-thread-starts-in-java-android
public class HomeActivity extends AppCompatActivity {

    private android.support.v7.widget.SearchView searchView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("serien","raa");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //load slang files
        //TODO actually do
        generateHome(Slang.getSl());

//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    ArrayList<ArrayList<Object>> l = Slang.getSl().doHome();
//                    System.out.println(l);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

        }

    public void generateHome(SourceSlangFile sl){
        new Thread(new Runnable() {
            public void run() {
                try {
                    final SourceSlangFile sl = Slang.getSl();
                    final ArrayList<Object> l = sl.doHome();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            generateHome(sl,l);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void generateHome(final SourceSlangFile sl, ArrayList<Object> secs){
        final LinearLayout lay = (LinearLayout)findViewById(R.id.home_lay);

        //add prog bar
        final ProgressBar prog = new ProgressBar(this);
        lay.addView(prog);

        final int secCount = secs.size();

        //listener for when everything fin, rem prog bar
        ((ViewGroup)lay).setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                if(lay.getChildCount()-1==secCount){
                    //rem prog
                    lay.removeView(prog);
                }
            }
            @Override
            public void onChildViewRemoved(View parent, View child) {}
        });

        int i = 0;
        for(Object obj : secs){
            final Tuple<Object,Integer> sec = new Tuple<Object, Integer>(obj,i);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<Object> args = null;
                        if (sec.x instanceof String) {
                            //method name
                            args = (ArrayList<Object>)sl.interpretMethod((String) sec.x, 0);//0 = home aka page 1
                        } else {
                            //array
                            args = (ArrayList<Object>) sec.x;
                        }
                        //args.add(sec.y);
                        final ArrayList<Object> finalargs = args;

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                View v = generateSection(finalargs);

                                int tot = lay.getChildCount()-1;//not incl spinner
                                if(sec.y<=tot){
                                    //add to top
                                    lay.addView(v,0);
                                }else if(sec.y>tot){
                                    //add just under spinner
                                    lay.addView(generateSection(finalargs), tot);
                                }
                            }
                        });


                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
            i++;
        }
    }

    public View generateSection(ArrayList<Object> args){
        String s = String.valueOf(args.get(1));
        TextView t = new TextView(this);
        t.setText(s);
        return t;
    }

    protected int getIdentifier(String literalId) {
        return getResources().getIdentifier(
                String.format("android:id/%s", literalId),
                null,
                null
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //IMPORTANT! NB: Use android.support.v7.widget.SearchView NOT android.widget.SearchView
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        searchView = (android.support.v7.widget.SearchView)item.getActionView();

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;//TODO
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void resetSearchView(){
        if(searchView!=null) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setIconified(true);
                return;
        }
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        resetSearchView();
    }
}
