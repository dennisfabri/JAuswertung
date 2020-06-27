package de.df.jauswertung.gui.plugins.upload;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;

import javax.swing.JMenuItem;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
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
                    Competition competition = new Competition();
                    competition.setName("upload");
                    competition.setId(13);
                    competition.setDate(LocalDate.now());
                    competition.setProtocol(new byte[] { (byte) 1, (byte) 2, (byte) 3 });

                    ObjectMapper mapper = new ObjectMapper();
                    mapper.findAndRegisterModules();

                    ClientConfig config = new ClientConfig();
                    config.register(mapper);

                    Client client = ClientBuilder.newClient(config);
                    client.register(mapper);
                    WebTarget webTarget = client.target("http://localhost:8080/");
                    WebTarget employeeWebTarget = webTarget.path("competition/" + competition.getId());
                    Invocation.Builder invocationBuilder = employeeWebTarget.request(MediaType.APPLICATION_JSON);
                    Response response = invocationBuilder.put(Entity.entity(competition, MediaType.APPLICATION_JSON));
                    System.out.println("Upload: " + response.getStatus() + " " + response.getStatusInfo());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        upload.setToolTipText(I18n.getToolTip("Upload"));

        menues = new MenuInfo[1];
        menues[0] = new MenuInfo(I18n.get("Edit"), 500, upload, 976);
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