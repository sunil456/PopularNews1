package com.sunil.popularnews;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.sunil.popularnews.adapter.Adapter;
import com.sunil.popularnews.api.ApiClient;
import com.sunil.popularnews.api.ApiInterface;
import com.sunil.popularnews.models.Article;
import com.sunil.popularnews.models.News;
import com.sunil.popularnews.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//https://www.youtube.com/watch?v=Sujg1yg4a2E&list=PLT3-dzFEBix16zontJjPJPeYUWtE_HREq&index=3
public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    public static final String API_KEY ="a4c2a3e374e54d8fa7bf5d51469d4303";
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        loadJson("");
    }


    public void loadJson(final String keyword)
    {
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String country = Utils.getCountry();
        String language = "en";
        Call<News> call;

        if (keyword.length() > 0)
        {
            call = apiInterface.getNewSearch(keyword , language , "publishedAt" , API_KEY);
        }
        else
        {
            call = apiInterface.getNews(country , API_KEY);
        }

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if (response.isSuccessful() && response.body().getArticle() != null)
                {
                    if (!articles.isEmpty())
                    {
                        articles.clear();
                    }
                    articles = response.body().getArticle();
                    adapter = new Adapter(articles , MainActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    initListener();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "No Result", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

            }
        });
    }

    private void initListener()
    {
        adapter.setOnItemClickListener(new Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent intent = new Intent(MainActivity.this , NewsDetailActivity.class);

                Article article = articles.get(position);

                intent.putExtra("url" , article.getUrl());
                intent.putExtra("title" , article.getTitle());
                intent.putExtra("img" , article.getUrlToImage());
                intent.putExtra("date" , article.getPublishedAt());
                intent.putExtra("source" , article.getSource().getName());
                intent.putExtra("author" , article.getAuthor());

                startActivity(intent);



                Toast.makeText(MainActivity.this, "Clicked On : " + article.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main , menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                if (query.length() > 2)
                {
                    loadJson(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                loadJson(newText);
                return false;
            }
        });

        searchMenuItem.getIcon().setVisible(false , false);
        return true;
    }
}
