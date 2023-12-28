package de.df.jauswertung.gui.plugins;

import static de.df.jauswertung.gui.UpdateEventConstants.REASON_PENALTY;
import static de.df.jauswertung.gui.UpdateEventConstants.REASON_POINTS_CHANGED;

import java.awt.Toolkit;
import java.util.Collections;
import java.util.function.BiFunction;

import javax.swing.SwingUtilities;

import de.df.jauswertung.daten.regelwerk.Strafe;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;

class TimeInputListener implements BiFunction<String, String[], String> {

    private static final String EVENTTITLE_SET_PENALTY = "SetPenalty";
    public static final int MAX_TIME = 9996000;

    private final IPluginManager controller;
    private final IPlugin callingPlugin;
    private final TimeInputAdapter consumer;

    public TimeInputListener(TimeInputAdapter consumer, IPlugin callingPlugin, IPluginManager controller) {
        if (consumer == null) {
            throw new NullPointerException("Consumer must not be null.");
        }
        this.consumer = consumer;
        this.callingPlugin = callingPlugin;
        this.controller = controller;
    }

    public boolean isValidTime(String value) {
        if (value.length() == 0) {
            return true;
        }
        try {

            int x = Integer.parseInt(value);
            if (x >= MAX_TIME) {
                return false;
            }
            x = (x / 100) % 100;
            return (x < 60) && isValidInt(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public boolean isValidInt(String value) {
        if (value.length() == 0) {
            return true;
        }
        try {

            int x = Integer.parseInt(value);
            return x >= 0;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public String updated(String oldValue, String newValue) {
        if (consumer.getSchwimmer() == null) {
            return "";
        }
        if (isValidInt(newValue) && (!consumer.isByTimes() || isValidTime(newValue))) {
            consumer.updateTime();
            return newValue;
        }
        String zeit = getInputField().getText();
        String charactersWithoutDigits = removeDigits(zeit);
        if (charactersWithoutDigits.length() == 1) {
            switch (charactersWithoutDigits) {
            case "p":
                setPenaltyPoints();
                break;
            case "c":
                setPenaltyCode();
                break;
            case ",", "z":
                showZieleinlauf();
                break;
            case "#":
                setNoPenalty();
                break;
            case "m", "+":
                setMeanTime();
                break;
            case "d":
                setDisqualifikation();
                break;
            case "n":
                if (oldValue.isEmpty()) {
                    setNA();
                }
                break;
            case "w":
                setWithdraw();
                break;
            case "f", "b":
                if (oldValue.isEmpty()) {
                    setDidNotFinish();
                }
                break;
            default:
                beep();
                break;
            }
        } else {
            beep();
        }
        return oldValue;
    }

    private void beep() {
        SwingUtilities.invokeLater(() -> Toolkit.getDefaultToolkit().beep());
    }

    private String removeDigits(String zeit) {
        return zeit.replaceAll("\\d", "");
    }

    private String removeNonDigits(String zeit) {
        return zeit.replaceAll("\\D", "");
    }

    private JIntegerField getInputField() {
        return consumer.getInputField();
    }

    private void setPenaltyPoints() {
        SwingUtilities.invokeLater(() -> {
            if (consumer.checkHighPoints()) {
                consumer.runPenaltyPoints();
            }
        });
    }

    private void setPenaltyCode() {
        SwingUtilities.invokeLater(() -> {
            if (consumer.checkHighPoints()) {
                consumer.runPenaltyCode();
            }
        });
    }

    private void setMeanTime() {
        SwingUtilities.invokeLater(() -> {
            if (consumer.checkHighPoints()) {
                consumer.runMeanTimeEditor();
            }
        });
    }

    private void showZieleinlauf() {
        SwingUtilities.invokeLater(() -> {
            if (consumer.checkHighPoints()) {
                consumer.zeigeZieleinlauf();
            }
        });
    }

    private void setNoPenalty() {
        if (consumer.setStrafen(Collections.emptyList())) {
            SwingUtilities.invokeLater(() -> {
                consumer.updatePenalty();
                controller.sendDataUpdateEvent(EVENTTITLE_SET_PENALTY, REASON_POINTS_CHANGED | REASON_PENALTY,
                        consumer.getSchwimmer(), consumer.getDiscipline(), callingPlugin);
            });
        }
    }

    private void setDisqualifikation() {
        consumer.addStrafe(Strafe.DISQUALIFIKATION);
        SwingUtilities.invokeLater(() -> {
            consumer.updatePenalty();
            controller.sendDataUpdateEvent(EVENTTITLE_SET_PENALTY, REASON_POINTS_CHANGED | REASON_PENALTY,
                    consumer.getSchwimmer(), consumer.getDiscipline(), callingPlugin);
        });
    }

    private void setWithdraw() {
        consumer.addStrafe(consumer.getCompetition().getStrafen().getWithdraw());
        SwingUtilities.invokeLater(() -> {
            consumer.updatePenalty();
            controller.sendDataUpdateEvent(EVENTTITLE_SET_PENALTY, REASON_POINTS_CHANGED | REASON_PENALTY,
                    consumer.getSchwimmer(), consumer.getDiscipline(), callingPlugin);
        });
    }

    private void setDidNotFinish() {
        consumer.addStrafe(consumer.getCompetition().getStrafen().getDidNotFinish());
        SwingUtilities.invokeLater(() -> {
            consumer.updatePenalty();
            controller.sendDataUpdateEvent(EVENTTITLE_SET_PENALTY, REASON_POINTS_CHANGED | REASON_PENALTY,
                    consumer.getSchwimmer(), consumer.getDiscipline(), callingPlugin);
        });
    }

    private void setNA() {
        consumer.addStrafe(consumer.getCompetition().getStrafen().getNichtAngetreten());
        SwingUtilities.invokeLater(() -> {
            consumer.updatePenalty();
            controller.sendDataUpdateEvent(EVENTTITLE_SET_PENALTY, REASON_POINTS_CHANGED | REASON_PENALTY,
                    consumer.getSchwimmer(), consumer.getDiscipline(), callingPlugin);
        });
    }

    private String cleanup(String targetValue) {
        JIntegerField inputField = consumer.getInputField();
        final String newValue = removeNonDigits(targetValue);
        if (!inputField.getText().equals(newValue)) {
            SwingUtilities.invokeLater(() -> inputField.setText(newValue));
        }
        return newValue;
    }

    private String reactToInput(String oldValue, String newValue) {
        return cleanup(updated(oldValue, newValue));
    }

    @Override
    public String apply(String oldValue, String[] changes) {
        return reactToInput(oldValue, changes[changes.length - 1]);
    }
}