package de.wiltherr.ws2812fx.ui.views.live;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.SegmentConfig;
import de.wiltherr.ws2812fx.WS2812FX;
import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.ui.SessionData;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@SpringUI
public class LiveModeController implements View {

    private static final Logger log = LoggerFactory.getLogger(LiveModeController.class);
    final LiveModeView view;
    private final WS2812FX ws2812FX;
    private final WS2812FXStripModel ws2812FXStripModel = SessionData.getStripModel();
    SegmentConfig liveSegmentConfigModel;
    Set<Integer> segmentSelection = new HashSet<>();

    public LiveModeController(WS2812FX ws2812FX) {
        this.ws2812FX = ws2812FX;
        view = new LiveModeViewImpl();
        registerListeners();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        view.setWS2812FXModel(ws2812FXStripModel);
        liveSegmentConfigModel = ws2812FXStripModel.getSegment(0).getConfig().copy();
        view.setLiveSegmentConfigModel(liveSegmentConfigModel);
    }

    @Override
    public Component getViewComponent() {
        return view;
    }

    private void registerListeners() {
        view.setListener(new LiveModeView.Listener() {


            @Override
            public void onSegmentSelect(Set<Segment> newSelectedSegments) {
                Set<Integer> added = newSelectedSegments.stream().map(Segment::getIndex).collect(Collectors.toSet());
                segmentSelection.addAll(added);
                updateSegmentsConfig(added, liveSegmentConfigModel);
            }

            @Override
            public void onSegmentUnselect(Set<Segment> newUnselectedSegments) {
                Set<Integer> removed = newUnselectedSegments.stream().map(Segment::getIndex).collect(Collectors.toSet());
                segmentSelection.removeAll(removed);
                //TODO maybe update removed items with default config
            }

            @Override
            public void onLiveSegmentConfigChange() {
                updateSegmentsConfig(segmentSelection, liveSegmentConfigModel);
            }
        });
    }


    private void updateSegmentsConfig(Set<Integer> segmentSelection, SegmentConfig segmentConfig) {
        if (segmentSelection.isEmpty())
            return;

        SegmentConfig segmentConfigCopy = segmentConfig.copy();
        if (segmentSelection.size() == ws2812FXStripModel.getSegments().size()) {
            try {
                ws2812FX.updateSegmentAll(segmentConfigCopy); //update strip
                ws2812FXStripModel.updateSegmentAll(segmentConfigCopy); //update model
                log.info("Updated all segments config successfully");
            } catch (WS2812FXException e) {
                log.error("Failed to update all segments config / segment configuration: " + segmentConfigCopy, e);
                Notification.show("Failed to update all segments config: " + e.getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
            }
        } else {
            try {
                ws2812FX.updateSegmentMulti(segmentSelection, segmentConfigCopy); //update strip
                ws2812FXStripModel.updateSegmentMulti(segmentSelection, segmentConfigCopy); //update model
                log.info("Updated multi segments config successfully");
            } catch (WS2812FXException e) {
                log.error("Failed to update multi segments config / segment configuration: " + segmentConfigCopy, e);
                Notification.show("Failed to update all segments config: " + e.getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
            }
//            List<Integer> failedList = new ArrayList<>();
//            segmentSelection.forEach(segmentIndex -> {
//                try {
//                    ws2812FXService.updateSegment(segmentIndex, segmentConfigCopy);
//                    ws2812FXStripModel.updateSegment(segmentIndex, segmentConfigCopy);
//                    log.error("Updated segment " + segmentIndex + " config successfully");
//                } catch (WS2812FXException e) {
//                    log.error("Failed to update segment " + segmentIndex + " config / segment configuration: " + segmentConfigCopy, e);
//                    failedList.add(segmentIndex);
//                }
//            });
//            if (!failedList.isEmpty())
//                Notification.show("Failed to updated segmentConfig for segments "
//                        + failedList.stream().map(String::valueOf).collect(Collectors.joining(", ")) + ".", Notification.Type.WARNING_MESSAGE);
        }

    }
}
