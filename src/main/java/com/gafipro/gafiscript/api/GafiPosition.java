package com.gafipro.gafiscript.api;

public record GafiPosition(double x, double y, double z) {
    public GafiPosition add(double dx, double dy, double dz) {
        return new GafiPosition(x + dx, y + dy, z + dz);
    }

    public GafiPosition subtract(double dx, double dy, double dz) {
        return new GafiPosition(x - dx, y - dy, z - dz);
    }

    public double distanceTo(GafiPosition other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static GafiPosition of(double x, double y, double z) {
        return new GafiPosition(x, y, z);
    }
}
