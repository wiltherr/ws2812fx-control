package de.wiltherr.ws2812fx.ui;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.*;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.app.service.WS2812FXService;
import de.wiltherr.ws2812fx.ui.views.edit.segmentation.EditSegmentationController;
import de.wiltherr.ws2812fx.ui.views.live.LiveModeController;
import de.wiltherr.ws2812fx.ui.views.navigation.WS2812FXSettingsController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.stream.Collectors;

import static com.vaadin.ui.themes.ValoTheme.BUTTON_LINK;
import static com.vaadin.ui.themes.ValoTheme.MENU_ITEM;

/**
 * This UI is the application entry point. A UI may either represent a browser window
 * (or tab) or some part create an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@StyleSheet("mytheme.scss")
@SpringUI(path = "")
public class WS2812ControlUI extends UI {

    private static final Logger log = LoggerFactory.getLogger(WS2812ControlUI.class);

    private final WS2812FXService ws2812FXService;

    @Autowired
    public WS2812ControlUI(WS2812FXService ws2812FXService) {
        super();
        this.ws2812FXService = ws2812FXService;
    }


    @Override
    protected void init(VaadinRequest vaadinRequest) {
        initWS2812FXStripModel();

        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setHeightUndefined();
        rootLayout.setMargin(false);

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(new MarginInfo(false, true));

        Navigator navigator = new Navigator(this, mainLayout);
        navigator.addView("", new LiveModeController(ws2812FXService));
        navigator.addView("editSegmentation", new EditSegmentationController(ws2812FXService));
//        navigator.addView("list", listView);

        HorizontalLayout navigationLayout = new HorizontalLayout();
        navigationLayout.setSpacing(true);

        Button link1 = new Button("Live Mode", e -> navigator.navigateTo(""));
        link1.addStyleNames(BUTTON_LINK, MENU_ITEM);
        Button link2 = new Button("Edit Segmentation", e -> navigator.navigateTo("editSegmentation"));
        link2.addStyleNames(BUTTON_LINK, MENU_ITEM);

        navigationLayout.addComponents(link1, link2);

        WS2812FXSettingsController settingsController = new WS2812FXSettingsController(ws2812FXService);

        rootLayout.addComponents( //
                navigationLayout, //
                settingsController.getView(),
                mainLayout //
        );

        setContent(rootLayout);
    }

    private void initWS2812FXStripModel() {
        try {
            //synchronize strip model
            SessionData.getStripModel().resetSegments(ws2812FXService.getSegments().stream().map(Segment::copy).collect(Collectors.toList()));
            //stripModel.setBrightness(ws2812FXService.getBrigthness()); TODO
            SessionData.getStripModel().setLength(ws2812FXService.getLength());
        } catch (WS2812FXException e) {
            log.error("Synchronizing model from strip failed while fetching data from external strip. ", e);
            Notification.show("Getting initial data failed: " + e.getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
        }
    }
}
