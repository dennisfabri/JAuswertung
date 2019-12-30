package de.df.jauswertung.gui.plugins.elektronischezeit;

import de.df.jauswertung.gui.plugins.elektronischezeit.layer.HeatInfo;
import de.dm.ares.data.Heat;

interface IETStrategy {

    public HeatMatchingMode isDirectMatching();

    public Heat[] generateHeats();

    public String getHeatname(int index);

    public boolean checkTimes(int index, int[] result);

    public void setTimes(int index, int[] result);

    public void setNoPenalty(int heatnr, int row);

    public void setDisqualification(int heatnr, int row);

    public void setNA(int heatnr, int row);

    public int getTime(int heatnr, int index);

    public void setTime(int heatnr, int index, int timevalue);

    public String[] getHeatnames();

    public int getLanecount();

    public HeatInfo getHeat(int index);

}
