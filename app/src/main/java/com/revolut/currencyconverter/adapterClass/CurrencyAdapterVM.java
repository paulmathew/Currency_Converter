package com.revolut.currencyconverter.adapterClass;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.revolut.currencyconverter.R;
import com.revolut.currencyconverter.model.ListItems;
import com.revolut.currencyconverter.utils.Constants;
import com.revolut.currencyconverter.utils.CurrencyPreference;
import com.revolut.currencyconverter.viewModel.AdapterCallBack;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CurrencyAdapterVM extends RecyclerView.Adapter<CurrencyAdapterVM.ViewHolder> {
    ArrayList<ListItems> listItems;

    CurrencyPreference currencyPreference;
    Double multiplier=1.0;
    Context context;
    AdapterCallBack adapterCallBack;


    public CurrencyAdapterVM(ArrayList<ListItems> listItems, Context applicationContext, AdapterCallBack callback)
    {
//        try {
//            adapterCallBack = ((AdapterCallBack) context);
//        } catch (ClassCastException e) {
//            Log.e("Call back ",""+e.getLocalizedMessage());
//
//        }
        this.adapterCallBack=callback;
        this.listItems=listItems;
        this.context=applicationContext;
        currencyPreference= CurrencyPreference.getInstance(applicationContext);



    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType)
        {
            case 1:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.currency_item,parent,false),1);
            case 0:
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.currency_item,parent,false),0);

        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        int viewType=listItems.get(position).getPos();
        if(viewType==1)
        {

            holder.rate_name.setText(listItems.get(position).getRate_name());
            holder.amount.setText(listItems.get(position).getRate());
            holder.amount.setEnabled(true);


            holder.amount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Double inputValue=0.0;
                    currencyPreference.saveBoolean(Constants.IS_BUSY,true);
                    try {
                        inputValue= Double.parseDouble(s.toString());
                        if (inputValue > 0) {
                            currencyPreference.saveData(Constants.CURRENT_MULTIPLIER, s.toString());
                            listItems.get(position).setRate(s.toString());

                        } else {
                            currencyPreference.saveData(Constants.CURRENT_MULTIPLIER, "0.");
                        }
                    }
                    catch (NumberFormatException ne)
                    {
                        inputValue=1.0;
                        currencyPreference.saveData(Constants.CURRENT_MULTIPLIER, "1.");
                        listItems.get(position).setRate("");
                        //   notifyItemChanged(position);
                    }

                    adapterCallBack.updateList(inputValue);

                }
            });
            try {
                holder.amount.setSelection(listItems.get(position).getRate().length());
            }
            catch (IndexOutOfBoundsException ioe)
            {

            }

        }
        else if(viewType==0)
        {
            holder.rate_name.setText(listItems.get(position).getRate_name());
            holder.amount.setText(listItems.get(position).getRate());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    currencyPreference.saveData(Constants.CURRENT_RATE,listItems.get(position).getRate_name());

                    adapterCallBack.listScrollUp(position);
                }
            });
            holder.amount.setEnabled(false);

        }


    }







    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getPos();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.txt_rate)
        TextView rate_name;
        @BindView(R.id.amount)
        EditText amount;
        @BindView(R.id.cardview)
        CardView cardView;



        public ViewHolder(@NonNull View itemView,int viewType ) {
            super(itemView);
            ButterKnife.bind(this,itemView);

        }


    }






}
