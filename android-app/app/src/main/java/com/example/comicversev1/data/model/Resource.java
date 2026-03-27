package com.example.comicversev1.data.model;

public abstract class Resource<T> {

    public static final class Success<T> extends Resource<T> {
        private final T data;
        public Success(T data) {
            this.data = data;
        }
        public T getData() {
            return data;
        }
    }

    public static final class Error<T> extends Resource<T> {
        private final String message;
        private final Throwable exception;
        private final int errorCode; // 401, 500, etc.

        public Error(String message, Throwable exception, int errorCode) {
            this.message = message;
            this.exception = exception;
            this.errorCode = errorCode;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getException() {
            return exception;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }

    public static final class Loading<T> extends Resource<T> {
        public Loading() {}
    }
}
