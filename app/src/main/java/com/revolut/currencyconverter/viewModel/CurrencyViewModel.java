package com.revolut.currencyconverter.viewModel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;
import com.revolut.currencyconverter.ApplicationClass;
import com.revolut.currencyconverter.dateBase.CurrencyDataBase;
import com.revolut.currencyconverter.httpClient.APIClient;
import com.revolut.currencyconverter.model.CurrencyRates;
import com.revolut.currencyconverter.model.ListItems;
import com.revolut.currencyconverter.utils.Constants;
import com.revolut.currencyconverter.utils.CurrencyPreference;

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

public class CurrencyViewModel extends ViewModel  implements CurrencyViewModelInterface{

    private APIClient.ApiInterface apiInterface;
    private CurrencyPreference currencyPreference;

    private Double currentMultiplier=1.0;
    CompositeDisposable  compositeDisposable;


    private final MutableLiveData<ArrayList<ListItems>> currencyListItem=new MutableLiveData<>();
    private  final MutableLiveData<Boolean> isError=new MutableLiveData<>();
    private final MutableLiveData<String>errorMsg=new MutableLiveData<>();

    public CurrencyViewModel()
    {


        apiInterface=APIClient.getInstance().getApiInterface();

        currencyPreference= ApplicationClass.currencyPreference;
        currencyPreference.saveData(Constants.CURRENT_MULTIPLIER,"1.0");
        compositeDisposable = new CompositeDisposable();

        String rate=(currencyPreference.getData(Constants.CURRENT_RATE).equals("1"))? "EUR":currencyPreference.getData(Constants.CURRENT_RATE);
        fetchCurrencyList(rate,false);
        //apiRepeatFn();
        currentMultiplier=Double.parseDouble((currencyPreference.
                getData(Constants.CURRENT_MULTIPLIER).equals("1"))? "1.0":currencyPreference.getData(Constants.CURRENT_MULTIPLIER));
    }
    @Override
    public LiveData<ArrayList<ListItems>> getCurrencyList()
    {

        return currencyListItem;
    }
    public  LiveData<Boolean> getErrors()
    {
        return isError;
    }
    public  LiveData<String>getErrorMsg(){return  errorMsg;}


    @Override
    public void fetchCurrencyList(String mainRate,boolean justUpdate) {

        Log.e("Main Rate ",""+mainRate);

        attachDisposable( apiInterface.getResponse(mainRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCurrencyObserver( mainRate,false,justUpdate)));
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
                    }

                    @Override
                    public void onNext(Long value) {

                        String rate = (currencyPreference.getData(Constants.CURRENT_RATE).equals("1")) ? "EUR" : currencyPreference.getData(Constants.CURRENT_RATE);
                        updateRepeateMode(rate);

                        //isError.setValue(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                        Log.e("Is This "," 1");

//                        isError.setValue(true);
//                        errorMsg.setValue("errorAlert"+e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                        Log.e("On", " onComplete");
                    }
                });

    }

    @Override
    public void updateRepeateMode(String mainRate) {

        attachDisposable(  apiInterface.getResponse(mainRate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getCurrencyObserver(mainRate,true,false)));

    }


    @Override
    public DisposableSingleObserver<JsonObject> getCurrencyObserver(String mainRate, boolean isRepeat, boolean justUpdate) {


        return new DisposableSingleObserver<JsonObject>() {
            @Override
            public void onSuccess(JsonObject jsonObject) {
                JsonObject jsonObject1=new JsonObject();
                jsonObject1=jsonObject.getAsJsonObject("rates");
                Set<?> s =  jsonObject1.keySet();
                Iterator<?> i = s.iterator();
                ArrayList<ListItems> items=new ArrayList<>();
                DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");
                List<CurrencyRates> listCurrencyRates=new ArrayList<>();
                currentMultiplier=Double.parseDouble((currencyPreference.
                        getData(Constants.CURRENT_MULTIPLIER).equals("1"))? "1.0":currencyPreference.getData(Constants.CURRENT_MULTIPLIER));
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
                isError.setValue(false);
                if(!justUpdate) {

                    currencyListItem.setValue(items);
                    if (!isRepeat) {

                        currencyPreference.saveBoolean(Constants.IS_REPEATING,true);
                        apiRepeatFn();

                    } else {


                    }
                }
                else {
                }

            }

            @Override
            public void onError(Throwable e) {
                System.out.println(e.getLocalizedMessage());
                isError.setValue(true);
                if(e instanceof TimeoutException || e instanceof SocketTimeoutException || e instanceof UnknownHostException)
                {
                    // view.connectionTimeoutAlert();

                    errorMsg.setValue("connectionTimeOut");
                }
                else
                {
                    // view.errorAlert(e.getLocalizedMessage());

                    errorMsg.setValue("errorAlert"+e.getMessage());
                }


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
