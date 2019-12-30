package de.df.jauswertung.dp.displaytool;

import java.awt.event.*;

import javax.swing.SwingConstants;
import javax.swing.table.*;

import de.df.jauswertung.gui.util.I18n;
import de.df.jutils.gui.filefilter.SimpleFileFilter;
import de.df.jutils.gui.jtable.JTableUtils;
import de.df.jutils.gui.util.FileChooserUtils;

public class DataDisplayWindow extends DisplayToolWindow {

    private final Launcher controller;

    private TableModel     tablemodel;

    public DataDisplayWindow(Launcher dt) {
        controller = dt;

        update();
        JTableUtils.setAlignmentRenderer(getTable(), new int[] { SwingConstants.RIGHT, SwingConstants.LEFT },
                SwingConstants.RIGHT);
        JTableUtils.setPreferredCellSizes(getTable(), false, false);

        getDisplay().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rows = 10;
                try {
                    rows = Integer.parseInt(getRows().getText());
                } catch (Exception ex) {
                    // Nothing to do
                }
                new JHeatPresentationFrame(null, rows, controller.getCompetition()).setVisible(true);
            }
        });
    }

    public void update() {
        tablemodel = new DefaultTableModel(controller.getCompetition().getTable(),
                new String[] { "Platz", "Name", "Punkte", "Serc", "Summe" }) {
            private static final long serialVersionUID = 5421181568284256122L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        getTable().setModel(tablemodel);

        getCompetitionName().setText(controller.getCompetition().getName());
    }

    @Override
    protected void doOpen() {
        String filename = FileChooserUtils.chooseFile("Öffnen", "Öffnen",
                new SimpleFileFilter[] { new SimpleFileFilter("Competition-Datei", ".competition", ".xml") },
                getWindow());
        if (filename != null) {
            controller.setFilename(filename);
            doUpdate();
        }
    }

    @Override
    protected void doUpdate() {
        controller.update();
        update();
    }
}