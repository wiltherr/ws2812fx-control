package de.wiltherr.ws2812fx.ui.components;

import com.vaadin.data.HasValue;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Slider;
import de.wiltherr.ws2812fx.Color;
import de.wiltherr.ws2812fx.ui.components.wheelcolorpicker.WheelColorPicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class WS2812ColorPicker extends HorizontalLayout implements HasValue<Color> {

    private static final Logger log = LoggerFactory.getLogger(WS2812ColorPicker.class);
    private final List<ValueChangeListener<Color>> valueChangeListeners = new ArrayList<>();
    private WheelColorPicker wheelColorPicker;
    private Slider whiteValueSlider;
    private CheckBox autoCalcWhiteCheckBox;
    private Color value;

    public WS2812ColorPicker() {
        wheelColorPicker = new WheelColorPicker();

        whiteValueSlider = new Slider(Color.MIN_VALUE, Color.MAX_VALUE, 0);
        whiteValueSlider.setCaption("White");
        whiteValueSlider.setOrientation(SliderOrientation.HORIZONTAL);
        whiteValueSlider.setWidth(185, Unit.PIXELS);

        autoCalcWhiteCheckBox = new CheckBox("Auto calculate white");


        this.setDefaultComponentAlignment(Alignment.BOTTOM_LEFT);
        this.addComponents(wheelColorPicker, whiteValueSlider, autoCalcWhiteCheckBox);
        this.setComponentAlignment(autoCalcWhiteCheckBox, Alignment.MIDDLE_LEFT);
        this.setMargin(false);
        registerListeners();
    }

    private static de.wiltherr.ws2812fx.Color convertVaadinColorToWs2812Fx(com.vaadin.shared.ui.colorpicker.Color vaadinColor) {
        return new de.wiltherr.ws2812fx.Color(vaadinColor.getRed(), vaadinColor.getGreen(), vaadinColor.getBlue(), 0);
    }

    private static com.vaadin.shared.ui.colorpicker.Color convertWS2812FxColorToVaadin(de.wiltherr.ws2812fx.Color ws2812FxColor) {
        return new com.vaadin.shared.ui.colorpicker.Color(ws2812FxColor.getR(), ws2812FxColor.getG(), ws2812FxColor.getB(), 255);
    }

    private static Color convertRGBToRGBW(int r, int g, int b) {
        float red = (float) r;
        float green = (float) g;
        float blue = (float) b;
        //calculate white
        //source: https://stackoverflow.com/a/40318604

        //Get the maximum between R, G, and B
        float tM = Math.max(red, Math.max(green, blue));

        if (tM == 0) {
            //If the maximum value is 0, immediately return pure black.
            return new Color();
        } else {
            //This section serves to figure out what the color with 100% hue is
            float multiplier = 255.0f / tM;
            float hR = red * multiplier;
            float hG = green * multiplier;
            float hB = blue * multiplier;

            //This calculates the Whiteness (not strictly speaking Luminance) of the color
            float M = Math.max(hR, Math.max(hG, hB));
            float m = Math.min(hR, Math.min(hG, hB));
            float Luminance = ((M + m) / 2.0f - 127.5f) * (255.0f / 127.5f) / multiplier;

            //Calculate the output values
            int Wo = (int) (Luminance);
            int Bo = (int) (blue - Luminance);
            int Ro = (int) (red - Luminance);
            int Go = (int) (green - Luminance);

            //Trim them so that they are all between 0 and 255
            if (Wo < 0) Wo = 0;
            if (Bo < 0) Bo = 0;
            if (Ro < 0) Ro = 0;
            if (Go < 0) Go = 0;
            if (Wo > 255) Wo = 255;
            if (Bo > 255) Bo = 255;
            if (Ro > 255) Ro = 255;
            if (Go > 255) Go = 255;

            return new Color(Ro, Go, Bo, Wo);
        }
    }

    private void registerListeners() {
        wheelColorPicker.setJsEventListener(new WheelColorPicker.JSEventListener() {
            @Override
            public void onChange(com.vaadin.shared.ui.colorpicker.Color color) {
                log.error("js event on change: " + color.getCSS());
            }

            @Override
            public void onFocus(com.vaadin.shared.ui.colorpicker.Color color) {

            }

            @Override
            public void onBlur(com.vaadin.shared.ui.colorpicker.Color color) {

            }

            @Override
            public void onSliderUp(com.vaadin.shared.ui.colorpicker.Color color) {
                log.error("js event on slider up:" + color.getCSS());
                Color newValue = convertVaadinColorToWs2812Fx(color);
                if (autoCalcWhiteCheckBox.getValue()) {
                    Color rgbwColor = convertRGBToRGBW(newValue.getR(), newValue.getG(), newValue.getB());
                    newValue.setR(rgbwColor.getR());
                    newValue.setG(rgbwColor.getG());
                    newValue.setB(rgbwColor.getB());
                    newValue.setW(rgbwColor.getW());
                } else {
                    newValue.setW(whiteValueSlider.getValue().intValue());
                }
                Color oldValue = value;
                value = newValue;
                fireValueChangeListener(oldValue);
            }

            @Override
            public void onSliderDown(com.vaadin.shared.ui.colorpicker.Color color) {

            }

            @Override
            public void onSliderMove(com.vaadin.shared.ui.colorpicker.Color color) {
                if (autoCalcWhiteCheckBox.getValue()) {
                    //update white value if auto calculate
                    Color rgbwColor = convertRGBToRGBW(color.getRed(), color.getGreen(), color.getBlue());
                    whiteValueSlider.setValue((double) rgbwColor.getW());
                    whiteValueSlider.markAsDirty();
                }
            }
        });
        whiteValueSlider.addValueChangeListener((ValueChangeListener<Double>) event -> {
            if (!autoCalcWhiteCheckBox.getValue()) {
                Color newValue = convertVaadinColorToWs2812Fx(wheelColorPicker.getValue());
                newValue.setW((event.getValue().intValue()));
                Color oldValue = value;
                this.value = newValue;
                fireValueChangeListener(oldValue);
            }
        });
        autoCalcWhiteCheckBox.addValueChangeListener(event -> whiteValueSlider.setEnabled(!event.getValue()));
    }

    private void fireValueChangeListener(Color oldValue) {
        this.valueChangeListeners.forEach(colorValueChangeListener -> colorValueChangeListener.valueChange(new ValueChangeEvent<Color>(WS2812ColorPicker.this, oldValue, true)));
    }

    @Override
    public Color getValue() {
        return this.value;
    }

    @Override
    public void setValue(Color value) {
        this.value = value;
        wheelColorPicker.setValue(convertWS2812FxColorToVaadin(value));
        whiteValueSlider.setValue((double) value.getW());
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void setReadOnly(boolean readOnly) {

    }

    @Override
    public Registration addValueChangeListener(ValueChangeListener<Color> listener) {
        valueChangeListeners.add(listener);

        return (Registration) () -> valueChangeListeners.remove(listener);
    }
}
