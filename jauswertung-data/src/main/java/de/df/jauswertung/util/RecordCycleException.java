package de.df.jauswertung.util;

public class RecordCycleException extends RuntimeException {

    public RecordCycleException(Class<?> recordClass) {
        super("Cannot copy a cycle that runs through record " + recordClass.getName());
    }
}
