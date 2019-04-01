package de.wiltherr.ws2812fx.ui.views.edit.segmentation;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewBeforeLeaveEvent;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.WS2812FX;
import de.wiltherr.ws2812fx.WS2812FXException;
import de.wiltherr.ws2812fx.app.service.WS2812FXService;
import de.wiltherr.ws2812fx.ui.SessionData;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringUI
public class EditSegmentationController implements View {

    private static final Logger log = LoggerFactory.getLogger(EditSegmentationController.class);

    private final EditSegmentationView view;
    private final WS2812FXStripModel ws2812FXStripModel = SessionData.getStripModel();
    private final List<Segment> segmentationModel;
    private final WS2812FX ws2812FX;

    public EditSegmentationController(WS2812FX ws2812FX) {
        this.ws2812FX = ws2812FX;
        view = new EditSegmentationViewImpl();
        segmentationModel = new ArrayList<>();
        registerListeners();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        segmentationModel.clear();
        segmentationModel.addAll(ws2812FXStripModel.getSegments().stream().map(Segment::copy).collect(Collectors.toList()));
        view.setModel(segmentationModel);
    }

    @Override
    public void beforeLeave(ViewBeforeLeaveEvent event) {
        event.navigate();
        //TODO reset invalid segments
    }

    @Override
    public Component getViewComponent() {
        return view;
    }

    private void registerListeners() {
        view.setListener(new EditSegmentationView.Listener() {
            @Override
            public void clickedResetSegmentation() {
                //reformat indices:
                IntStream.range(0, segmentationModel.size()).forEach(i ->
                        segmentationModel.get(i).setIndex(i)
                );
                resetSegments(segmentationModel);
                view.setModel(segmentationModel); //update view
            }

            @Override
            public void clickedAddSegment() {
                int maxIndex = segmentationModel.stream().mapToInt(Segment::getIndex).max().orElse(-1);
                Segment addSegment = new Segment(maxIndex + 1, -1, -1, WS2812FXService.DEFAULT_CONFIG);
                segmentationModel.add(addSegment);
                view.setModel(segmentationModel); //update view
            }

            @Override
            public void clickedAutoSegmentation() {
                int length = ws2812FXStripModel.getLength();
                //reformat indices:
                IntStream.range(0, segmentationModel.size()).forEach(i -> {
                    Segment segment = segmentationModel.get(i);
                    segment.setIndex(i);
                    segment.setStart(((length / segmentationModel.size()) * i));
                    if (i == segmentationModel.size() - 1)
                        segment.setStop(length - 1);
                    else
                        segment.setStop(((length / segmentationModel.size()) * (i + 1)) - 1);
                });
                //ws2812FXStripModel.resetSegments(segments); //update segmentationModel
                view.setModel(segmentationModel); //update view
            }

            @Override
            public void segmentRemoved(int segmentIndex) {
                segmentationModel.remove(segmentIndex);
                //reformat indices:
                IntStream.range(0, segmentationModel.size()).forEach(i ->
                        segmentationModel.get(i).setIndex(i)
                );
                view.setModel(segmentationModel); //update view
            }
        });
    }

    private void resetSegments(List<Segment> segments) {
        try {
            ws2812FX.resetSegments(segments); //update strip
            ws2812FXStripModel.resetSegments(segments); //update segmentationModel
            log.error("reset segments successfully");
        } catch (WS2812FXException e) {
            log.error("reset segments failed", e);
            Notification.show("Reset segments failed: " + e.getLocalizedMessage(), Notification.Type.WARNING_MESSAGE);
        }
    }
}
