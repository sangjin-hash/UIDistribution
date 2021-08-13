// IServiceInterface.aidl
package com.example.host;

// Declare any non-default types here with import statements
import com.example.host.IServiceCallback;


interface IServiceInterface {
<<<<<<< HEAD
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String getData();
=======


    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
>>>>>>> ryu
}