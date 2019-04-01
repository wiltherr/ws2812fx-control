package de.wiltherr.ws2812fx.ui.views.edit.segmentation;

import com.vaadin.ui.Component;
import de.wiltherr.ws2812fx.Segment;

import java.util.List;

public interface EditSegmentationView extends Component {

    public void setListener(Listener listener);

    public void setModel(List<Segment> model);

    public interface Listener {
        public void clickedResetSegmentation();

        public void clickedAddSegment();

        public void clickedAutoSegmentation();

        public void segmentRemoved(int segmentIndex);
    }
}
