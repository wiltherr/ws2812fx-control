package de.wiltherr.ws2812fx.ui.components.wheelcolorpicker;

import com.vaadin.shared.ui.JavaScriptComponentState;
import com.vaadin.shared.ui.colorpicker.Color;
import elemental.json.Json;
import elemental.json.JsonObject;

public class WheelColorPickerState extends JavaScriptComponentState {
    public JsonObject jsonValue = colorToJsonObject(new Color(0, 0, 0, 255));


    public static Color jsonObjectToColor(JsonObject jsonObject) {
        //reference: https://github.com/fujaru/jquery-wheelcolorpicker/wiki/Plugin%3AMethods#getcolor;
        return new Color(
                (int) (jsonObject.getNumber("r") * 255),
                (int) (jsonObject.getNumber("g") * 255),
                (int) (jsonObject.getNumber("b") * 255),
                (int) (jsonObject.getNumber("a") * 255)
        );
    }


    public static JsonObject colorToJsonObject(Color color) {
        JsonObject result = Json.createObject();
        result.put("r", (double) (color.getRed() / 255d));
        result.put("g", (double) (color.getGreen() / 255d));
        result.put("b", (double) (color.getBlue() / 255d));
        result.put("a", (double) (color.getAlpha() / 255d));
        return result;
    }
}
