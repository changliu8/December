package com.example.december.ui.announcement;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnnouncementViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AnnouncementViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is announcement fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}