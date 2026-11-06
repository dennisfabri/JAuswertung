package de.df.jauswertung.gui.plugins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jutils.gui.JIntegerField;
import de.df.jutils.plugin.IPlugin;
import de.df.jutils.plugin.IPluginManager;

class TimeInputListenerTest {

    private TimeInputAdapter consumer;
    private JIntegerField inputField;
    private TimeInputListener listener;

    @BeforeEach
    void setUp() {
        consumer = mock(TimeInputAdapter.class);
        inputField = mock(JIntegerField.class);
        when(consumer.getSchwimmer()).thenReturn(mock(ASchwimmer.class));
        when(consumer.getInputField()).thenReturn(inputField);
        when(consumer.isByTimes()).thenReturn(true);
        listener = new TimeInputListener(consumer, mock(IPlugin.class), mock(IPluginManager.class));
    }

    private String type(String oldValue, String newValue) {
        when(inputField.getText()).thenReturn(newValue);
        return listener.apply(oldValue, new String[] { newValue });
    }

    @Test
    void digitsFormingInvalidIntermediateTimeStayInField() {
        assertEquals("17064", type("1706", "17064"));
        verify(consumer, never()).updateTime();
    }

    @Test
    void validTimeUpdatesModel() {
        assertEquals("170648", type("17064", "170648"));
        verify(consumer).updateTime();
    }
}
