package com.sunnykatiyar.appmanager.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

class PageViewModel extends ViewModel {

    private final MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private final LiveData<String> mText = Transformations.map(mIndex, input -> "Hello world from section: " + input);

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }
}