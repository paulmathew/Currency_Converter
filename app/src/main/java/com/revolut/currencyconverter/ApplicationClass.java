package com.revolut.currencyconverter;

import android.app.Application;

import com.revolut.currencyconverter.dateBase.CurrencyDataBase;
import com.revolut.currencyconverter.utils.CurrencyPreference;

public class ApplicationClass extends Application {
    public static   CurrencyPreference currencyPreference;
    public static CurrencyDataBase currencyDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        currencyPreference=CurrencyPreference.getInstance(this);
        currencyDataBase=CurrencyDataBase.getInstance(this);
    }
}
