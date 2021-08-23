// IServiceInterface.aidl
package com.example.host;

// Declare any non-default types here with import statements


interface IServiceInterface {

    void isClick();
    boolean getClick();

    void setStringText(String text);
    String getStringText();

    void setSizeOfText(int size);
    int getSizeOfText();
}