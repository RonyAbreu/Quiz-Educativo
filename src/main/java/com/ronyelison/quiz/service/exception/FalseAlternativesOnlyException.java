package com.ronyelison.quiz.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FalseAlternativesOnlyException extends Exception{
    public FalseAlternativesOnlyException(String message) {
        super(message);
    }
}
