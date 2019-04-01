package de.wiltherr.ws2812fx.ui.views.navigation;

import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import de.wiltherr.ws2812fx.WS2812FX;
import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.ui.SessionData;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WS2812FXSettingsController {

    private static final Logger log = LoggerFactory.getLogger(WS2812FXSettingsController.class);
    private final WS2812FX ws2812FX;
    private final WS2812FXStripModel ws2812FXStripModel = SessionData.getStripModel();
    private final WS2812FXSettingsView view;

    public WS2812FXSettingsController(WS2812FX ws2812FX) {
        this.ws2812FX = ws2812FX;
        this.view = new WS2812FXSettingsViewImpl();
        view.setModel(ws2812FXStripModel);
        registerListeners();
    }

    public Component getView() {
        return view;
    }

    private void registerListeners() {
        view.setListener(new WS2812FXSettingsView.Listener() {
            @Override
            public void onBrightnessChanged(int brightness) {
                changeBrightness(brightness);
            }

            @Override
            public void clickedStartStop() {
                toggleStartStop();
            }

            @Override
            public void clickedPauseResume() {
                togglePauseResume();
            }
        });
    }

    private void toggleStartStop() {
        try {
            if (ws2812FXStripModel.isStopped()) {
                ws2812FX.start();
                ws2812FXStripModel.start();
            } else {
                ws2812FX.stop();
                ws2812FXStripModel.stop();
            }
            view.setModel(ws2812FXStripModel); //update view
        } catch (WS2812FXException e) {
            log.error("failed to send start/stop command to strip. ", e);
            handleWS2812FXConnectorException(e);
        }
    }

    private void togglePauseResume() {
        try {
            if (ws2812FXStripModel.isPaused()) {
                ws2812FX.resume();
                ws2812FXStripModel.resume();
            } else {
                ws2812FX.pause();
                ws2812FXStripModel.pause();
            }
            view.setModel(ws2812FXStripModel); //update view
        } catch (WS2812FXException e) {
            log.error("failed to send pause/resume command to strip. ", e);
            handleWS2812FXConnectorException(e);
        }
    }

    private void changeBrightness(int brightness) {
        try {
            ws2812FX.setBrightness(brightness);
            ws2812FXStripModel.setBrightness(brightness);
        } catch (WS2812FXException e) {
            log.error("failed to set brightness on strip. ", e);
            handleWS2812FXConnectorException(e);
        }
    }

    private void handleWS2812FXConnectorException(WS2812FXException exception) {
        Notification.show(exception.getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
    }
}
