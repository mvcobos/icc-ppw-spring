package ec.edu.ups.icc.fundamentos01.core.exceptions.base;

import org.springframework.http.HttpStatus;

public class ApplicationException extends RuntimeException{
    
    private final HttpStatus status;

    protected ApplicationException(HttpStatus status, String message){
        super(message);
        this.status = status; // Set the HTTP status code for the exception
    }

    public HttpStatus getStatus() {
        return status;
    }
}
