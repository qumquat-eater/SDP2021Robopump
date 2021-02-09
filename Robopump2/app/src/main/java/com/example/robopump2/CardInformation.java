package com.example.robopump2;

public class CardInformation {

    private String cardNumber;
    private int cvcNumber;
    private String expiryDate;

    public CardInformation(String cardNumber, int cvcNumber, String expiryDate) {
        this.cardNumber = cardNumber;
        this.cvcNumber = cvcNumber;
        this.expiryDate = expiryDate;
    }

    //Methods to validate input in correct format(not currently used and not tested)

    // Validate MM/YY input from
    // https://stackoverflow.com/questions/11528949/validate-the-credit-card-expiry-date-using-java/11531283
    boolean validateCardExpiryDateFormat(String expiryDate) {
        return expiryDate.matches("(?:0[1-9]|1[0-2])/[0-9]{2}");
    }

    // Validates card number is between a certain length and consists only of No's.
    boolean validateCardNumberFormat(String cardNumber) {
        return cardNumber.length() < 20 && cardNumber.length() > 7 //check constraints
                && (cardNumber.matches("[0-9]+"));
    }

    // Validates CVC number is 3 digits long and consists only of No's.
    boolean validateCVCFormat(String cardNumber) {
        //check constraints
        return cardNumber.length() == 3 && (cardNumber.matches("[0-9]+"));
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCvcNumber() {
        return cvcNumber;
    }

    public void setCvcNumber(int cvcNumber) {
        this.cvcNumber = cvcNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
