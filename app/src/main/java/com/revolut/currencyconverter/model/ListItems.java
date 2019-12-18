package com.revolut.currencyconverter.model;

public class ListItems  {


    String rate_name,rate;
    Double multiple;
    int pos;


    public ListItems(String rate_name,String rate,int pos,Double multiple)
    {

        this.multiple=multiple;
        this.pos=pos;
        this.rate_name=rate_name;
        this.rate=rate;
    }

    public Double getMultiple() {
        return multiple;
    }

    public void setMultiple(Double multiple) {
        this.multiple = multiple;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getRate_name() {
        return rate_name;
    }

    public void setRate_name(String rate_name) {
        this.rate_name = rate_name;
    }


    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

}
