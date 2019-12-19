package com.revolut.currencyconverter.viewModel;

import androidx.lifecycle.LiveData;

import com.google.gson.JsonObject;
import com.revolut.currencyconverter.model.CurrencyRates;
import com.revolut.currencyconverter.model.ListItems;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;

public interface CurrencyViewModelInterface {
    public void fetchCurrencyList(String mainRate,boolean justUpdate);
    public void apiRepeatFn();
    public  void updateRepeateMode(String mainRate);
    public DisposableSingleObserver<JsonObject> getCurrencyObserver(String mainRate, boolean isRepeat, boolean justUpdate);
    public  void attachDisposable(Disposable disposable);
    public CompositeDisposable disposeDisposable();

    public LiveData<ArrayList<ListItems>> getCurrencyList();
}
