package com.example.tintuc24h.ui.main;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentContainer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.tintuc24h.Common.Common;
import com.example.tintuc24h.Interface.NewsService;
import com.example.tintuc24h.Model.WebSite;
import com.example.tintuc24h.NewsAdapter.ListSourceAdapter;
import com.example.tintuc24h.R;
import com.google.gson.Gson;

import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsFragment extends Fragment {

    RecyclerView listWebsite;
    RecyclerView.LayoutManager layoutManager;
    NewsService mService;
    ListSourceAdapter  adapter;
    AlertDialog dialog;
    SwipeRefreshLayout swipeLayout;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_news, container, false);

        //init Cache
        Paper.init(rootView.getContext());


        //Init view
        mService = Common.getNewsService();
        listWebsite = (RecyclerView) rootView.findViewById(R.id.list_source);
        listWebsite.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(rootView.getContext());
        listWebsite.setLayoutManager(layoutManager);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.Refresh);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadWebsiteSource(true);
            }
        });

        loadWebsiteSource(false);

        return rootView;
    }

    private void loadWebsiteSource(boolean isRefreshed) {
        if(!isRefreshed)
        {

            String cache = Paper.book().read("cache");
            if(cache != null && !cache.isEmpty() && !cache.equals("null")) // If have cache
            {
                WebSite website = new Gson().fromJson(cache,WebSite.class); // Convert cache from Json to Object
                adapter = new ListSourceAdapter(getActivity(),website);
                adapter.notifyDataSetChanged();
                listWebsite.setAdapter(adapter);
            }
            else // If not have cache
            {
               // dialog.show();
                //Fetch new data
                mService.getSources().enqueue(new Callback<WebSite>() {
                    @Override
                    public void onResponse(Call<WebSite> call, Response<WebSite> response) {
                        adapter  = new ListSourceAdapter(getActivity(),response.body());
                        adapter.notifyDataSetChanged();
                        listWebsite.setAdapter(adapter);

                        //Save to cache
                        Paper.book().write("cache",new Gson().toJson(response.body()));
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call<WebSite> call, Throwable t) {

                    }
                });
            }
        }
        else // If from Swipe to Refresh
        {

            swipeLayout.setRefreshing(true);
            //Fetch new data
            mService.getSources().enqueue(new Callback<WebSite>() {
                @Override
                public void onResponse(Call<WebSite> call, Response<WebSite> response) {
                    adapter  = new ListSourceAdapter(getActivity(),response.body());
                    adapter.notifyDataSetChanged();
                    listWebsite.setAdapter(adapter);

                    //Save to cache
                    Paper.book().write("cache",new Gson().toJson(response.body()));

                    //Dismiss refresh progressring
                    swipeLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<WebSite> call, Throwable t) {

                }
            });

        }
    }

}
