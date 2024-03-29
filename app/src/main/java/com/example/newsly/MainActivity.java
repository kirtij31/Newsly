package com.example.newsly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MoreClickListenerInterface, ChildImageListenerInterface, ConnectionListener{

    String news_url = "https://newsapi.org/v2/top-headlines?country=us&apiKey=4c5644188b9042008babda3162b2aa6e";

    RecyclerView recyclerView;

    ParentAdapter parentAdapter ;

    ArrayList<ChildModel> childModels;
    ArrayList<ParentModel> parentModels = new ArrayList<>();

    public  static ProgressBar progressBar;
    public  static TextView textView;

    BroadcastReceiver broadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().show();

        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.connectionStatus);

        recyclerView = findViewById(R.id.parent_rv);
        parentAdapter = new ParentAdapter(parentModels, this,this,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(parentAdapter);

        broadcastReceiver = new ConnectionReceiver(this, this);

        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public void fetch(){

        HashMap<String,ArrayList<ChildModel>> map = new HashMap<>();

        Call<NewsList> newsCall = NewsService.getInstance().myMemeInterface().getNews("in");
        newsCall.enqueue(new Callback<NewsList>() {
            @Override
            public void onResponse(Call<NewsList> call, Response<NewsList> response) {
                NewsList newsList = response.body();
                System.out.print(newsList);
                List<Article> articlesArrayList = newsList.getArticles();


               for(int i=0;i<articlesArrayList.size();i++){
                 Article articles = articlesArrayList.get(i);
                 String string = articles.getUrlToImage();
                 String description = articles.getContent();
                 ChildModel itemModel = new ChildModel(string,description);

                 Source source = articles.getSource();
                 String name = source.getName();


                   if(!map.containsKey(name)){

                       childModels = new ArrayList<>();
                       childModels.add(itemModel);

                       ParentModel parentModel = new ParentModel(name,childModels);
                       parentModels.add(parentModel);
                       parentAdapter.notifyDataSetChanged();
                       map.put(name,childModels );
                   }else{
                       childModels = map.get(name);
                       assert childModels != null;
                       childModels.add(itemModel);
                       parentAdapter.notifyDataSetChanged();
                   }
               }

            }

            @Override
            public void onFailure(Call<NewsList> call, Throwable t) {

            }
        });


    }

    @Override
    public void onMoreClicked(ArrayList<ChildModel> childModelArrayList, String title) {
        Intent intent = new Intent(MainActivity.this,ParentFullScreen.class);
        intent.putExtra("list",childModelArrayList);
        intent.putExtra("title",title);
        startActivity(intent);
    }


    @Override
    public void onImageClicked(String image_url, String description) {
        Intent intent = new Intent(MainActivity.this,NewsDescription.class);
        intent.putExtra("image_url",image_url);
        intent.putExtra("description",description);
        startActivity(intent);
    }

    @Override
    public void checkConnection() {
        fetch();
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        ConnectionReceiver.isNetworkConnected(this);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        ConnectionReceiver.isNetworkConnected(this);
//
//    }
//
//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        ConnectionReceiver.isNetworkConnected(this);
//    }

}
