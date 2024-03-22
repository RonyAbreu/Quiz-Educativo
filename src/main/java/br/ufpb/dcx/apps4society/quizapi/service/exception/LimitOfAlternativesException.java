package br.ufpb.dcx.apps4society.quizapi.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LimitOfAlternativesException extends Exception{

    public LimitOfAlternativesException(String message) {
        super(message);
    }
}
