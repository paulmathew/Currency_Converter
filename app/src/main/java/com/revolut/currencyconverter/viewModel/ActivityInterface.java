package com.revolut.currencyconverter.viewModel;

public interface ActivityInterface {

    public void errorAlert(String msg);
    public void connectionTimeoutAlert();
    public void closeAllAlert();
}
