package android.hardware.bydauto.doorlock;

/**
 * STUB — real implementation lives in the BYD Dilink system framework.
 * compileOnly: not packaged. We subclass this at runtime; the framework's
 * real abstract class is the one actually loaded on the device.
 */
public abstract class AbsBYDAutoDoorLockListener {
    public AbsBYDAutoDoorLockListener() {}

    /**
     * @param area  one of BYDAutoDoorLockDevice.DOOR_LOCK_AREA_*
     * @param state one of BYDAutoDoorLockDevice.DOOR_LOCK_STATE_*
     */
    public void onDoorLockStatusChanged(int area, int state) {}
}
