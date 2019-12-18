package com.revolut.currencyconverter.view;

import com.revolut.currencyconverter.model.CurrencyRates;
import com.revolut.currencyconverter.model.ListItems;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public interface MainActivityView {

    public void fetchList(ArrayList<ListItems> listItems);

    public void listScrollUp(int pos);

    public void updateList(double value);
    public void repeateListUpdate(ArrayList<ListItems> listItems,double multiplier);
    public void connectionTimeoutAlert();
    public  void errorAlert(String msg);

    public void closeAllAlert();



}
