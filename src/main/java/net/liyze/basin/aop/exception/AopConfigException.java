package net.liyze.basin.aop.exception;

import net.liyze.basin.context.exception.NestedRuntimeException;

public class AopConfigException extends NestedRuntimeException {

    public AopConfigException() {
        super();
    }

    public AopConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopConfigException(String message) {
        super(message);
    }

    public AopConfigException(Throwable cause) {
        super(cause);
    }
}
