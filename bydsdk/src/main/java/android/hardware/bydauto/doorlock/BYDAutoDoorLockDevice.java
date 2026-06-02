package android.hardware.bydauto.doorlock;

import android.content.Context;
import android.hardware.bydauto.AbsBYDAutoDevice;

/**
 * STUB of the real BYD framework class (android.hardware.bydauto.doorlock).
 * compileOnly -> NOT bundled in the APK; the device's framework provides the
 * real class at runtime.
 *
 * IMPORTANT — constant inlining:
 * The real integer values of these constants are NOT published in the Javadoc.
 * If we wrote `= 1` (a compile-time constant) javac/kotlinc would INLINE the
 * literal into our service and it would compare against the WRONG number at
 * runtime. By initializing with a non-constant expression (Integer.parseInt)
 * the field gets NO ConstantValue attribute, so the compiler emits a real
 * field read (getstatic) that resolves to the framework's true value on the
 * car. The "0"/"1"/"2" below are placeholders and are never used at runtime.
 */
public class BYDAutoDoorLockDevice extends AbsBYDAutoDevice {

    public static final int DOOR_LOCK_STATE_INVALID = Integer.parseInt("0");
    public static final int DOOR_LOCK_STATE_UNLOCK  = Integer.parseInt("1");
    public static final int DOOR_LOCK_STATE_LOCK    = Integer.parseInt("2");

    public static final int DOOR_LOCK_COMMAND_SUCCESS       = Integer.parseInt("0");
    public static final int DOOR_LOCK_COMMAND_FAILED        = Integer.parseInt("1");
    public static final int DOOR_LOCK_COMMAND_TIMEOUT       = Integer.parseInt("2");
    public static final int DOOR_LOCK_COMMAND_BUSY          = Integer.parseInt("3");
    public static final int DOOR_LOCK_COMMAND_INVALID_VALUE = Integer.parseInt("4");

    public static final int DOOR_LOCK_AREA_LEFT_FRONT      = Integer.parseInt("0");
    public static final int DOOR_LOCK_AREA_LEFT_REAR       = Integer.parseInt("1");
    public static final int DOOR_LOCK_AREA_RIGHT_FRONT     = Integer.parseInt("2");
    public static final int DOOR_LOCK_AREA_RIGHT_REAR      = Integer.parseInt("3");
    public static final int DOOR_LOCK_AREA_BACK            = Integer.parseInt("4");
    public static final int DOOR_LOCK_AREA_CHILDLOCK_LEFT  = Integer.parseInt("5");
    public static final int DOOR_LOCK_AREA_CHILDLOCK_RIGHT = Integer.parseInt("6");

    public static BYDAutoDoorLockDevice getInstance(Context con) { return null; }

    public int getDoorLockStatus(int area) { return 0; }

    public void registerListener(AbsBYDAutoDoorLockListener l) {}

    public void unregisterListener(AbsBYDAutoDoorLockListener l) {}
}
