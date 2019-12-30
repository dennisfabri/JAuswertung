/*
 * Created on 20.01.2006
 */
package de.df.jauswertung.gui.util;

import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.table.TableModel;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.util.format.StartnumberFormatManager;
import de.df.jutils.gui.jtable.BasicTableModel;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.util.AReturnRunner;

public final class TableRegistrationUtils {

	private TableRegistrationUtils() {
		// Hide
	}

	public static <T extends ASchwimmer> TableModel getMeldungenTable(final LinkedList<T> teilies) {
		return EDTUtils.executeOnEDTwithReturn(() -> getMeldungenTableI(teilies));
	}

	private static <T extends ASchwimmer> TableModel getMeldungenTableI(final LinkedList<T> teilies) {
		TableModel model = null;

		if (teilies == null) {
			return null;
		}

		ListIterator<T> li = teilies.listIterator();

		Object[][] o = null;
		o = new Object[teilies.size()][6];

		for (int y = 0; y < teilies.size(); y++) {
			for (int x = 0; x < o[y].length; x++) {
				o[y][x] = "";
			}

			if (li.hasNext()) {
				T t = li.next();

				Object[] row = o[y];

				row[0] = StartnumberFormatManager.format(t);
				if (t.isAusserKonkurrenz()) {
					row[1] = t.getName() + " (" + I18n.get("AusserKonkurrenzShort") + ")";
				} else {
					row[1] = t.getName();
				}
				row[2] = t.getGliederung();
				row[3] = t.getAK().toString();
				row[4] = I18n.geschlechtToString(t);
				row[5] = t.getBemerkung();
			}
		}

		Object[] titel = null;
		titel = new Object[6];

		titel[0] = I18n.get("StartnumberShort");
		titel[1] = I18n.get("Name");
		titel[2] = I18n.get("Organisation");
		titel[3] = I18n.get("AgeGroup");
		titel[4] = I18n.get("Sex");
		titel[5] = I18n.get("Comment");

		@SuppressWarnings("rawtypes")
		Class[] types = new Class[] { String.class, String.class, String.class, String.class, String.class,
				String.class };

		model = new BasicTableModel(titel, o, types);

		return model;
	}
}