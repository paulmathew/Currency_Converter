package com.revolut.currencyconverter.dateBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.revolut.currencyconverter.model.CurrencyRates;

@Database(entities = CurrencyRates.class,exportSchema = false,version = 1)
public abstract class CurrencyDataBase extends RoomDatabase {

    private static final String DB_name="currency_db";
    private static CurrencyDataBase instance;

    public static  synchronized CurrencyDataBase getInstance(Context context)
    {
        if(instance==null)
        {
            instance= Room.databaseBuilder(context.getApplicationContext(),CurrencyDataBase.class,DB_name)
                    .fallbackToDestructiveMigration()
                    .build();

        }
        return instance;
    }
    public  abstract CurrencyDao currencyDao();
}
