package com.example.robopump2;

public class CardInformation {

    private String cardNumber;
    private String cvcNumber;
    private String expiryDate;

    public CardInformation(String cardNumber, String cvcNumber, String expiryDate) {
        this.cardNumber = cardNumber;
        this.cvcNumber = cvcNumber;
        this.expiryDate = expiryDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCvcNumber() { return cvcNumber; }


    public String getExpiryDate() {
        return expiryDate;
    }

}