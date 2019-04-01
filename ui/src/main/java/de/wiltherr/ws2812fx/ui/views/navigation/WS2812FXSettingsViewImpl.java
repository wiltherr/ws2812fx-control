package de.wiltherr.ws2812fx.ui.views.navigation;

import com.vaadin.data.Binder;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Slider;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;

import static com.vaadin.ui.themes.ValoTheme.*;

public class WS2812FXSettingsViewImpl extends HorizontalLayout implements WS2812FXSettingsView {

    private final Button startStopButton;
    private final Button pauseResumeButton;
    private final Slider brightnessSlider;
    //icons: https://vaadin.com/components/vaadin-icons/html-examples/icons-basic-demos
    Binder<WS2812FXStripModel> modelBinder = new Binder<>();

    public WS2812FXSettingsViewImpl() {
        startStopButton = new Button();
        startStopButton.setIcon(VaadinIcons.POWER_OFF);
        startStopButton.addStyleNames(BUTTON_ICON_ONLY, BUTTON_BORDERLESS, MENU_ITEM, "start-stop");

        ReadOnlyHasValue<String> startStopButtonAddStyleName = new ReadOnlyHasValue<>(startStopButton::addStyleName);
        modelBinder.bind(startStopButtonAddStyleName, ws2812FXStripModel -> ws2812FXStripModel.isStopped() ? "stopped" : "not-stopped", null);

        ReadOnlyHasValue<String> startStopButtonRemoveStyleName = new ReadOnlyHasValue<>(startStopButton::removeStyleName);
        modelBinder.bind(startStopButtonRemoveStyleName, ws2812FXStripModel -> ws2812FXStripModel.isStopped() ? "not-stopped" : "stopped", null);

        pauseResumeButton = new Button();
        pauseResumeButton.addStyleNames(BUTTON_ICON_ONLY, BUTTON_BORDERLESS, MENU_ITEM, "pause-resume");
        ReadOnlyHasValue<String> pauseResumeButtonAddStyleName = new ReadOnlyHasValue<>(pauseResumeButton::addStyleName);
        modelBinder.bind(pauseResumeButtonAddStyleName, ws2812FXStripModel -> ws2812FXStripModel.isPaused() ? "paused" : "not-paused", null);

        ReadOnlyHasValue<String> pauseResumeButtonRemoveStyleName = new ReadOnlyHasValue<>(pauseResumeButton::removeStyleName);
        modelBinder.bind(pauseResumeButtonRemoveStyleName, ws2812FXStripModel -> ws2812FXStripModel.isPaused() ? "not-paused" : "paused", null);

        ReadOnlyHasValue<Resource> pauseResumeButtonIcon = new ReadOnlyHasValue<>(pauseResumeButton::setIcon);
        modelBinder.bind(pauseResumeButtonIcon, ws2812FXStripModel -> ws2812FXStripModel.isPaused() ? VaadinIcons.PLAY : VaadinIcons.PAUSE, null);

        brightnessSlider = new Slider(0, 255);
        brightnessSlider.setResolution(0);
        brightnessSlider.setOrientation(SliderOrientation.HORIZONTAL);
        brightnessSlider.setWidth(200, Sizeable.Unit.PIXELS);

        addComponents(startStopButton, pauseResumeButton, brightnessSlider);
    }

    @Override
    public void setListener(WS2812FXSettingsView.Listener listener) {
        startStopButton.addClickListener(event -> listener.clickedStartStop());
        pauseResumeButton.addClickListener(event -> listener.clickedPauseResume());
        brightnessSlider.addValueChangeListener(event -> {
            listener.onBrightnessChanged(event.getValue().intValue());
        });
    }

    @Override
    public void setModel(WS2812FXStripModel model) {
        modelBinder.readBean(model);
    }
}
