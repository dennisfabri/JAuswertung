package de.df.jauswertung.gui.plugins.upload;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JToggleButton;

import de.df.jauswertung.gui.plugins.upload.competitiondlrgnet.TimesUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.df.jauswertung.daten.AWettkampf;
import de.df.jauswertung.gui.plugins.CorePlugin;
import de.df.jauswertung.gui.util.I18n;
import de.df.jauswertung.gui.util.IconManager;
import de.df.jauswertung.util.BetaFeature;
import de.df.jauswertung.web.iscupload.ResultUploader;
import de.df.jauswertung.web.uploadstatus.UploadState;
import de.df.jauswertung.web.uploadstatus.UploadStatus;
import de.df.jutils.gui.util.EDTUtils;
import de.df.jutils.plugin.ANullPlugin;
import de.df.jutils.plugin.ButtonInfo;
import de.df.jutils.plugin.IPluginManager;
import de.df.jutils.plugin.UpdateEvent;

@SuppressWarnings("unused")
public class MUploadPlugin extends ANullPlugin {

    private static final Logger log = LoggerFactory.getLogger(MUploadPlugin.class);

    private static final long ONE_MINUTE = 60 * 1000;
    private static final long FIFTEEN_SECOND = 15 * 1000;

    private static final Map<UploadState, String> ICONS = Map.of(UploadState.OK, "upload", UploadState.WARNING,
            "uploadwarning", UploadState.ERROR, "uploaderror");

    private final ButtonInfo[] buttons;

    private CorePlugin core;

    private final JToggleButton uploadButton;

    private final ResultUploader iscResultUploader = new ResultUploader();

    //private final TimesUploader timesUploader = new TimesUploader("https://dev.lisasp.org");
    private final TimesUploader timesUploader = new TimesUploader("https://competition.dlrg.net");
    // private final TimesUploader timesUploader = new TimesUploader("http://localhost:9001");

    public MUploadPlugin() {
        uploadButton = new JToggleButton(IconManager.getSmallIcon("upload"));
        uploadButton.setToolTipText(I18n.getToolTip("ISCUpload"));
        uploadButton.addActionListener(event -> {
            iscResultUploader.setActive(uploadButton.isSelected());
            timesUploader.setActive(uploadButton.isSelected());
            showUploadStatus();
        });

        buttons = new ButtonInfo[1];
        buttons[0] = new ButtonInfo(uploadButton, 1010);

        long period = BetaFeature.FAST_UPLOAD.isEnabled() ? FIFTEEN_SECOND : ONE_MINUTE;

        Timer uploadTimer = new Timer("ISC Upload Timer", true);
        uploadTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    uploadResultsToISC();
                } catch (Exception ex) {
                    log.warn("Could not execute isc-upload", ex);
                }
                try {
                    uploadResultsToCompetitionDlrgNet();
                } catch (Exception ex) {
                    log.warn("Could not execute results-upload", ex);
                }
                showUploadStatus();
            }
        }, period, period);
    }

    private void uploadResultsToISC() {
        AWettkampf<?> wk = core.getWettkampf();
        iscResultUploader.uploadResultsToISC(wk);
    }

    private void uploadResultsToCompetitionDlrgNet() {
        AWettkampf<?> wk = core.getWettkampf();
        timesUploader.uploadJson(wk);
    }

    private void showUploadStatus() {
        UploadStatus isc = iscResultUploader.getUploadStatus();
        UploadStatus competitionDlrgNet = timesUploader.getUploadStatus();
        EDTUtils.executeOnEDTAsync(() -> showUploadStatus(isc, competitionDlrgNet));
    }

    private void showUploadStatus(UploadStatus isc, UploadStatus competitionDlrgNet) {
        UploadState state = isc.state().compareTo(competitionDlrgNet.state()) >= 0 ? isc.state()
                : competitionDlrgNet.state();
        uploadButton.setIcon(IconManager.getSmallIcon(ICONS.get(state)));
        uploadButton.setToolTipText(toToolTip(isc, competitionDlrgNet));
    }

    private static String toToolTip(UploadStatus isc, UploadStatus competitionDlrgNet) {
        String toolTip = I18n.getToolTip("ISCUpload");
        if (isc.state() == UploadState.OK && competitionDlrgNet.state() == UploadState.OK) {
            return toolTip;
        }
        StringBuilder html = new StringBuilder("<html>").append(toolTip);
        appendProblem(html, I18n.get("ISCUpload"), isc);
        appendProblem(html, I18n.get("competition.dlrg.net"), competitionDlrgNet);
        return html.append("</html>").toString();
    }

    private static void appendProblem(StringBuilder html, String system, UploadStatus status) {
        if (status.state() == UploadState.OK) {
            return;
        }
        String key = status.state() == UploadState.ERROR ? "UploadStatus.Error" : "UploadStatus.Warning";
        String reason = I18n.get("UploadFailure." + status.lastFailure().reason().name(),
                status.lastFailure().statusCode());
        html.append("<br>").append(I18n.get(key, system, reason, status.failuresInARow()));
    }

    @Override
    public void setController(IPluginManager controller, String pluginuid) {
        super.setController(controller, pluginuid);
        core = (CorePlugin) controller.getFeature("de.df.jauswertung.core", pluginuid);
    }

    @Override
    public ButtonInfo[] getQuickButtons() {
        return buttons;
    }

    @Override
    public void dataUpdated(UpdateEvent due) {
        iscResultUploader.setDirtyFlag();
    }
}
