package com.revolut.currencyconverter.presenter;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.revolut.currencyconverter.dateBase.CurrencyDataBase;
import com.revolut.currencyconverter.httpClient.APIClient;
import com.revolut.currencyconverter.model.CurrencyRates;
import com.revolut.currencyconverter.model.ListItems;
import com.revolut.currencyconverter.utils.Constants;
import com.revolut.currencyconverter.utils.CurrencyPreference;
import com.revolut.currencyconverter.view.MainActivityView;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CurrencyPresenter implements  ICurrenctPresenter {

    private APIClient.ApiInterface apiInterface;
    private MainActivityView view;
    private CurrencyDataBase currencyDataBase;
    private Context context;
    private String TAG="Hello";
    private CurrencyPreference currencyPreference;
    private Double currentMultiplier=1.0;
    CompositeDisposable  compositeDisposable;
    Disposable disposable;
    public CurrencyPresenter(MainActivityView view, Context context)
    {
        this.view=view;
        this.context=context;
        apiInterface=APIClient.getInstance().getApiInterface();

        currencyDataBase=CurrencyDataBase.getInstance(context);
        currencyPreference=CurrencyPreference.getInstance(context);
        currencyPreference.saveData(Constants.CURRENT_MULTIPLIER,"1.0");
        compositeDisposable = new CompositeDisposable();

        String rate=(currencyPreference.getData(Constants.CURRENT_RATE).equals("1"))? "EUR":currencyPreference.getData(Constants.CURRENT_RATE);
        fetchCurrencyList(rate,false);
        //apiRepeatFn();
        currentMultiplier=Double.parseDouble((currencyPreference.
                getData(Constants.CURRENT_MULTIPLIER).equals(""))? "1.0":currencyPreference.getData(Constants.CURRENT_MULTIPLIER));
    }

    @Override
    public void fetchCurrencyList(String mainRate,boolean justUpdate) {

        deleteTable();

        Log.e("Main Rate ",""+mainRate);

        attachDisposable( apiInterface.getResponse(mainRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCurrencyObserver( mainRate,false,justUpdate)));
    }

    @Override
    public void listScrollUp(int position) {
        view.listScrollUp(position);
    }

    @Override
    public void updateListdata(double val) {
        view.updateList(val);
    }


    @Override
    public void apiRepeatFn() {

        Observable.interval(0,1, TimeUnit.SECONDS)

                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        //Log.e(TAG, " onSubscribe : " + d.isDisposed());
                        disposable=d;
                    }

                    @Override
                    public void onNext(Long value) {

                        String rate = (currencyPreference.getData(Constants.CURRENT_RATE).equals("")) ? "EUR" : currencyPreference.getData(Constants.CURRENT_RATE);
                        updateRepeateMode(rate);


                    }

                    @Override
                    public void onError(Throwable e) {

                    //    Log.e(TAG, " onError : " + e.getMessage());
                        view.errorAlert(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                       // Log.e(TAG, " onComplete");
                    }
                });

    }

    @Override
    public void updateRepeateMode(String mainRate) {
        // Delete The DB
        deleteTable();

        attachDisposable(  apiInterface.getResponse(mainRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCurrencyObserver(mainRate,true,false)));

    }

    @Override
    public void deleteTable() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                currencyDataBase.currencyDao().clearTable();

            }
        })
                .subscribeOn(Schedulers.io()).subscribe();

    }

    @Override
    public void insertIntoRateTable(List<CurrencyRates> listCurrencyRates) {

        Log.e("total from API",""+listCurrencyRates.size());
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                currencyDataBase.currencyDao().insertRate(listCurrencyRates);

            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        // getRatesFromDB();
                        //apiRepeatFn();

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public DisposableSingleObserver<JsonObject> getCurrencyObserver(String mainRate,boolean isRepeat,boolean justUpdate) {
        view.closeAllAlert();

        return new DisposableSingleObserver<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                JsonObject jsonObject1=new JsonObject();
                jsonObject1=jsonObject.getAsJsonObject("rates");
                Set<?> s =  jsonObject1.keySet();
                Iterator<?> i = s.iterator();
                ArrayList<ListItems>items=new ArrayList<>();
                DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");
                List<CurrencyRates> listCurrencyRates=new ArrayList<>();
                currentMultiplier=Double.parseDouble((currencyPreference.
                        getData(Constants.CURRENT_MULTIPLIER).equals(""))? "1.0":currencyPreference.getData(Constants.CURRENT_MULTIPLIER));
                CurrencyRates firstRate=new CurrencyRates(mainRate,currentMultiplier);


                if(!isRepeat) {
                    items.add(new ListItems(mainRate, decimalFormat.format(1), 1, 0.0));
                    currencyPreference.saveData(Constants.CURRENT_SINGLERATE+mainRate,""+decimalFormat.format(1));
                }
                else
                {
                    items.add(new ListItems(mainRate,""+currentMultiplier,1,0.0));
                    currencyPreference.saveData(Constants.CURRENT_SINGLERATE+mainRate,""+currentMultiplier);

                }

                do{
                    String rate_name = i.next().toString();
                    Double rate=jsonObject1.get(rate_name).getAsDouble();

                    if(!justUpdate)
                        items.add(new ListItems(rate_name, decimalFormat.format(rate), 0, 0.0));


                    currencyPreference.saveData(Constants.CURRENT_SINGLERATE+rate_name,""+jsonObject1.get(rate_name));

                }while(i.hasNext());

                if(!justUpdate) {
                    if (!isRepeat) {
                        view.fetchList(items);
                        apiRepeatFn();

                    } else {
                        view.repeateListUpdate(items,currentMultiplier);

                    }
                }
                else {
                }
            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getLocalizedMessage());
                if(e instanceof TimeoutException || e instanceof SocketTimeoutException || e instanceof UnknownHostException)
                {
                    view.connectionTimeoutAlert();
                    if(isRepeat)
                        disposable.dispose();
                }
                else
                    view.errorAlert(e.getLocalizedMessage());

            }
        };
    }

    @Override
    public void attachDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);

    }

    @Override
    public CompositeDisposable disposeDisposable() {
        return  compositeDisposable;

    }


}
