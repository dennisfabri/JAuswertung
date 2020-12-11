package de.df.jauswertung.gui.plugins.upload;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JMenuItem;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.daten.ASchwimmer;
import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.daten.EinzelWettkampf;
import de.df.jauswertung.daten.Mannschaft;
import de.df.jauswertung.daten.MannschaftWettkampf;
import de.df.jauswertung.daten.Teilnehmer;
import de.df.jauswertung.daten.laufliste.OWDisziplin;
import de.df.jauswertung.daten.laufliste.OWSelection;
import de.df.jauswertung.daten.regelwerk.Altersklasse;
import de.df.jauswertung.daten.regelwerk.Disziplin;
import de.df.jauswertung.daten.regelwerk.Regelwerk;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.plugins.upload.dto.CompetitionType;
import de.df.jauswertung.gui.plugins.upload.dto.Individual;
import de.df.jauswertung.gui.plugins.upload.dto.Result;
import de.df.jauswertung.gui.plugins.upload.dto.ResultsDto;
import de.df.jauswertung.gui.plugins.upload.dto.SexIndividual;
import de.df.jauswertung.gui.plugins.upload.dto.SexTeam;
import de.df.jauswertung.gui.plugins.upload.dto.Team;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.ExportManager;
import de.df.jauswertung.util.CompetitionUtils;
import de.df.jauswertung.util.SearchUtils;
import de.df.jauswertung.util.Utils;
import de.df.jauswertung.util.ergebnis.FormelManager;
import de.df.jauswertung.util.ergebnis.ResultUtils;
import de.df.jauswertung.util.ergebnis.SchwimmerData;
import de.df.jauswertung.util.ergebnis.SchwimmerResult;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class MUploadPlugin extends ANullPlugin {

    private MenuInfo[] menues;
    private CorePlugin core;

    private JMenuItem upload;

    public MUploadPlugin() {
        upload = new JMenuItem(I18n.get("Upload"));
        upload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                try {
                    AWettkampf<?> wk = core.getFilteredWettkampf();

                    String jsonAttachmentId = "e9c3e05a-d44e-4cc8-b9fe-3ee883c24b86";
                    String pdfAttachmentId = "bd727123-ca9b-4dad-b6ac-911d54b940111d";

                    uploadPdf(pdfAttachmentId, wk);
                    uploadJson(jsonAttachmentId, wk);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        upload.setToolTipText(I18n.getToolTip("Upload"));

        if (Utils.isInDevelopmentMode()) {
            menues = new MenuInfo[1];
            menues[0] = new MenuInfo(I18n.get("Edit"), 500, upload, 976);
        } else {
            menues = new MenuInfo[0];
        }
    }

    private void uploadJson(String attachmentId, AWettkampf<?> wk) {
        try {
            ResultsDto results = createResults(wk);

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            ClientConfig config = new ClientConfig();
            config.register(mapper);

            Client client = ClientBuilder.newClient(config);
            client.register(mapper);
            WebTarget webTarget = client.target("http://localhost:8080/");

            String path = String.join("/", new String[] { "api", "attachments", attachmentId, "json" });
            WebTarget employeeWebTarget = webTarget.path(path);
            Invocation.Builder invocationBuilder = employeeWebTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.put(Entity.entity(results, MediaType.APPLICATION_JSON));
            System.out.println("Upload: " + response.getStatus() + " " + response.getStatusInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FunctionalInterface
    interface CreateCompetitor<T, S> {
        S create(T t, List<Result> results);
    }

    private ResultsDto createResults(AWettkampf<?> wk) throws IOException {
        return new ResultsDto(getCompetitionType(wk), createIndividualResults(wk), createTeamResults(wk));
    }

    private List<Individual> createIndividualResults(AWettkampf<?> wk) {
        if (wk instanceof EinzelWettkampf) {
            EinzelWettkampf ewk = (EinzelWettkampf) wk;
            return createResults(ewk, (s, r) -> createIndividual(s, r));
        }
        return new ArrayList<>();
    }

    private List<Team> createTeamResults(AWettkampf<?> wk) {
        if (wk instanceof MannschaftWettkampf) {
            MannschaftWettkampf mwk = (MannschaftWettkampf) wk;
            return createResults(mwk, (s, r) -> createTeam(s, r));
        }
        return new ArrayList<>();
    }

    private CompetitionType getCompetitionType(AWettkampf<?> wk) {
        return FormelManager.isOpenwater(wk.getRegelwerk().getFormelID()) ? CompetitionType.OPENWATER
                : CompetitionType.POOL;
    }

    private Individual createIndividual(Teilnehmer t, List<Result> results) {
        return new Individual(t.getVorname(), t.getNachname(),
                t.isMaennlich() ? SexIndividual.MALE : SexIndividual.FEMALE, t.getJahrgang(), t.getAK().getName(),
                results);
    }

    private Team createTeam(Mannschaft t, List<Result> results) {
        return new Team(t.getName(), new ArrayList<Individual>(), t.isMaennlich() ? SexTeam.MALE : SexTeam.FEMALE,
                t.getAK().getName(), results);
    }

    private <T extends ASchwimmer, X> List<X> createResults(AWettkampf<T> wk, CreateCompetitor<T, X> creator) {
        List<X> results = new ArrayList<>();

        Regelwerk aks = wk.getRegelwerk();
        for (int x = 0; x < aks.size(); x++) {
            LinkedList<T> teilies = SearchUtils.getSchwimmer(wk, aks.getAk(x));
            if ((teilies != null) && (teilies.size() > 0)) {
                if (aks.getAk(x).hasMehrkampfwertung()) {
                    results.addAll(altersklasse(wk, aks.getAk(x), false, creator));
                    results.addAll(altersklasse(wk, aks.getAk(x), true, creator));
                }

                if (wk.isHeatBased()) {
                    if (wk.isHeatBased()) {
                        for (OWDisziplin<T> y : wk.getLauflisteOW().getDisziplinen()) {
                            Altersklasse ak = wk.getRegelwerk().getAk(y.akNummer);
                            int qualified = 0;
                            int[] runden = ak.getDisziplin(y.disziplin, y.maennlich).getRunden();
                            boolean isFinal = runden.length <= y.round;
                            if (!isFinal) {
                                qualified = runden[y.round];
                            }

                            AWettkampf<T> wkl = CompetitionUtils.createCompetitionFor(wk,
                                    new OWSelection(wk.getRegelwerk().getAk(y.akNummer), y.akNummer, y.maennlich,
                                            y.disziplin, y.round, isFinal));

                            if (wkl.getRegelwerk().getAk(x).hasEinzelwertung()) {
                                AWettkampf<T> ew = CompetitionUtils.generateEinzelwertungswettkampf(wk, x, false);
                                // if (!isFinal) {
                                // ew.getRegelwerk().setFormelID(FormelILS.ID);
                                // }
                                if (ew != null) {
                                    for (int i = 0; i < wkl.getRegelwerk().size(); i++) {
                                        Altersklasse a = wkl.getRegelwerk().getAk(i);
                                        for (Disziplin[] dx : a.getDisziplinen()) {
                                            for (Disziplin d : dx) {
                                                d.setName(d.getName());
                                            }
                                        }
                                        if (a.getDiszAnzahl() == 0) {
                                            break;
                                        }
                                        // isFinal = y.round >= ak.getDisziplin(y.disziplin,
                                        // y.maennlich).getRunden().length;
                                        String runde = " - " + I18n.getRound(y.round, isFinal);
                                        a.setName(aks.getAk(i).getName() + " - " + a.getDisziplin(0, true).getName()
                                                + runde);

                                        results.addAll(altersklasse(wkl, a, false, creator));
                                        results.addAll(altersklasse(wkl, a, true, creator));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (aks.getAk(x).hasEinzelwertung()) {
                        AWettkampf<T> ew = CompetitionUtils.generateEinzelwertungswettkampf(wk, x, false);
                        if (ew != null) {
                            for (int y = 0; y < ew.getRegelwerk().size(); y++) {
                                Altersklasse a = ew.getRegelwerk().getAk(y);
                                a.setName(aks.getAk(x).getName() + " - " + a.getDisziplin(0, true).getName());
                                altersklasse(ew, a, false, creator);
                                altersklasse(ew, a, true, creator);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    private static <TX extends ASchwimmer, X> List<X> altersklasse(AWettkampf<TX> wk, Altersklasse ak,
            boolean maennlich, CreateCompetitor<TX, X> creator) {

        List<X> collected = new ArrayList<X>();

        SchwimmerResult<TX>[] results = ResultUtils.getResults(wk, ak, maennlich, null, false);
        if (results.length == 0) {
            return collected;
        }

        double points = results[0].getPoints();
        for (SchwimmerResult<TX> result1 : results) {
            if (!result1.hasKeineWertung()) {
                List<Result> times = new ArrayList<>();
                for (int x = 0; x < result1.getResults().length; x++) {
                    SchwimmerData<TX> data = result1.getResults()[x];
                    if (data.getTime() > 0) {
                        String discipline = result1.getSchwimmer().getAK()
                                .getDisziplin(x, result1.getSchwimmer().isMaennlich()).getName();
                        times.add(new Result(discipline, data.getTime() * 10, I18n.getPenaltyShort(data.getStrafe()), 0,
                                0, 0, 0));
                    }
                }
                collected.add(creator.create(result1.getSchwimmer(), times));
            }
        }

        return collected;
    }

    private void uploadPdf(String attachmentId, AWettkampf<?> wk) {
        try {
            UploadAttachmentDto pdf = new UploadAttachmentDto(createPdf(wk));

            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules();

            ClientConfig config = new ClientConfig();
            config.register(mapper);

            Client client = ClientBuilder.newClient(config);
            client.register(mapper);
            WebTarget webTarget = client.target("http://localhost:8080/");

            String path = String.join("/", new String[] { "api", "attachments", attachmentId, "pdf" });
            WebTarget employeeWebTarget = webTarget.path(path);
            Invocation.Builder invocationBuilder = employeeWebTarget.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.put(Entity.entity(pdf, MediaType.APPLICATION_JSON));
            System.out.println("Upload: " + response.getStatus() + " " + response.getStatusInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private byte[] createPdf(AWettkampf<?> wk) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean result = ExportManager.export("PDF", out, ExportManager.PROTOCOL, wk, null);
            if (result) {
                return out.toByteArray();
            } else {
                return new byte[0];
            }
        }
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public MenuInfo[] getMenues() {
        return menues;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        // TODO Auto-generated method stub
    }
}