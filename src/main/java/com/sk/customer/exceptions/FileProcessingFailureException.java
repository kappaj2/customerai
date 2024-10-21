package com.sk.customer.exceptions;

public class FileProcessingFailureException extends RuntimeException {
     public FileProcessingFailureException(Exception e) {
          super(e);
     }
}
