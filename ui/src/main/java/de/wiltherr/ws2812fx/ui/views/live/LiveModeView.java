package de.wiltherr.ws2812fx.ui.views.live;

import com.vaadin.ui.Component;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.SegmentConfig;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;

import java.util.Set;

public interface LiveModeView extends Component {

    public void setListener(Listener listener);

    public void setWS2812FXModel(WS2812FXStripModel ws2812FXStripModel);

    void setLiveSegmentConfigModel(SegmentConfig segmentConfig);

    public interface Listener {


//        void onSelectionChanged(Set<Integer> selectedSegmentIndices, Set<Integer> notSelectedSegmentIndices);

        void onSegmentSelect(Set<Segment> newSelectedSegments);

        void onSegmentUnselect(Set<Segment> newUnselectedSegments);

        void onLiveSegmentConfigChange();
    }
}
