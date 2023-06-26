package com.itranswarp.summer.context.exception;

public class BeanNotOfRequiredTypeException extends BeansException {

    public BeanNotOfRequiredTypeException() {
    }

    public BeanNotOfRequiredTypeException(String message) {
        super(message);
    }
}
