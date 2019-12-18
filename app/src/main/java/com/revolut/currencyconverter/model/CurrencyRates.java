package com.revolut.currencyconverter.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "rates",indices = @Index(value = {"name"},unique = true))
public class CurrencyRates {



    @PrimaryKey(autoGenerate =true)
    private  int id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "rate")
    private  double rate;

    public CurrencyRates(String name,Double rate)
    {

        this.name=name;
        this.rate=rate;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
