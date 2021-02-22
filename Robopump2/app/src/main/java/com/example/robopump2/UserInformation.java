package com.example.robopump2;

public class UserInformation {


    private String userName;
    private String email;
    private String postcode;
    private CardInformation details;

    public UserInformation(String userName, String email, String postcode, CardInformation details) {
        this.userName = userName;
        this.email = email;
        this.postcode = postcode;
        this.details = details;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPostcode() {
        return postcode;
    }

    public CardInformation getDetails() {
        return details;
    }
}