// IServiceInterface.aidl
package com.example.host;

// Declare any non-default types here with import statements
import com.example.host.IServiceCallback;


interface IServiceInterface {


    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}