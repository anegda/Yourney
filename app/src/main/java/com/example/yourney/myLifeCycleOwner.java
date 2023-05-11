package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

public class myLifeCycleOwner implements LifecycleOwner {
    public myLifeCycleOwner(){
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return null;
    }
}
