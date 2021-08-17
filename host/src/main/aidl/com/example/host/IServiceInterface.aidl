// IServiceInterface.aidl
package com.example.host;

// Declare any non-default types here with import statements


interface IServiceInterface {
    void serviceThreadStart();
    String getStringText(String text);
    int getSizeOfText(int size);
    int getFlag(int flag);
}