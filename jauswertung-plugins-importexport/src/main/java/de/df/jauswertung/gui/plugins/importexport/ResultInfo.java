package de.df.jauswertung.gui.plugins.importexport;

public class ResultInfo {

    public ResultInfo(ResultType result) {
        this(result, null);
    }

    public ResultInfo(ResultType result, Exception ex) {
        this.result = result;
        this.exception = ex;
    }

    public ResultType getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }

    private final ResultType result;
    private final Exception exception;
}
