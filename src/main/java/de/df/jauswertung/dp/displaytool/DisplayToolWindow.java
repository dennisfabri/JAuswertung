package de.df.jauswertung.dp.displaytool;

import java.awt.event.*;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

public abstract class DisplayToolWindow {

    private JFrame frmDisplayTool;
    private JTable table;
    private JTextField tfName;
    private JButton btnDisplay;
    private JScrollPane scrollPane;
    private JTextField tfRows;

    /**
     * Create the application.
     */
    public DisplayToolWindow() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmDisplayTool = new JFrame();
        frmDisplayTool.setTitle("Display Tool");
        frmDisplayTool.setBounds(100, 100, 450, 300);
        frmDisplayTool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmDisplayTool.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                RowSpec.decode("fill:default"),
                FormSpecs.RELATED_GAP_ROWSPEC,
                RowSpec.decode("fill:default"),
                FormSpecs.RELATED_GAP_ROWSPEC,
                RowSpec.decode("fill:default:grow"),
                FormSpecs.RELATED_GAP_ROWSPEC,
                RowSpec.decode("fill:default"),
                FormSpecs.RELATED_GAP_ROWSPEC,}));
        
        JLabel lblWettkampf = new JLabel("Wettkampf");
        frmDisplayTool.getContentPane().add(lblWettkampf, "2, 2, left, default");
        
        tfName = new JTextField();
        tfName.setEditable(false);
        frmDisplayTool.getContentPane().add(tfName, "4, 2, 3, 1, fill, default");
        tfName.setColumns(10);
        
        JLabel lblZeilen = new JLabel("Zeilen");
        frmDisplayTool.getContentPane().add(lblZeilen, "2, 4, left, default");
        
        tfRows = new JTextField();
        tfRows.setText("10");
        tfRows.setColumns(2);
        frmDisplayTool.getContentPane().add(tfRows, "4, 4, 3, 1, fill, default");
        
        scrollPane = new JScrollPane();
        frmDisplayTool.getContentPane().add(scrollPane, "2, 6, 5, 1, fill, fill");
        
        table = new JTable();
        scrollPane.setViewportView(table);
        
        btnDisplay = new JButton("Anzeigen");
        frmDisplayTool.getContentPane().add(btnDisplay, "6, 8");
        
        JMenuBar menuBar = new JMenuBar();
        frmDisplayTool.setJMenuBar(menuBar);
        
        JMenu mnDatei = new JMenu("Datei");
        menuBar.add(mnDatei);
        
        JMenuItem miOpen = new JMenuItem("\u00D6ffnen");
        miOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doOpen();
            }
        });
        mnDatei.add(miOpen);
        
        JMenuItem miUpdate = new JMenuItem("Aktualisieren");
        miUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doUpdate();
            }
        });
        mnDatei.add(miUpdate);
        
        JSeparator separator = new JSeparator();
        mnDatei.add(separator);
        
        JMenuItem miExit = new JMenuItem("Beenden");
        miExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        mnDatei.add(miExit);
    }

    protected JTextField getCompetitionName() {
        return tfName;
    }
    protected JTable getTable() {
        return table;
    }
    protected JButton getDisplay() {
        return btnDisplay;
    }
    protected JFrame getWindow() {
        return frmDisplayTool;
    }
    
    protected abstract void doOpen();
    protected abstract void doUpdate();
    
    protected JTextField getRows() {
        return tfRows;
    }
}