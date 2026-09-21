package br.com.sistemabancario.api.dto;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

public class ErrorResponseDTO {
  


    private final OffsetDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldErrorDTO> fieldErrors;
 
    private ErrorResponseDTO(int status, String error, String message, String path,
                              List<FieldErrorDTO> fieldErrors) {
        this.timestamp = OffsetDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }
 
    public static ErrorResponseDTO of(int status, String error, String message, String path) {
        return new ErrorResponseDTO(status, error, message, path, Collections.emptyList());
    }
 
    public static ErrorResponseDTO withFieldErrors(int status, String error, String message, String path,
                                                     List<FieldErrorDTO> fieldErrors) {
        return new ErrorResponseDTO(status, error, message, path, fieldErrors);
    }
 
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }
 
    public int getStatus() {
        return status;
    }
 
    public String getError() {
        return error;
    }
 
    public String getMessage() {
        return message;
    }
 
    public String getPath() {
        return path;
    }
 
    public List<FieldErrorDTO> getFieldErrors() {
        return fieldErrors;
    }
}
