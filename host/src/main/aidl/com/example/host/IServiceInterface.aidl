// IServiceAidlInterface.aidl
package com.example.host;

// Declare any non-default types here with import statements
import com.example.host.IServiceCallback;


interface IServiceAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String getData();
}