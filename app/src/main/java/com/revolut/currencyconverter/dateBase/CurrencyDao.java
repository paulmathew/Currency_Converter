package com.revolut.currencyconverter.dateBase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.revolut.currencyconverter.model.CurrencyRates;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;

@Dao
public interface CurrencyDao {


    @Query("SELECT * FROM rates")
    Maybe<List<CurrencyRates>> getAllCurrencyRates();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRate(List<CurrencyRates> currencyRates);

    @Update
    void updateRate(CurrencyRates currencyRates);

    @Delete
    void deleteRate(CurrencyRates currencyRates);

    @Query("DELETE FROM rates")
    public void clearTable();

    @Query("SELECT * FROM rates WHERE name= :name")
    Maybe<CurrencyRates> getOnRate(String name);
}
