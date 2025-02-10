package com.foodsaver.server.exceptions;

public class EmailNotVerifiedException extends ApiRequestException {
    public EmailNotVerifiedException() {
        super("Email not verified");
    }
}
