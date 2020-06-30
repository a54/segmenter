package ru.sberbank.sbernet.segmenter.amazonroutines.adapters;

public class CephException extends RuntimeException {
    public CephException() {
    }

    public CephException(String message) {
        super(message);
    }
}
