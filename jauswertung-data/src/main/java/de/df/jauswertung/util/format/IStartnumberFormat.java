package de.df.jauswertung.util.format;

public interface IStartnumberFormat {

    public String GetId();

    public String Format(int i);

    public int Convert(String sn);
}