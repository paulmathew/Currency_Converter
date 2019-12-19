package com.revolut.currencyconverter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.revolut.currencyconverter.adapterClass.CurrencyAdapter;
import com.revolut.currencyconverter.dateBase.CurrencyDataBase;
import com.revolut.currencyconverter.model.ListItems;
import com.revolut.currencyconverter.presenter.CurrencyPresenter;
import com.revolut.currencyconverter.utils.Constants;
import com.revolut.currencyconverter.utils.CurrencyPreference;
import com.revolut.currencyconverter.view.MainActivityView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    CurrencyPresenter currencyPresenter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeToRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;


    CurrencyAdapter currencyAdapter;
    ArrayList<ListItems> listItems;
    CurrencyDataBase currencyDataBase;
    CurrencyPreference currencyPreference;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    AlertDialog connectionTimeoutBuilder,errorAlertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        currencyPresenter=new CurrencyPresenter(this,getApplicationContext());
        currencyDataBase= CurrencyDataBase.getInstance(getApplicationContext());
        currencyPreference= CurrencyPreference.getInstance(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                swipeRefreshLayout.setRefreshing(false);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                currencyPreference.saveBoolean(Constants.IS_BUSY,true);
            }
        });




    }



    @Override
    public void fetchList(ArrayList<ListItems> listitem) {
        listItems=new ArrayList<>();
        listItems.addAll(listitem);
        currencyPreference.saveData(Constants.CURRENT_MULTIPLIER, "1.0");
        currencyAdapter=new CurrencyAdapter(listItems,currencyPresenter,getApplicationContext());
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(currencyAdapter);
        swipeRefreshLayout.setRefreshing(false);
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);



    }

    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmerAnimation();
    }

    @Override
    public void listScrollUp(int pos) {
        ListItems singleItem=listItems.get(pos);
        listItems.remove(pos);
        listItems.get(0).setPos(0);
        singleItem.setPos(1);
        listItems.add(0,singleItem);
        recyclerView.smoothScrollToPosition(0);
        currencyAdapter.notifyItemChanged(0);
        currencyPresenter.fetchCurrencyList(currencyPreference.getData(Constants.CURRENT_RATE),true);



    }

    @Override
    public void updateList(double value) {
        Log.e("MUL ",""+value);
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");
        for(int i=0;i<listItems.size();i++)
        {

            if(i==0)
            {
                listItems.get(i).setRate(decimalFormat.format(
                        value));
            }
            else
            {


                double originalItemValue = Double.parseDouble(
                        currencyPreference.getData(Constants.CURRENT_SINGLERATE + listItems.get(i).getRate_name())
                );
                listItems.get(i).setRate(decimalFormat.format(
                        originalItemValue * value));


            }
        }
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                for(int i=1;i<listItems.size();i++) {
                    currencyAdapter.notifyItemChanged(i);


                }
            }
        };
        handler.post(r);
        swipeRefreshLayout.setRefreshing(false);



    }

    @Override
    public void repeateListUpdate(ArrayList<ListItems> listItem,double multiplier) {

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");
        for(int i=0;i<listItems.size();i++)
        {
            if(i!=0)
            {

                double originalItemValue = Double.parseDouble(
                        currencyPreference.getData(Constants.CURRENT_SINGLERATE + listItems.get(i).getRate_name())
                );
                listItems.get(i).setRate(decimalFormat.format(
                        originalItemValue * multiplier));


            }
        }
        new Thread((new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(28);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        for(int i=1;i<listItems.size();i++) {
                            currencyAdapter.notifyItemChanged(i);
                        }
                    }
                });
            }
        })).start();

        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void connectionTimeoutAlert() {
       // currencyPresenter.disposeDisposable().dispose();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);
        connectionTimeoutBuilder= new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Network Connection Problem")
                .setMessage("Problem Occurred While Connecting With The Server Please Connect To The Internet")
                .setPositiveButton("Retry", null)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Retry", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        shimmerFrameLayout.startShimmerAnimation();
                        shimmerFrameLayout.setVisibility(View.VISIBLE);
                        String rate=(currencyPreference.getData(Constants.CURRENT_RATE).equals("1"))? "EUR":currencyPreference.getData(Constants.CURRENT_RATE);

                        currencyPresenter.fetchCurrencyList(rate,false);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(MainActivity.this, "You are Offline", Toast.LENGTH_SHORT).show();
                    }
                }).show();

    }

    @Override
    public void errorAlert(String msg) {
        currencyPresenter.disposeDisposable().dispose();
        errorAlertBuilder =new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle("Alert")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        currencyPresenter=new CurrencyPresenter(MainActivity.this,getApplicationContext());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                }).show();

    }

    @Override
    public void closeAllAlert() {
        if(errorAlertBuilder!=null)
            errorAlertBuilder.dismiss();

        if(connectionTimeoutBuilder!=null)
            connectionTimeoutBuilder.dismiss();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currencyPresenter.disposeDisposable().dispose();

    }

}
