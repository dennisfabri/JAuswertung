package de.df.jauswertung.gui.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionedDocumentListener implements DocumentListener {
    private static final Logger log = LoggerFactory.getLogger(SessionedDocumentListener.class);

    private volatile boolean isUpdating = false;

    private String oldValue = "";

    private List<String> currentChanges = new ArrayList<>();

    private final BiFunction<String, String[], String> processor;

    public SessionedDocumentListener(BiFunction<String, String[], String> processor) {
        this.processor = processor;
    }

    public void updateWith(String value) {
        oldValue = value;
    }

    private void collectChanges(Document document) {
        try {
            startSession();
            currentChanges.add(document.getText(0, document.getLength()));
        } catch (BadLocationException e1) {
            log.warn("Could not extract String.", e1);
        }
    }

    private void startSession() {
        if (isUpdating) {
            return;
        }
        log.debug("Starting {}", oldValue);
        SwingUtilities.invokeLater(this::finishSession);
    }

    private void finishSession() {
        String[] changes;
        synchronized (this) {
            if (currentChanges.isEmpty() || oldValue.equals(currentChanges.get(currentChanges.size() - 1))) {
                changes = new String[0];
            } else {
                changes = currentChanges.toArray(String[]::new);
            }
            currentChanges.clear();
        }

        if (changes.length > 0) {
            log.info("Accept: {} -> {}", oldValue, changes);
            oldValue = processor.apply(oldValue, changes);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        collectChanges(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        collectChanges(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        collectChanges(e.getDocument());
    }
}
