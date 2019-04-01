package de.wiltherr.ws2812fx;

import java.util.Arrays;

public enum Mode {
    STATIC(0, "Static"),                    //FX_MODE_STATIC
    BLINK(1, "Blink"),//FX_MODE_BLINK
    BREATH(2, "Breath"),//FX_MODE_BREATH
    COLOR_WIPE(3, "Color Wipe"),//FX_MODE_COLOR_WIPE
    COLOR_WIPE_INV(4, "Color Wipe Inverse"),//FX_MODE_COLOR_WIPE_INV
    COLOR_WIPE_REV(5, "Color Wipe Reverse"),//FX_MODE_COLOR_WIPE_REV
    COLOR_WIPE_REV_INV(6, "Color Wipe Reverse Inverse"),//FX_MODE_COLOR_WIPE_REV_INV
    COLOR_WIPE_RANDOM(7, "Color Wipe Random"),//FX_MODE_COLOR_WIPE_RANDOM
    RANDOM_COLOR(8, "Random Color"),//FX_MODE_RANDOM_COLOR
    SINGLE_DYNAMIC(9, "Single Dynamic"),//FX_MODE_SINGLE_DYNAMIC
    MULTI_DYNAMIC(10, "Multi Dynamic"),//FX_MODE_MULTI_DYNAMIC
    RAINBOW(11, "Rainbow"),//FX_MODE_RAINBOW
    RAINBOW_CYCLE(12, "Rainbow Cycle"),//FX_MODE_RAINBOW_CYCLE
    SCAN(13, "Scan"),//FX_MODE_SCAN
    DUAL_SCAN(14, "Dual Scan"),//FX_MODE_DUAL_SCAN
    FADE(15, "Fade"),//FX_MODE_FADE
    THEATER_CHASE(16, "Theater Chase"),//FX_MODE_THEATER_CHASE
    THEATER_CHASE_RAINBOW(17, "Theater Chase Rainbow"),//FX_MODE_THEATER_CHASE_RAINBOW
    RUNNING_LIGHTS(18, "Running Lights"),//FX_MODE_RUNNING_LIGHTS
    TWINKLE(19, "Twinkle"),//FX_MODE_TWINKLE
    TWINKLE_RANDOM(20, "Twinkle Random"),//FX_MODE_TWINKLE_RANDOM
    TWINKLE_FADE(21, "Twinkle Fade"),//FX_MODE_TWINKLE_FADE
    TWINKLE_FADE_RANDOM(22, "Twinkle Fade Random"),//FX_MODE_TWINKLE_FADE_RANDOM
    SPARKLE(23, "Sparkle"),//FX_MODE_SPARKLE
    FLASH_SPARKLE(24, "Flash Sparkle"),//FX_MODE_FLASH_SPARKLE
    HYPER_SPARKLE(25, "Hyper Sparkle"),//FX_MODE_HYPER_SPARKLE
    STROBE(26, "Strobe"),//FX_MODE_STROBE
    STROBE_RAINBOW(27, "Strobe Rainbow"),//FX_MODE_STROBE_RAINBOW
    MULTI_STROBE(28, "Multi Strobe"),//FX_MODE_MULTI_STROBE
    BLINK_RAINBOW(29, "Blink Rainbow"),//FX_MODE_BLINK_RAINBOW
    CHASE_WHITE(30, "Chase White"),//FX_MODE_CHASE_WHITE
    CHASE_COLOR(31, "Chase Color"),//FX_MODE_CHASE_COLOR
    CHASE_RANDOM(32, "Chase Random"),//FX_MODE_CHASE_RANDOM
    CHASE_RAINBOW(33, "Chase Rainbow"),//FX_MODE_CHASE_RAINBOW
    CHASE_FLASH(34, "Chase Flash"),//FX_MODE_CHASE_FLASH
    CHASE_FLASH_RANDOM(35, "Chase Flash Random"),//FX_MODE_CHASE_FLASH_RANDOM
    CHASE_RAINBOW_WHITE(36, "Chase Rainbow White"),//FX_MODE_CHASE_RAINBOW_WHITE
    CHASE_BLACKOUT(37, "Chase Blackout"),//FX_MODE_CHASE_BLACKOUT
    CHASE_BLACKOUT_RAINBOW(38, "Chase Blackout Rainbow"),//FX_MODE_CHASE_BLACKOUT_RAINBOW
    COLOR_SWEEP_RANDOM(39, "Color Sweep Random"),//FX_MODE_COLOR_SWEEP_RANDOM
    RUNNING_COLOR(40, "Running Color"),//FX_MODE_RUNNING_COLOR
    RUNNING_RED_BLUE(41, "Running Red Blue"),//FX_MODE_RUNNING_RED_BLUE
    RUNNING_RANDOM(42, "Running Random"),//FX_MODE_RUNNING_RANDOM
    LARSON_SCANNER(43, "Larson Scanner"),//FX_MODE_LARSON_SCANNER
    COMET(44, "Comet"),//FX_MODE_COMET
    FIREWORKS(45, "Fireworks"),//FX_MODE_FIREWORKS
    FIREWORKS_RANDOM(46, "Fireworks Random"),//FX_MODE_FIREWORKS_RANDOM
    MERRY_CHRISTMAS(47, "Merry Christmas"),//FX_MODE_MERRY_CHRISTMAS
    FIRE_FLICKER(48, "Fire Flicker"),//FX_MODE_FIRE_FLICKER
    FIRE_FLICKER_SOFT(49, "Fire Flicker (soft)"),//FX_MODE_FIRE_FLICKER_SOFT
    FIRE_FLICKER_INTENSE(50, "Fire Flicker (intense)"),//FX_MODE_FIRE_FLICKER_INTENSE
    CIRCUS_COMBUSTUS(51, "Circus Combustus"),//FX_MODE_CIRCUS_COMBUSTUS
    HALLOWEEN(52, "Halloween"),//FX_MODE_HALLOWEEN
    BICOLOR_CHASE(53, "Bicolor Chase"),//FX_MODE_BICOLOR_CHASE
    TRICOLOR_CHASE(54, "Tricolor Chase"),//FX_MODE_TRICOLOR_CHASE
    ICU(55, "ICU"),//FX_MODE_ICU
    CUSTOM_0(56, "Custom 0"),//FX_MODE_CUSTOM_0
    CUSTOM_1(57, "Custom 1"),//FX_MODE_CUSTOM_1
    CUSTOM_2(58, "Custom 2"),//FX_MODE_CUSTOM_2
    CUSTOM_3(59, "Custom 3");//FX_MODE_CUSTOM_3

    private int modeNumber;
    private String modeName;

    Mode(int modeNumber, String modeName) {
        this.modeNumber = modeNumber;
        this.modeName = modeName;
    }

    public static Mode valueOf(int modeNumber) {
        return Arrays.stream(values())
                .filter(mode -> modeNumber == mode.getModeNumber())
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(String.format("No mode with number %d can be found.", modeNumber))
                );
    }

    public int getModeNumber() {
        return modeNumber;
    }

    public String getModeName() {
        return modeName;
    }
}
