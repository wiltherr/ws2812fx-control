package de.wiltherr.ws2812fx.ui.views.live;


import com.vaadin.data.Binder;
import com.vaadin.data.StatusChangeListener;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.CheckBoxGroup;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Slider;
import com.vaadin.ui.VerticalLayout;
import de.wiltherr.ws2812fx.Mode;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.SegmentConfig;
import de.wiltherr.ws2812fx.WS2812FX;
import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;
import de.wiltherr.ws2812fx.ui.components.WS2812ColorPicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LiveModeViewImpl extends VerticalLayout implements LiveModeView {
    private static final Logger log = LoggerFactory.getLogger(LiveModeViewImpl.class);
    private final Binder<SegmentConfig> liveSegmentConfigBinder = new Binder<>();
    private final CheckBoxGroup<SegmentSelectItem> segmentSelectCheckBoxGroup;
    private final Binder<AtomicBoolean> segmentationModeBinder = new Binder<>();
    Binder<WS2812FXStripModel> ws2812FXStripBinder = new Binder<>();


    public LiveModeViewImpl() {

        segmentationModeBinder.setBean(new AtomicBoolean(false));
        segmentSelectCheckBoxGroup = new CheckBoxGroup<>("Segment Auswahl");

        /********* *************************/

        ComboBox<Mode> modeComboBox = new ComboBox<>();
        modeComboBox.setCaption("Mode");
        modeComboBox.setItemCaptionGenerator(Mode::getModeName);
        modeComboBox.setTextInputAllowed(false);
        modeComboBox.setDataProvider(new ListDataProvider<>(Arrays.asList(Mode.values())));
        liveSegmentConfigBinder.bind(modeComboBox, SegmentConfig::getMode, SegmentConfig::setMode);

        Slider speedValueSlider = new Slider(0, 100, 1);
        speedValueSlider.setCaption("Speed");
        speedValueSlider.setOrientation(SliderOrientation.HORIZONTAL);
        speedValueSlider.setWidth(600, Unit.PIXELS);
        liveSegmentConfigBinder.forField(speedValueSlider).withConverter(sliderValue -> {
            int absolut_speed = (int) ((WS2812FX.SPEED_MAX / speedValueSlider.getMax()) * sliderValue);
            int speed = (int) (WS2812FX.SPEED_MAX - absolut_speed);
            if (speed < WS2812FX.SPEED_MIN) speed = WS2812FX.SPEED_MIN;
            return speed;
        }, ws2812fxValue -> {
            double sliderValue = ((WS2812FX.SPEED_MAX - ws2812fxValue) * speedValueSlider.getMax()) / WS2812FX.SPEED_MAX;
            return sliderValue;
        }).bind(SegmentConfig::getSpeed, SegmentConfig::setSpeed);

        VerticalLayout colorPickersLayout = new VerticalLayout();
        colorPickersLayout.setMargin(false);
        IntStream.range(0, 3).forEach(i -> { //TODO replace range end (3) with static config variable
            WS2812ColorPicker ws2812ColorPicker = new WS2812ColorPicker();
            liveSegmentConfigBinder.forField(ws2812ColorPicker).bind(segmentConfig -> segmentConfig.getColors().get(i),
                    (segmentConfig, color) -> segmentConfig.getColors().set(i, color));
            colorPickersLayout.addComponent(ws2812ColorPicker);

        });

        /************ *********************/

        this.addComponents(segmentSelectCheckBoxGroup, modeComboBox, speedValueSlider, colorPickersLayout);
        this.setSizeFull();
        this.setMargin(false);
    }

    @Override
    public void setListener(LiveModeView.Listener listener) {
        liveSegmentConfigBinder.addStatusChangeListener((StatusChangeListener) event -> {
            listener.onLiveSegmentConfigChange();
        });

        segmentSelectCheckBoxGroup.addSelectionListener((MultiSelectionListener<SegmentSelectItem>) event -> {
            listener.onSegmentSelect(event.getAddedSelection().stream().map(segmentSelectItem -> (Segment) segmentSelectItem).collect(Collectors.toSet()));
            listener.onSegmentUnselect(event.getRemovedSelection().stream().map(segmentSelectItem -> (Segment) segmentSelectItem).collect(Collectors.toSet()));
        });

        ws2812FXStripBinder.addStatusChangeListener(event ->
                log.error("ws2812fxbinder status changed " + ws2812FXStripBinder.getBean().getSegments().stream().map(Segment::toString).collect(Collectors.joining("\n", "[\n", "}")))
        );
    }

    @Override
    public void setWS2812FXModel(WS2812FXStripModel ws2812FXStripModel) {
        ws2812FXStripBinder.setBean(ws2812FXStripModel);
        if (liveSegmentConfigBinder.getBean() == null && ws2812FXStripModel.getSegments().size() > 0) {
            //init work configuration
            setLiveSegmentConfigModel(ws2812FXStripModel.getSegment(0).getConfig().copy());
        }
        segmentSelectCheckBoxGroup.setItems(ws2812FXStripBinder.getBean().getSegments().stream().map(SegmentSelectItem::new).collect(Collectors.toList()));
    }

    @Override
    public void setLiveSegmentConfigModel(SegmentConfig segmentConfig) {
        liveSegmentConfigBinder.setBean(segmentConfig);
    }

    public class SegmentSelectItem extends Segment {

        public SegmentSelectItem(Segment segment) {
            super(segment.getIndex(), segment.getStart(), segment.getStop(), segment.getConfig());
        }

        @Override
        public String toString() {
            return "Segment " + this.getIndex() + " (" + this.getStart() + " - " + this.getStop() + ")";
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public int compareTo(Segment o) {
            return super.compareTo(o);
        }
    }
}
