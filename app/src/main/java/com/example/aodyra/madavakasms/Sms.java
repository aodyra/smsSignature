package com.example.aodyra.madavakasms;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aodyra on 12/1/16.
 */

public class Sms implements Parcelable{
    private String phonenumber, message;

    public Sms(Parcel in){
        phonenumber = in.readString();
        message = in.readString();
    }

    public Sms(String phonenumber, String message){
        this.phonenumber = phonenumber;
        this.message = message;
    }

    public static final Parcelable.Creator<Sms> CREATOR = new Parcelable.Creator<Sms>() {
        @Override
        public Sms createFromParcel(Parcel source) {
            return new Sms(source);
        }

        @Override
        public Sms[] newArray(int size) {
            return new Sms[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phonenumber);
        dest.writeString(message);
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getMessage() {
        return message;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
