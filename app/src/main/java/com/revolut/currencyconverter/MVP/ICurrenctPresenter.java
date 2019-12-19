package com.revolut.currencyconverter.MVP;

import com.google.gson.JsonObject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public interface ICurrenctPresenter {

    public void fetchCurrencyList(String mainRate,boolean justUpdate);
    public void listScrollUp(int pos);
    public  void updateListdata(double value);


    public void apiRepeatFn();
    public  void updateRepeateMode(String mainRate);

    public DisposableSingleObserver<JsonObject> getCurrencyObserver(String mainRate,boolean isRepeat,boolean justUpdate);
    public  void attachDisposable(Disposable disposable);
    public CompositeDisposable disposeDisposable();

}
