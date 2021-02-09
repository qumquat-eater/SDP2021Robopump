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

    //Methods to validate input in correct format(not currently used and not tested)

    // Validate length of name
    boolean validateNameFormat(String userName) {
        return userName.length() != 0 && userName.length() < 40;
    }

    // Validate length of name
    boolean validateEmailFormat(String email) {
        return email.length() != 0 && email.length() < 40; //maybe add more complex regex to check email format
    }

    // Validate length of postcode
    boolean validatePostcodeFormat(String postcode){
        return postcode.length() != 0 && postcode.length() < 40;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public CardInformation getDetails() {
        return details;
    }

    public void setDetails(CardInformation details) {
        this.details = details;
    }
}
