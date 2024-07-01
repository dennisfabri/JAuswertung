package org.lisasp.competition.base.api.exception;

public class NotFoundException extends Exception {
    public NotFoundException(String type, String id) {
        super(String.format("%s with id '%s' not found.", type, id));
    }
}
