package de.wiltherr.ws2812fx.ui.views.navigation;

import com.vaadin.ui.Component;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;

public interface WS2812FXSettingsView extends Component {
    public void setListener(Listener listener);

    public void setModel(WS2812FXStripModel model);

    public interface Listener {
        public void onBrightnessChanged(int brightness);

        public void clickedStartStop();

        public void clickedPauseResume();
    }
}
