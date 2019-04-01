package de.wiltherr.ws2812fx;

public enum FadeOption {

    /**
     * https://github.com/kitesurfer1404/WS2812FX/blob/849ec0aaec694ea0f265f9da976237cf765ff3bf/extras/WS2812FX%20Users%20Guide.md
     * Fade Options can be used with following modes:
     * FX_MODE_TWINKLE_FADE
     * FX_MODE_TWINKLE_FADE_RANDOM
     * FX_MODE_LARSON_SCANNER
     * FX_MODE_COMET
     * FX_MODE_FIREWORKS
     * FX_MODE_FIREWORKS_RANDOM
     */
    NO_OPTION(0x00),
    XFAST(0x10),
    FAST(0x20),
    MEDIUM(0x30),
    SLOW(0x40),
    XSLOW(0x50),
    XXSLOW(0x60),
    GLACIAL(0x70);

    private int fadeOptionIntValue;

    private FadeOption(int fadeOptionIntValue) {
        this.fadeOptionIntValue = fadeOptionIntValue;
    }

    public int getIntValue() {
        return fadeOptionIntValue;
    }
}
