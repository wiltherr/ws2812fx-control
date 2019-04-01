//package de.wiltherr.ws2812fx.ui.views.edit.segmentation;
//
//import com.vaadin.data.Binder;
//import com.vaadin.data.ReadOnlyHasValue;
//import com.vaadin.icons.VaadinIcons;
//import com.vaadin.ui.*;
//import de.wiltherr.ws2812fx.Segment;
//import de.wiltherr.ws2812fx.ui.WS2812FXStripModel;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class SegmentationSelector extends CustomComponent {
//
//    final List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();
//    final HorizontalLayout rootLayout;
//    final HorizontalLayout selectionLayout;
//    final private Binder<LinkedHashMap<Integer, Boolean>> selectionMapBinder;
//    final private Binder<WS2812FXStripModel> ws2812FXStripBinder;
//
//    public SegmentationSelector(Binder<WS2812FXStripModel> ws2812FXStripBinder) {
//        //init fields and binders
//        this.ws2812FXStripBinder = ws2812FXStripBinder;
//        selectionMapBinder = new Binder<>();
//        selectionMapBinder.setBean(new LinkedHashMap<>());
////        selectionMapBinder.setBean(this.ws2812FXStripBinder.getBean().getSegments().stream().map(Segment::getIndex)
////                .collect(Collectors.toMap(Function.identity(), x -> Boolean.FALSE))); //TODO nullpointer
//
//        //init view layouts
//        rootLayout = new HorizontalLayout();
//
//        selectionLayout = new HorizontalLayout();
//        selectionLayout.setMargin(false);
//
//        rootLayout.setMargin(false);
//
//        Button addButton = new Button("Add Segment");
////        addButton.addClickListener()
//
//        rootLayout.addComponents(selectionLayout);
//        setCompositionRoot(rootLayout);
//
//
////        ws2812FXStripBinder.addStatusChangeListener((StatusChangeListener) event -> {
////            if (ws2812FXStripBinder.getBean().getSegments().size() != selectionMapBinder.getBean().size()) {
////                selectionMapBinder.setBean(this.ws2812FXStripBinder.getBean().getSegments().stream().map(Segment::getIndex)
////                        .collect(Collectors.toMap(Function.identity(), x -> Boolean.FALSE)));
////                resetLayout();
////            }
////        });
//    }
//
//    private void resetLayout() {
//        selectionLayout.removeAllComponents();
//        ws2812FXStripBinder.getBean().getSegments().stream().map(this::createSegmentLayout).forEach(selectionLayout::addComponent);
//    }
//
//
//
//    public void updateModel() {
//
//        if(updatedSegmentList.size() != selectionMapBinder.getBean().size()) {
//            LinkedHashMap<Integer,Boolean> newSelectionMap = new LinkedHashMap<>();
//            for (int i = 0; i < updatedSegmentList.size(); i++) {
//                Integer segmentIndex = updatedSegmentList.get(i).getIndex();
//                newSelectionMap.put(segmentIndex, Boolean.FALSE);
//            }
//            selectionMapBinder.setBean(newSelectionMap);
//        }
//
//
//    }
//
//    public Set<Integer> getSelected() {
//        return selectionMapBinder.getBean().entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toSet());
//    }
//
//    public Set<Integer> getNotSelected() {
//        return selectionMapBinder.getBean().entrySet().stream().filter(selection -> !selection.getValue()).map(Map.Entry::getKey).collect(Collectors.toSet());
//    }
//
//    public void addSelectionChangeListener(SelectionChangeListener selectionChangeListener) {
//        selectionChangeListeners.add(selectionChangeListener);
//    }
//
//
//    private void fireSelectionChangeListener() {
//        selectionChangeListeners.forEach(selectionChangeListener -> selectionChangeListener.selectionChanged(getSelected(), getNotSelected()));
//    }
//
//    public interface SelectionChangeListener {
//        void selectionChanged(Set<Integer> selectedSegments, Set<Integer> notSelectedSegments);
//    }
//}
