package com.revolut.currencyconverter.httpClient;


import com.google.gson.JsonObject;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class APIClient {

    private static APIClient instance=null;


    public  String url="https://revolut.duckdns.org/";

    private  ApiInterface apiInterface;


    public static  APIClient getInstance()
    {
        if(instance==null)
        {
            instance=new APIClient();
        }
        return  instance;

    }
    private APIClient()
    {
        getClient(url);
    }

    private void getClient(String url) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(interceptor)
                .build();


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())


                .build();

        apiInterface=retrofit.create(ApiInterface.class);


    }

    public ApiInterface getApiInterface()
    {
        return  apiInterface;
    }
    public interface ApiInterface
    {
        @GET("latest")
        Single<JsonObject> getResponse(@Query("base")String base);



    }
}
