package com.example.aodyra.madavakasms;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by aodyra on 12/1/16.
 */

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.MyViewHolder> {

    private List<Sms> smsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView phonenumber, message;

        public MyViewHolder(View itemView) {
            super(itemView);
            phonenumber = (TextView) itemView.findViewById(R.id.phonenumber);
            message = (TextView) itemView.findViewById(R.id.message);
        }
    }

    public SmsAdapter(List<Sms> smsList) {
        this.smsList = smsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_list_row,parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Sms sms =  smsList.get(position);
        holder.phonenumber.setText(sms.getPhonenumber());
        String contentMessage = sms.getMessage();
        if(contentMessage.length() > 50) contentMessage = contentMessage.substring(0,70)+"...";
        holder.message.setText(contentMessage);
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }
}
