package de.wiltherr.ws2812fx.ui.components.wheelcolorpicker;

import com.vaadin.annotations.JavaScript;
import com.vaadin.annotations.StyleSheet;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.AbstractJavaScriptComponent;
import com.vaadin.ui.JavaScriptFunction;
import elemental.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;

@StyleSheet("vaadin://bower_components/jquery-wheelcolorpicker/css/wheelcolorpicker.css")
@JavaScript({"https://ajax.googleapis.com/ajax/libs/jquery/2.0.3/jquery.min.js", "vaadin://bower_components/jquery-wheelcolorpicker/jquery.wheelcolorpicker.js", "vaadin://connectors/wheelcolorpicker-connector.js"})
public class WheelColorPicker extends AbstractJavaScriptComponent {

    private static final Logger log = LoggerFactory.getLogger(WheelColorPicker.class);

    private JSEventListener jsEventListener;

    private ArrayList<Consumer<Color>> valueChangeListeners = new ArrayList<>();

    //refrence: https://github.com/vaadin/book-examples/blob/master/src/com/vaadin/book/examples/client/js/MyComponent.java
    public WheelColorPicker() {
        registerListeners();
    }

    private void registerListeners() {
        //system events for vaadin value
        addFunction("onValueChange", (JavaScriptFunction) arguments -> {
            JsonObject jsonObject = arguments.getObject(0);
            Color color = WheelColorPickerState.jsonObjectToColor(jsonObject);

            setValue(color, true, false);
        });

        //direct access to jquery-wheelcolorpicker events (see  https://github.com/fujaru/jquery-wheelcolorpicker/wiki/Plugin%3AEvents)
        //TODO add functions for all events. for possible event overview see https://github.com/fujaru/jquery-wheelcolorpicker/wiki/Plugin%3AEvents
        addFunction("onChange", (JavaScriptFunction) arguments -> {
            JsonObject jsonObject = arguments.getObject(0);
            Color color = WheelColorPickerState.jsonObjectToColor(jsonObject);
            if (jsEventListener != null) {
                jsEventListener.onChange(color);
            }
        });
        addFunction("onFocus", (JavaScriptFunction) arguments -> {
            if (jsEventListener != null) {
                Color wheelColor = WheelColorPickerState.jsonObjectToColor(arguments.getObject(0));
                jsEventListener.onFocus(wheelColor);
            }
        });
        addFunction("onBlur", (JavaScriptFunction) arguments -> {
            if (jsEventListener != null) {
                Color wheelColor = WheelColorPickerState.jsonObjectToColor(arguments.getObject(0));
                jsEventListener.onBlur(wheelColor);
            }
        });
        addFunction("onSliderUp", (JavaScriptFunction) arguments -> {
            if (jsEventListener != null) {
                Color wheelColor = WheelColorPickerState.jsonObjectToColor(arguments.getObject(0));
                jsEventListener.onSliderUp(wheelColor);
            }
        });
        addFunction("onSliderDown", (JavaScriptFunction) arguments -> {
            if (jsEventListener != null) {
                Color wheelColor = WheelColorPickerState.jsonObjectToColor(arguments.getObject(0));
                jsEventListener.onSliderDown(wheelColor);
            }
        });
        addFunction("onSliderMove", (JavaScriptFunction) arguments -> {
            if (jsEventListener != null) {
                Color wheelColor = WheelColorPickerState.jsonObjectToColor(arguments.getObject(0));
                jsEventListener.onSliderMove(wheelColor);
            }
        });
    }

    public void setJsEventListener(JSEventListener jsEventListener) {
        this.jsEventListener = jsEventListener;
    }

    @Override
    public WheelColorPickerState getState() {
        return (WheelColorPickerState) super.getState();
    }

    @Override
    public WheelColorPickerState getState(boolean markAsDirty) {
        return (WheelColorPickerState) super.getState(markAsDirty);
    }

    private void fireValueChangeListener(Color color) {
        valueChangeListeners.forEach(colorConsumer -> colorConsumer.accept(color));
    }

    public void setValue(Color value, boolean fireServerValueChangeListener, boolean updateOnClient) {
        if (updateOnClient) {
            //trigger clientside state change listener (value will be updated in input field)
            getState(true).jsonValue = WheelColorPickerState.colorToJsonObject(value);
        } else {
            getState(false).jsonValue = WheelColorPickerState.colorToJsonObject(value);
        }

        //fire value change listeners after set new value
        if (fireServerValueChangeListener) {
            fireValueChangeListener(value); //trigger serverside value change listener
        }
    }

    public Color getValue() {
        return WheelColorPickerState.jsonObjectToColor(getState(false).jsonValue);
    }

    public void setValue(Color value) {
        setValue(value, true, true);
    }

    public void addValueChangeListener(Consumer<Color> colorConsumer) {
        valueChangeListeners.add(colorConsumer);
    }

    public interface JSEventListener {
        public void onChange(Color color); //will be triggered when value change listener is triggered

        public void onFocus(Color color);

        public void onBlur(Color color);

        public void onSliderUp(Color color);

        public void onSliderDown(Color color);

        public void onSliderMove(Color color);
    }
}
