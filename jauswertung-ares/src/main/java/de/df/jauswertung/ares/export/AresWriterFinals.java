package de.df.jauswertung.ares.export;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWLauf;
import de.df.jauswertung.daten.laufliste.OWLaufliste;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;

final class AresWriterFinals {

    private AresWriterFinals() {
    }

    private static final String CHARSET = "ISO-8859-1";

    public static <T extends ASchwimmer> void writeAres(AWettkampf<T>[] wks, String dir) throws IOException {
        FileOutputStream fos;

        fos = new FileOutputStream(dir + File.separator + "LSTCAT.TXT");
        writeAKs(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTLONG.TXT");
        Hashtable<String, Integer> disziplinen = writeLaengen(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTSTYLE.TXT");
        LengthUtil.writeStyles(disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "Lstconc.TXT");
        writeNames(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTREC.TXT");
        writeRecs(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTRACE.TXT");
        writeRaceList(wks, disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "lstrnum.txt");
        writeNrList(wks, disziplinen, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTROUND.TXT");
        writeRoundList(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "LSTSTART.TXT");
        writeHeatList(wks, fos);
        fos.close();

        fos = new FileOutputStream(dir + File.separator + "lsttitpr.txt");
        writeCompetitionInfo(wks, fos);
        fos.close();
    }

    private static void writeAKs(AWettkampf<?>[] wks, OutputStream os) throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"Kategorie\";\"Abr�vCat\"");
        for (AWettkampf<?> wk : wks) {
            writeAKs(wk.getRegelwerk(), ps);
        }
    }

    private static final Map<String, Integer> akGenderToPos = new HashMap<>();

    private static String akGenderToString(String ak, boolean isMale, Regelwerk aks) {
        return ak + " " + (isMale ? aks.getTranslation("maleShort", "m") : aks.getTranslation("femaleShort", "w"));
    }

    private static int akGenderToPosition(String ak, boolean isMale, Regelwerk aks) {
        return akGenderToPos.computeIfAbsent(akGenderToString(ak, isMale, aks).toLowerCase(), s -> -1);
    }

    private static void addAkGenderPos(String ak, boolean isMale, Regelwerk aks) {
        akGenderToPos.put(akGenderToString(ak, isMale, aks).toLowerCase(), akGenderToPos.size() - 1);

    }

    private static void writeAKs(Regelwerk aks, PrintStream ps) throws UnsupportedEncodingException {
        for (int x = 0; x < aks.size(); x++) {

            String ak = aks.getAk(x).getName().toUpperCase().replace("\"", "");
            for (int y = 0; y < 2; y++) {
                if (akGenderToPosition(ak, y == 1, aks) < 0) {
                    addAkGenderPos(ak, y == 1, aks);
                    ps.println("\"" + akGenderToString(ak, y == 1, aks) + "\";\"" + akGenderToPosition(ak,
                                                                                                       y == 1,
                                                                                                       aks) + "\"");
                }
            }
        }
    }

    private static <T extends ASchwimmer> Hashtable<String, Integer> writeLaengen(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("\"idLength\";\"Longueur\";\"Mlongueur\";\"Relais\"");
        // 0; "25 m" ; 25 ;1
        Hashtable<String, Integer> disziplinen = new Hashtable<>();
        for (AWettkampf<T> wk : wks) {
            writeLaengen(wk.getRegelwerk(), ps, disziplinen);
        }
        return disziplinen;
    }

    private static void writeLaengen(Regelwerk aks, PrintStream ps, Hashtable<String, Integer> disziplinen) throws UnsupportedEncodingException {
        // 0; "25 m" ; 25 ;1
        for (int x = 0; x < aks.size(); x++) {
            Altersklasse ak = aks.getAk(x);
            for (int y = 0; y < ak.getDiszAnzahl(); y++) {
                Disziplin d = ak.getDisziplin(y, false);
                Integer i = disziplinen.get(d.getName());
                if (i == null) {
                    int id = disziplinen.size();
                    Discipline discipline = LengthUtil.guessLength(d.getName());
                    int laenge2 = discipline.length() * discipline.amount();
                    String laenge1 = discipline.getDistance();
                    int anschlaege = 1;
                    ps.println(id + ";\"" + laenge1 + "\";" + laenge2 + ";" + anschlaege);
                    disziplinen.put(d.getName(), id);
                    System.out.println(id + " -> " + d.getName().toUpperCase() + " / " + disziplinen.get(d.getName()));
                }
            }
        }
    }

    private static <T extends ASchwimmer> void writeRaceList(AWettkampf<T>[] wks, Hashtable<String, Integer> disziplinen, OutputStream os) throws UnsupportedEncodingException {
        // event;round;nbHeat;idLen;idStyle;abCat;date;time
        // 1 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 2 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 3 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 4 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 5 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 6 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 7 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 8 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;
        // 9 ;0 ;1 ;0 ;0 ;"0" ;"04/27/09" ;"00:00" ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;nbHeat;idLen;idStyle;abCat;date;time");

        LinkedList<DisciplineInfo<T>> ll = getRounds(wks);
        for (DisciplineInfo<T> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = owd.getLaeufe().size();

            Altersklasse ak = wk.getRegelwerk().getAk(0);
            Disziplin d = ak.getDisziplin(owd.disziplin, owd.maennlich);

            int akmw = akGenderToPosition(ak.getName(), owd.maennlich, wk.getRegelwerk());

            if (akmw < 0) {
                System.out.print("akmw -> " + akmw);
                System.out.println(" " + akGenderToString(ak.getName(), owd.maennlich, wk.getRegelwerk()));
            }

            Integer value = disziplinen.get(d.getName());

            if (value == null) {
                System.out.println("Disziplin \"" + d.getName() + "\" wurde nicht gefunden.");
                value = 0;
            }
            int round = getRound(wk, owd);
            int idLen = value;
            String date = "10/18/09";
            String time = "00:00";
            ps.println(id1 + " ;" + round + " ;" + id2 + " ;" + idLen + " ;" + idLen + " ;\"" + akmw + "\" ;\"" + date + "\" ;\"" + time + "\" ;");

        }
    }

    private static <T extends ASchwimmer> LinkedList<DisciplineInfo<T>> getRounds(AWettkampf<T>[] wks) {
        LinkedList<DisciplineInfo<T>> ll = new LinkedList<>();
        for (AWettkampf<T> wk : wks) {
            OWLaufliste<T> low = wk.getLauflisteOW();
            for (OWDisziplin<T> dis : low.getDisziplinen()) {
                ll.add(new DisciplineInfo<>(wk, dis));
            }
        }

        ll.sort((lx1, lx2) -> {
            AWettkampf<T> wk1 = lx1.getFirst();
            AWettkampf<T> wk2 = lx2.getFirst();
            OWDisziplin<T> l1 = lx1.getSecond();
            OWDisziplin<T> l2 = lx2.getSecond();
            int id1 = wk1.getRegelwerk().getRundenId(l1);
            int id2 = wk2.getRegelwerk().getRundenId(l2);
            return id1 - id2;
        });
        return ll;
    }

    private static <T extends ASchwimmer> LinkedList<HeatInfo<T>> getHeats(AWettkampf<T>[] wks) {
        LinkedList<HeatInfo<T>> ll = new LinkedList<>();
        for (AWettkampf<T> wk : wks) {
            OWLaufliste<T> low = wk.getLauflisteOW();
            for (OWDisziplin<T> dis : low.getDisziplinen()) {
                for (OWLauf<T> l : dis.getLaeufe()) {
                    ll.add(new HeatInfo<>(wk, dis, l));
                }
            }
        }

        ll.sort((lx1, lx2) -> {
            AWettkampf<T> wk1 = lx1.getFirst();
            AWettkampf<T> wk2 = lx2.getFirst();
            OWDisziplin<T> d1 = lx1.getSecond();
            OWDisziplin<T> d2 = lx2.getSecond();
            OWLauf<T> l1 = lx1.getThird();
            OWLauf<T> l2 = lx2.getThird();
            int id1 = wk1.getRegelwerk().getRundenId(d1) * 1000 + getLaufnummer(l1);
            int id2 = wk2.getRegelwerk().getRundenId(d2) * 1000 + getLaufnummer(l2);
            return id1 - id2;
        });
        return ll;
    }

    private static <T extends ASchwimmer> int getLaufnummer(OWLauf<T> l1) {
        return l1.getLaufnummer();
    }

    private static <T extends ASchwimmer> void writeHeatList(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;heat;lane;relais;idBib
        // 4 ;1 ;0 ;1 ;0 ;3 ;
        // 4 ;1 ;0 ;2 ;0 ;106 ;
        // 4 ;1 ;0 ;3 ;0 ;7 ;
        // 4 ;1 ;0 ;4 ;0 ;108 ;
        // 4 ;1 ;0 ;5 ;0 ;109 ;
        // 4 ;1 ;0 ;6 ;0 ;105 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;nbHeat;idLen;idStyle;abCat;date;time");

        LinkedList<HeatInfo<T>> ll = getHeats(wks);

        for (HeatInfo<T> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = lauf.getLaufnummer() - 1;

            for (int x = 0; x < lauf.getBahnen(); x++) {
                T t = lauf.getSchwimmer(x);
                if (t != null) {
                    int relay = 0;

                    int lane = x + 1;
                    String sn = "" + getId(t);
                    String date = "10/18/09";
                    String time = "00:00";
                    int round = getRound(wk, owd);
                    ps.println(id1 + " ;" + round + " ;" + id2 + " ;" + lane + " ;" + relay + " ;" + sn + ";\"" + date + "\" ; \"" + time + "\" ;");
                }
            }
        }
    }

    private static <T extends ASchwimmer> void writeNrList(AWettkampf<T>[] wks, Hashtable<String, Integer> disziplinen, OutputStream os) throws UnsupportedEncodingException {
        // id;event;round ;heat
        // 1 ;1 ;0 ;0 ;
        // 2 ;1 ;0 ;0 ;

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;event;round ;heat");

        LinkedList<HeatInfo<T>> ll = getHeats(wks);

        int x = 1;
        for (HeatInfo<T> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();
            OWLauf<T> lauf = lx.getThird();

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int id2 = lauf.getLaufnummer() - 1;

            int round = getRound(wk, owd);

            ps.println(x + " ;" + id1 + " ;" + round + " ;" + id2 + " ;");
            x++;
        }
    }

    private static <T extends ASchwimmer> int getRound(AWettkampf<T> wk, OWDisziplin<T> owd) {
        int round = 0;
        if (wk.isFinal(owd)) {
            round = 3;
        } else if (owd.round == 0) {
            round = 1;
        } else if (owd.round > 0) {
            round = 2;
        }
        return round;
    }

    private static <T extends ASchwimmer> void writeNames(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // id;bib;lastname;firstname;birthyear;abNat;abCat
        // 1 ;"257" ;"POPOV" ;"Alexander" ;"1971" ;"RUS" ;"M" ;
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("id;bib;lastname;firstname;birthyear;abNat;abCat");
        int offset = 0;
        for (AWettkampf<T> wk : wks) {
            offset = writeNames(wk, ps, offset);
        }
    }

    private static String getKey(ASchwimmer t) {
        return t.getStartnummer() + "+" + t.getName() + "+" + t.getAKNummer() + "+" + (t.isMaennlich() ? 1 : 0);
    }

    private static int getId(ASchwimmer t) {
        String key = getKey(t);
        int sn = 0;
        if (!snMapping.containsKey(key)) {
            sn = nextSN;
            snMapping.put(key, sn);
            nextSN++;
        } else {
            sn = snMapping.get(key);
        }
        return sn;
    }

    private static final Hashtable<String, Integer> snMapping = new Hashtable<>();
    private static int nextSN = 1;

    private static <T extends ASchwimmer> int writeNames(AWettkampf<T> wk, PrintStream ps, int offset) {
        for (T t : wk.getSchwimmer()) {
            int sn = getId(t);
            int akmw = akGenderToPosition(t.getAK().getName(), t.isMaennlich(), wk.getRegelwerk());
            ps.print(sn + ";\"" + sn + "\";");
            if (t instanceof Teilnehmer tn) {
                int jg = tn.getJahrgang();
                String vn = cleanupName(tn.getVorname().replace("\"", ""));
                String nn = cleanupName(tn.getNachname().replace("\"", ""));
                ps.print("\"" + nn + "\";\"" + vn + "\";\"" + (jg > 0 ? "" + jg : "") + "\"");
            } else {
                ps.print("\"" + cleanupName(t.getName()) + "\";\"\";\"\"");
            }
            ps.println(";\"" + cleanupName(t.getGliederung()) + "\";\"" + akmw + "\"");
        }
        return wk.getRegelwerk().size() * 2 + offset;
    }

    private static String cleanupName(String name) {
        if (name.startsWith("WOPR Wroc")) {
            return "Wroclawska Drucyna Ratownicza";
        }
        return name
                .replace("\"", "")
                .replace("DLRG ", "")
                .replace("SLSGB ", "")
                .replace("KNBRD ", "")
                .replace("FINS ", "")
                .replace(" VZW (BRC VZW)", "")
                .replace("BLSF ", "")
                .replace("Bereitsschafts-Polizei", "BePo")
                .trim();
    }

    private static <T extends ASchwimmer> void writeRecs(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idLen;idStyle;idRec;abCat;time;name;date;place");
    }

    private static <T extends ASchwimmer> void writeCompetitionInfo(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // event;round;text
        // 1;6;"Open Nederlandse Kampioenschappen Korte Baan 2007";"";""

        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("event;round;text");
        // Laufliste<T> liste = wk.getLaufliste();

        LinkedList<DisciplineInfo<T>> ll = getRounds(wks);

        for (DisciplineInfo<T> lx : ll) {
            AWettkampf<T> wk = lx.getFirst();
            OWDisziplin<T> owd = lx.getSecond();

            Regelwerk aks = wk.getRegelwerk();
            String disziplin = aks.getAk(owd.akNummer).getDisziplin(owd.disziplin, owd.maennlich).getName();
            String gender =
                    owd.maennlich ? aks.getTranslation("maleShort", "m") : aks.getTranslation("femaleShort", "w");

            String heat = switch (owd.round) {
                case 0 -> "Preheat";
                default -> wk.isFinal(owd) ? "Final" : String.format("Round %d", owd.round + 1);
            };

            int id1 = wk.getRegelwerk().getRundenId(owd);
            int round = getRound(wk, owd);

            String text = disziplin + " " + gender + " - " + heat;

            ps.println(id1 + " ;" + round + " ;\"" + text + "\" ;\"" + text + "\" ;\"\" ;");
        }
    }

    private static final String numbers =
            IntStream.range(1, 100).mapToObj(i -> i + (i < 10 ? " " : "")).collect(Collectors.joining(" "));

    private static <T extends ASchwimmer> void writeRoundList(AWettkampf<T>[] wks, OutputStream os) throws UnsupportedEncodingException {
        // idLen;idStyle;idRec;abCat;time;name;date;place
        PrintStream ps = new PrintStream(os, true, CHARSET);
        ps.println("idRound;TITLE;RoundAbrev;roundtext; sequence");
        ps.println("0; \"Lauf\"; \"L\"; \"Lauf\";  \"" + numbers + "\"");
        ps.println("1; \"Vorlauf\"; \"VL\"; \"Vorlauf\";  \"" + numbers + "\"");
        ps.println("2; \"Zwischenlauf\"; \"ZL\"; \"Zwischenlauf\";  \"" + numbers + "\"");
        ps.println("3; \"Finale\"; \"F\"; \"Finale\";  \"" + numbers + "\"");
    }

    private static class HeatInfo<T extends ASchwimmer> {
        private final AWettkampf<T> wk;
        private final OWDisziplin<T> discipline;

        private final OWLauf<T> heat;

        public HeatInfo(AWettkampf<T> wk, OWDisziplin<T> discipline, OWLauf<T> heat) {
            this.wk = wk;
            this.discipline = discipline;
            this.heat = heat;
        }

        public AWettkampf<T> getFirst() {
            return wk;
        }

        public OWDisziplin<T> getSecond() {
            return discipline;
        }

        public OWLauf<T> getThird() {
            return heat;
        }
    }

    private static class DisciplineInfo<T extends ASchwimmer> {
        private final AWettkampf<T> wk;
        private final OWDisziplin<T> discipline;

        public DisciplineInfo(AWettkampf<T> wk, OWDisziplin<T> discipline) {
            this.wk = wk;
            this.discipline = discipline;
        }

        public AWettkampf<T> getFirst() {
            return wk;
        }

        public OWDisziplin<T> getSecond() {
            return discipline;
        }
    }
}
