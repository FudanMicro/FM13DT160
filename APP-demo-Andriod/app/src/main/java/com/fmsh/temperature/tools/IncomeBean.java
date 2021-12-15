package com.fmsh.temperature.tools;


import android.os.Parcel;
import android.os.Parcelable;

public class IncomeBean implements Parcelable {
    private long time ;
    private double value;
    private double filed;

    public long getTradeDate() {
        return time;
    }

    public void setTradeDate(long tradeDate) {
        this.time = tradeDate;
    }

    public double getFiled() {
        return filed;
    }

    public void setFiled(double filed) {
        this.filed = filed;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public IncomeBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.time);
        dest.writeDouble(this.value);
        dest.writeDouble(this.filed);
    }

    protected IncomeBean(Parcel in) {
        this.time = in.readLong();
        this.value = in.readDouble();
        this.filed = in.readDouble();
    }

    public static final Parcelable.Creator<IncomeBean> CREATOR = new Parcelable.Creator<IncomeBean>() {
        @Override
        public IncomeBean createFromParcel(Parcel source) {
            return new IncomeBean(source);
        }

        @Override
        public IncomeBean[] newArray(int size) {
            return new IncomeBean[size];
        }
    };
}
