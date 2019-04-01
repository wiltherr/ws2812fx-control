package de.wiltherr.ws2812fx;

public class Options {

    //see https://github.com/kitesurfer1404/WS2812FX/blob/849ec0aaec694ea0f265f9da976237cf765ff3bf/extras/WS2812FX%20Users%20Guide.md#even-more-about-segments
    public static final int RESERVE_INT_VALUE = 0x80;
    public static final int GAMMA_INT_VALUE = 0x08;

    // options
    private boolean reverse; // bit 8: reverse animation
    private FadeOption fadeOption; // bits 5-7: fade rate
    private boolean gammaCorrection; // bits 4: gamma correction
    // bits 1-3: TBD

    public Options(boolean reverse, boolean gammaCorrection, FadeOption fadeOption) {
        this.reverse = reverse;
        this.gammaCorrection = gammaCorrection;
        this.fadeOption = fadeOption;
    }


    public boolean isReverse() {
        return reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public FadeOption getFadeOption() {
        return fadeOption;
    }

    public void setFadeOption(FadeOption fadeOption) {
        this.fadeOption = fadeOption;
    }

    public boolean isGammaCorrection() {
        return gammaCorrection;
    }

    public void setGammaCorrection(boolean gammaCorrection) {
        this.gammaCorrection = gammaCorrection;
    }

    public int calcIntValue() {
        return (this.isReverse() ? Options.RESERVE_INT_VALUE : 0) +
                this.getFadeOption().getIntValue() +
                (this.isGammaCorrection() ? Options.GAMMA_INT_VALUE : 0);
    }

    @Override
    public String toString() {
        return "Options{" +
                "reverse=" + reverse +
                ", fadeOption=" + fadeOption +
                ", gammaCorrection=" + gammaCorrection +
                '}';
    }

    public Options copy() {
        return new Options(reverse, gammaCorrection, fadeOption);
    }
}
