package com.myapp.booking.exceptions;

public class TokenRefreshException extends RuntimeException{
    public TokenRefreshException(String message){
        super(message);
    }
}
