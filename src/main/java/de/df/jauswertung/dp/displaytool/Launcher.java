package de.df.jauswertung.dp.displaytool;

import java.io.File;

import com.jgoodies.common.base.Strings;

import de.df.jauswertung.dp.displaytool.data.Competition;
import de.df.jauswertung.dp.displaytool.vm.CompetitionPresenter;
import de.df.jauswertung.io.*;
import de.df.jutils.gui.util.DesignInit;

public class Launcher {

    private CompetitionPresenter presenter = new CompetitionPresenter();

    private String               filename  = null;

    public void start() {
        presenter.setCompetition(new Competition("DP"));
        DataDisplayWindow window = new DataDisplayWindow(this);
        window.getWindow().pack();
        window.getWindow().setVisible(true);
    }

    public CompetitionPresenter getCompetition() {
        return presenter;
    }

    public static void main(String[] args) {
        DesignInit.init();

        new Launcher().start();
    }

    public void update() {
        if (filename != null) {
            try {
                
                Competition c = (Competition) InputManager.ladeObject(filename);
                if (c != null) {
                    presenter.setCompetition(c);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    public void setFilename(String string) {
        if (Strings.isNotEmpty(string)) {
            filename = string;
        }
    }
}