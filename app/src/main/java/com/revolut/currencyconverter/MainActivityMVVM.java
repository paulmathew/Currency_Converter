package com.revolut.currencyconverter;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.revolut.currencyconverter.adapterClass.CurrencyAdapterVM;
import com.revolut.currencyconverter.dateBase.CurrencyDataBase;
import com.revolut.currencyconverter.model.ListItems;
import com.revolut.currencyconverter.utils.Constants;
import com.revolut.currencyconverter.utils.CurrencyPreference;
import com.revolut.currencyconverter.viewModel.ActivityInterface;
import com.revolut.currencyconverter.viewModel.AdapterCallBack;
import com.revolut.currencyconverter.viewModel.CurrencyViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivityMVVM extends AppCompatActivity implements AdapterCallBack, ActivityInterface {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.swipeToRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    CurrencyViewModel currencyViewModel;
    CurrencyAdapterVM currencyAdapter;
    ArrayList<ListItems> listItems;
    CurrencyDataBase currencyDataBase;
    CurrencyPreference currencyPreference;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    AlertDialog alertConnectionTimeout, alertErrorDialog;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        currencyViewModel= ViewModelProviders.of(this).get(CurrencyViewModel.class);

        listItems=new ArrayList<>();
        currencyDataBase= CurrencyDataBase.getInstance(getApplicationContext());
        currencyPreference= CurrencyPreference.getInstance(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currencyViewModel.fetchCurrencyList(currencyPreference.getData(Constants.CURRENT_RATE),false);
                getFromViewModel();
                getErrorFromVM();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        currencyPreference.saveBoolean(Constants.IS_REPEATING,false);
        getFromViewModel();
        getErrorFromVM();

    }


    @Override
    protected void onResume() {
        super.onResume();
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmerAnimation();
    }

    private void getFromViewModel() {

        currencyViewModel.getCurrencyList().observe(this, new Observer<ArrayList<ListItems>>() {
            @Override
            public void onChanged(ArrayList<ListItems> listitem) {
                getErrorFromVM();
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                if(!currencyPreference.getBoolean(Constants.IS_REPEATING)) {

                    listItems.clear();
                    listItems.addAll(listitem);

                    currencyPreference.saveData(Constants.CURRENT_MULTIPLIER, "1.0");
                    currencyAdapter = new CurrencyAdapterVM(listItems, getApplicationContext(), MainActivityMVVM.this);
                    recyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(null);
                    recyclerView.setAdapter(currencyAdapter);
                    swipeRefreshLayout.setRefreshing(false);

                    currencyPreference.saveBoolean(Constants.IS_REPEATING,true);
                }
                else {


                    DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");
                    for(int i=0;i<listItems.size();i++)
                    {
                        if(i!=0)
                        {
                            double originalItemValue = Double.parseDouble(
                                    currencyPreference.getData(Constants.CURRENT_SINGLERATE + listItems.get(i).getRate_name())
                            );
                            Double multiplier=Double.parseDouble(currencyPreference.getData(Constants.CURRENT_MULTIPLIER));
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
                }


            }
        });
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
    public void listScrollUp(int pos) {
        closeAllAlert();
        ListItems singleItem=listItems.get(pos);
        listItems.remove(pos);
        listItems.get(0).setPos(0);
        singleItem.setPos(1);
        listItems.add(0,singleItem);
        recyclerView.smoothScrollToPosition(0);
        currencyAdapter.notifyItemChanged(0);
        currencyViewModel.fetchCurrencyList(currencyPreference.getData(Constants.CURRENT_RATE),true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currencyViewModel.disposeDisposable().dispose();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);
    }


    @Override
    public void connectionTimeoutAlert() {

        //  currencyViewModel.disposeDisposable().dispose();
        shimmerFrameLayout.stopShimmerAnimation();
        shimmerFrameLayout.setVisibility(View.GONE);
        alertConnectionTimeout = new AlertDialog.Builder(MainActivityMVVM.this)
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
                        //  currencyPresenter.fetchCurrencyList(rate,false);
                        currencyViewModel.getCurrencyList().removeObservers(MainActivityMVVM.this);
                        currencyPreference.saveBoolean(Constants.IS_REPEATING,false);
                        //

                        currencyViewModel.fetchCurrencyList(currencyPreference.getData(Constants.CURRENT_RATE),false);
                        getFromViewModel();
                        getErrorFromVM();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(MainActivityMVVM.this, "Offline Mode", Toast.LENGTH_SHORT).show();
                    }
                }).create();

        alertConnectionTimeout.show();

    }

    @Override
    public void errorAlert(String msg) {
        // currencyPresenter.disposeDisposable().dispose();
        alertErrorDialog =new AlertDialog.Builder(MainActivityMVVM.this)
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
                        //   currencyPresenter=new CurrencyPresenter(MainActivityMVP.this,getApplicationContext());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(MainActivityMVVM.this, "Canceled", Toast.LENGTH_SHORT).show();
                    }
                }).create();

        alertErrorDialog.show();

    }
    @Override
    public void closeAllAlert() {
        // Log.e("Close All","Called");
        if(alertErrorDialog !=null) {
            alertErrorDialog.dismiss();

        }

        if(alertConnectionTimeout !=null) {
            alertConnectionTimeout.dismiss();
        }

    }


    private void getErrorFromVM() {
        Observer errorMsg=new Observer<String>() {
            @Override
            public void onChanged(String s) {
                currencyViewModel.getErrorMsg().removeObservers(MainActivityMVVM.this);
                if(s.contains("connectionTimeOut"))
                {
                    connectionTimeoutAlert();
                }
                else
                {
                    errorAlert(s.substring(11,s.length()));
                }
            }
        };
        Observer isError= new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.e("Error Is ",""+aBoolean);

                if(aBoolean)
                {
                    currencyViewModel.getErrors().removeObservers(MainActivityMVVM.this);
                    currencyViewModel.getErrorMsg().observe(MainActivityMVVM.this, errorMsg);
                }
                else {
                    closeAllAlert();
                }
            }
        };


        currencyViewModel.getErrors().observe(this,isError);




    }




}
