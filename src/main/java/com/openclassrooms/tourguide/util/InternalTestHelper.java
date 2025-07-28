package com.openclassrooms.tourguide.util;

/**
 * Stores in a global variable the number of users the application should generate for testing.
 */
public class InternalTestHelper {

    // Set this default up to 100,000 for testing
    private static int internalUserNumber = 100;

    public static void setInternalUserNumber(int internalUserNumber) {
        InternalTestHelper.internalUserNumber = internalUserNumber;
    }

    public static int getInternalUserNumber() {
        return internalUserNumber;
    }
}
