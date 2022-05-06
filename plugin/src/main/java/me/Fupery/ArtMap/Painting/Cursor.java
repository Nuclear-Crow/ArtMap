package me.Fupery.ArtMap.Painting;

class Cursor {
    private final int limit;
    private final int resolutionFactor;
    private final int yawOffset;
    private int x, y;
    private float pitch, yaw;
    private boolean yawOffCanvas;
    private boolean pitchOffCanvas;
    private final float playerToCanvasHorizontalDistance = 0.6480925F; //If you come up with a good way to calculate this at runtime, please implement that.
    private final float playerToCanvasVerticalDistance = -0.00287597F; //If you come up with a good way to calculate this at runtime, please implement that.

    Cursor(int yawOffset, int resolutionFactor) {
        this.yawOffset = yawOffset;
        this.resolutionFactor = resolutionFactor;
        limit = (128 / resolutionFactor) - 1;
        yawOffCanvas = false;
        pitchOffCanvas = false;
        int mid = limit / 2;
        x = mid;
        y = mid;
    }

    void setPitch(float pitch) {
        if (Math.abs(this.pitch - pitch) > .0001) {
            this.pitch = pitch;
            updateYPos();
        }
    }

    void setYaw(float yaw) {
        if (Math.abs(this.yaw - yaw) > .0001) {
            this.yaw = yaw;
            updateXPos();
            updateYPos(); // Might be a bit counterintuitive, but while pitch can only affect Y, yaw can affect both X and Y
        }
    }

    private void updateXPos() {
        float yaw = (float)Math.toRadians(getAdjustedYaw());

        float physicalX = (float)(Math.tan(yaw)*playerToCanvasHorizontalDistance); //This is the coordinate within canvas' plane, with the middle of canvas being the coordinate origin
        x = (int)Math.floor( (physicalX + 0.5)*128/resolutionFactor );
        x = clampCoordinate(x);
    }

    private void updateYPos() {
        float pitch = (float)Math.toRadians(getAdjustedPitch());
        float yaw = (float)Math.toRadians(getAdjustedYaw());

        float physicalY = (float)(Math.tan(pitch)/Math.cos(yaw)*playerToCanvasHorizontalDistance + playerToCanvasVerticalDistance); //This is the coordinate within canvas' plane, with the middle of canvas being the coordinate origin
        y = (int)Math.floor( (physicalY + 0.5)*128/resolutionFactor );
        y = clampCoordinate(y);
    }

    private float getAdjustedYaw() {
        float yaw = this.yaw;
        float start = -180;
        float end = 180;

        float width = end - start;
        float offsetValue = yaw - start;

        yaw = (float) (offsetValue - (Math.floor(offsetValue / width) * width)) + start;

        yaw += (yaw > 0) ? -yawOffset : yawOffset;

        yawOffCanvas = (yaw > 45 || yaw < -45);
        return checkBounds(yaw);
    }

    private float getAdjustedPitch() {
        pitchOffCanvas = (pitch > 45 || pitch < -45);
        return checkBounds(pitch);
    }

    private float checkBounds(float value) {

        if (value > 40) {
            return 40;

        } else if (value < -40) {
            return -40;
        }
        return value;
    }

    private int clampCoordinate(int val){
        if(val>limit) return limit;
        if(val<0) return 0;
        return val;
    }

    int getX() {return x;}

    int getY() {
        return y;
    }

    boolean isOffCanvas() {
        return yawOffCanvas || pitchOffCanvas;
    }
}
