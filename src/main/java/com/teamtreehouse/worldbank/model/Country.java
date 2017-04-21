package com.teamtreehouse.worldbank.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Country{

    @Id
    private String code;

    @Column
    private String name;

    @Column
    private Double internetUsers;

    @Column
    private Double adultLiteracyRate;

    public Country(){}

    public Country(CountryBuilder builder){
        this.code = builder.code;
        this.name = builder.name;
        this.internetUsers = builder.internet;
        this.adultLiteracyRate = builder.literacy;
    }

    @Override
    public String toString() {
        if((adultLiteracyRate==null)&&(internetUsers==null)) {
            {
                return String.format("%-12s\t%-32s\t%15s\t%22s\t",
                        code, name, "--", "--");
            }
        }
        if (adultLiteracyRate == null) {
            return  String.format("%-12s\t%-32s\t%15.2f\t%22s",
                    code, name, internetUsers.doubleValue(), "--");
        }
        if (internetUsers == null) {
            return  String.format("%-12s\t%-32s\t%15s\t%22.2f\t",
                    code, name,"--", adultLiteracyRate.doubleValue());
        }

        return  String.format("%-12s\t%-32s\t%15.2f\t\t%18.2f",
                code, name, internetUsers.doubleValue(),adultLiteracyRate.doubleValue());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getInternetUsers() {
        return internetUsers;
    }

    public void setInternetUsers(Double internetUsers) {
        this.internetUsers = internetUsers;
    }

    public Double getAdultLiteracyRate() {
        return adultLiteracyRate;
    }

    public void setAdultLiteracyRate(Double adultLiteracyRate) {
        this.adultLiteracyRate = adultLiteracyRate;
    }


    public static class CountryBuilder {
        private String code;
        private String name;
        private Double internet;
        private Double literacy;

        public CountryBuilder() {}

        public CountryBuilder withCode(String code){
            this.code = code;
            return this;
        }

        public CountryBuilder withName(String name){
            this.name = name;
            return this;
        }

        public CountryBuilder withInternet(Double internet){
            this.internet = internet;
            return this;
        }

        public CountryBuilder withLiteracy(Double literacy){
            this.literacy = literacy;
            return this;
        }

        public Country build() { return new Country(this);}
    }
}
