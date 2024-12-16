package com.foodsaver.server.exceptions;

public class AccessDeniedException extends RuntimeException{
    public AccessDeniedException(String message){
        super(message) ;
    }
}
