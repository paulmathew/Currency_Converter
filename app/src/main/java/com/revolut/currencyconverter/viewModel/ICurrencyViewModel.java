package com.revolut.currencyconverter.viewModel;

import com.google.gson.JsonObject;
import com.revolut.currencyconverter.model.CurrencyRates;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public interface ICurrencyViewModel {
    public void fetchCurrencyList(String mainRate,boolean justUpdate);
    public void listScrollUp(int pos);
    public  void updateListdata(double value);


    public void apiRepeatFn();
    public  void updateRepeateMode(String mainRate);
    public void deleteTable();
    public  void insertIntoRateTable(List<CurrencyRates> listCurrencyRates);
    public DisposableSingleObserver<JsonObject> getCurrencyObserver(String mainRate, boolean isRepeat, boolean justUpdate);
    public  void attachDisposable(Disposable disposable);
    public CompositeDisposable disposeDisposable();
}
