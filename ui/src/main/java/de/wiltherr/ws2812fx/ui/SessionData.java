package de.wiltherr.ws2812fx.ui;


public class SessionData {
    private static WS2812FXStripModel ws2812FXStripModel = new WS2812FXStripModel();

    public static WS2812FXStripModel getStripModel() {
        return ws2812FXStripModel;
    }
}
