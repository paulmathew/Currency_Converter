package com.revolut.currencyconverter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class CurrencyPreference {
    private static CurrencyPreference yourPreference;
    private SharedPreferences sharedPreferences;

    public static CurrencyPreference getInstance(Context context) {
        if (yourPreference == null) {
            yourPreference = new CurrencyPreference(context);
        }
        return yourPreference;
    }

    private CurrencyPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("CurrencyRatePreference",Context.MODE_PRIVATE);
    }

    public void saveData(String key,String value) {
     //   Log.e("Key save",""+key);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putString(key, value);
        prefsEditor.commit();           
    }

    public String getData(String key) {
      //  Log.e("Key ",""+key);
        if (sharedPreferences!= null) {
           return sharedPreferences.getString(key, "1");
        }
        return "";         
    }


    public  void saveBoolean(String key,boolean value)
    {
      //  Log.e("Is bool val",""+value);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putBoolean(key, value);
        prefsEditor.commit();
    }
    public  boolean getBoolean(String key)
    {
        if (sharedPreferences!= null) {

            return sharedPreferences.getBoolean(key, false);
        }
        return false;
    }
}