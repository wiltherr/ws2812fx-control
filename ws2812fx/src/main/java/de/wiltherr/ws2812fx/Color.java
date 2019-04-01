package de.wiltherr.ws2812fx;

import java.util.stream.Stream;

public class Color {


    //RED(255, 0, 0, 0);

    public final static int MAX_VALUE = 255;
    public final static int MIN_VALUE = 0;
    protected int R, G, B, W;

    public Color(int r, int g, int b) {
        this(r, g, b, 0);
    }

    public Color(int r, int g, int b, int w) {
        if (Stream.of(r, g, b, w).anyMatch(i -> i > MAX_VALUE || i < MIN_VALUE)) {
            throw new IllegalArgumentException(
                    String.format("Color values (r, g, b or w) in range of %d and %d.", MIN_VALUE, MAX_VALUE));
        }
        this.R = r;
        this.G = g;
        this.B = b;
        this.W = w;
    }

    public Color() {
    }

    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }

    public int getG() {
        return G;
    }

    public void setG(int g) {
        G = g;
    }

    public int getB() {
        return B;
    }

    public void setB(int b) {
        B = b;
    }

    public int getW() {
        return W;
    }

    public void setW(int w) {
        W = w;
    }

    @Override
    public String toString() {
        return "{" +
                "R=" + R +
                ", G=" + G +
                ", B=" + B +
                ", W=" + W +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        if (R != color.R) return false;
        if (G != color.G) return false;
        if (B != color.B) return false;
        return W == color.W;
    }

    @Override
    public int hashCode() {
        int result = R;
        result = 31 * result + G;
        result = 31 * result + B;
        result = 31 * result + W;
        return result;
    }

    public Color copy() {
        return new Color(R, G, B, W);
    }
}
