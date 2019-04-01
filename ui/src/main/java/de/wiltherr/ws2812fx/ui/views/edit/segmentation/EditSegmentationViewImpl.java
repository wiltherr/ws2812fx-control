package de.wiltherr.ws2812fx.ui.views.edit.segmentation;


import com.vaadin.data.Binder;
import com.vaadin.data.ReadOnlyHasValue;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import de.wiltherr.ws2812fx.Segment;
import de.wiltherr.ws2812fx.ui.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EditSegmentationViewImpl extends VerticalLayout implements EditSegmentationView {
    //TODO split this view into model, view and controller
    //TODO after bind model to inputfields create view

    private static final Logger log = LoggerFactory.getLogger(EditSegmentationView.class);

    final HorizontalLayout selectionLayout;
    private final Button autoSegmentationButton;
    private final Button resetSegmentationButton;

    private final Button addSegmentButton;

    Binder<List<Segment>> modelBinder = new Binder<>();
    private EditSegmentationView.Listener listener;

    public EditSegmentationViewImpl() {

        selectionLayout = new HorizontalLayout();
        selectionLayout.setMargin(false);

        addSegmentButton = new Button("Add Segment");
        autoSegmentationButton = new Button("Auto Segmentation");
        resetSegmentationButton = new Button("Reset Segmentation");
        HorizontalLayout controlSegmentationLayout = new HorizontalLayout(addSegmentButton, autoSegmentationButton, resetSegmentationButton);

        this.addComponents(selectionLayout, controlSegmentationLayout);
        this.setSizeFull();
        this.setMargin(false);
    }

    @Override
    public void setListener(EditSegmentationView.Listener listener) {
        this.listener = listener;
        addSegmentButton.addClickListener(event -> {
            listener.clickedAddSegment();
        });
        autoSegmentationButton.addClickListener(event -> listener.clickedAutoSegmentation());
        resetSegmentationButton.addClickListener(event -> {
                    log.error("segmentation changed:" + modelBinder.getBean().stream().map(segment -> segment.getStart() + "-" + segment.getStop()).collect(Collectors.joining(", ")));
                    listener.clickedResetSegmentation();
                }
        );

        modelBinder.addStatusChangeListener(event ->
                log.error("ws2812fxbinder status changed " + modelBinder.getBean().stream().map(Segment::toString).collect(Collectors.joining("\n", "[\n", "}")))
        );
    }

    private Layout createSegmentLayout(Segment segment) {
        VerticalLayout segmentLayout = new VerticalLayout();
        segmentLayout.setMargin(false);
        segmentLayout.setSpacing(false);

        List<Binder.Binding> bindings = new ArrayList();

        HorizontalLayout titleAndDeleteButton = new HorizontalLayout();
        Label titleLabel = new Label();
        ReadOnlyHasValue<String> titleLabelText = new ReadOnlyHasValue<>(titleLabel::setValue);
        bindings.add(modelBinder.forField(titleLabelText).bind(model -> "Segment " + model.get(segment.getIndex()).getIndex(), null));

        Button deleteButton = new Button();
        deleteButton.setIcon(VaadinIcons.DEL_A);
        deleteButton.addClickListener(event -> {
            bindings.forEach(modelBinder::removeBinding);
            segmentLayout.removeAllComponents();
            listener.segmentRemoved(segment.getIndex());
        });

        titleAndDeleteButton.addComponents(titleLabel, deleteButton);

        HorizontalLayout editLayout = new HorizontalLayout();
        TextField startTextField = new TextField("Start");
        startTextField.setWidth(60, Unit.PIXELS);
        bindings.add(
                modelBinder.forField(startTextField)
                        .withValidator(str -> str.length() == 0, "Can't be empty")
                        .withConverter(new StringToIntegerConverter("Must be integer"))
                        .withValidator(i -> i > 0 && i < SessionData.getStripModel().getLength(), "Index must be in between 0 and " + SessionData.getStripModel().getLength())
                        .bind(model -> model.get(segment.getIndex()).getStart(), ((model, startValue) -> model.get(segment.getIndex()).setStart(startValue)))
        );
        TextField stopTextField = new TextField("Stop");
        stopTextField.setWidth(60, Unit.PIXELS);
        bindings.add(modelBinder.forField(stopTextField).bind(model -> String.valueOf(model.get(segment.getIndex()).getStop()), ((model, stopValue) -> model.get(segment.getIndex()).setStop(Integer.valueOf(stopValue)))));
        editLayout.addComponents(startTextField, stopTextField);

        Label modeLabel = new Label();
        ReadOnlyHasValue<String> modeLabelText = new ReadOnlyHasValue<>(modeLabel::setValue);
        bindings.add(modelBinder.forField(modeLabelText).bind(model -> model.get(segment.getIndex()).getConfig().getMode().getModeName(), null));

        Label primaryColorLabel = new Label();
        ReadOnlyHasValue<String> primaryColorLabelText = new ReadOnlyHasValue<>(primaryColorLabel::setValue);
        bindings.add(modelBinder.forField(primaryColorLabelText).bind(items -> items.get(segment.getIndex()).getConfig().getColors().get(0).toString(), null));

        segmentLayout.addComponents(titleAndDeleteButton, editLayout, modeLabel, primaryColorLabel);
        return segmentLayout;
    }

    @Override
    public void setModel(List<Segment> model) {

        if (modelBinder.getBean() == null || modelBinder.getBean().size() != selectionLayout.getComponentCount()) {
            selectionLayout.removeAllComponents();
            modelBinder.setBean(model);
            model.stream().sorted(Comparator.comparingInt(Segment::getIndex)).map(this::createSegmentLayout).forEach(selectionLayout::addComponent);
        } else {
            modelBinder.setBean(model);
        }
    }
}
