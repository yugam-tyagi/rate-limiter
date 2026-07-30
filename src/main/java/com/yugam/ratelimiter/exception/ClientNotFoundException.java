package com.yugam.ratelimiter.exception;

public class ClientNotFoundException extends RuntimeException{
    public ClientNotFoundException(String clientId){
        super("Client not found with id: " + clientId);
    }
}
