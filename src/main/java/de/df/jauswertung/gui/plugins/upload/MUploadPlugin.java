package de.df.jauswertung.gui.plugins.upload;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.swing.JMenuItem;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.poi.ss.formula.functions.T;
import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.io.ExportManager;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.MenuInfo;
import de.df.jutils.plugin.UpdateEvent;

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

                    String jsonAttachmentId = "eb3dcb8a-4c72-4799-8d5b-75a076b25d64";
                    String pdfAttachmentId = "f9e2cf27-9c84-437e-a222-5117545b2965";

                    uploadPdf(pdfAttachmentId, wk);
                    // uploadJson(jsonAttachmentId, wk);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        upload.setToolTipText(I18n.getToolTip("Upload"));

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Edit"), 500, upload, 976);
    }

    private void uploadJson(String attachmentId, AWettkampf<?> wk) {
        try {
            UploadAttachmentDto pdf = new UploadAttachmentDto(createPdf(wk));

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
            Response response = invocationBuilder.put(Entity.entity(pdf, MediaType.APPLICATION_JSON));
            System.out.println("Upload: " + response.getStatus() + " " + response.getStatusInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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