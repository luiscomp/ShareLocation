package com.example.admed.sharelocation.singletons;

import android.graphics.Bitmap;

/**
 * Created by Pichau on 23/11/2017.
 */

public class FotoSingleton {
    private static FotoSingleton ourInstance = null;

    public Bitmap foto;

    public static FotoSingleton getInstance() {
        if(ourInstance == null) {
            ourInstance = new FotoSingleton();
        }
        return ourInstance;
    }

    public static FotoSingleton resetInstance() {
        ourInstance = null;
        return ourInstance;
    }

    private FotoSingleton() {
    }
}
