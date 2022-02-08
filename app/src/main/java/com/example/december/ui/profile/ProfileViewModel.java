package com.example.december.ui.profile;

import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private FirebaseAuth fAuth;
    private Button mLogoutButton;
    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        fAuth = FirebaseAuth.getInstance();
        mText.setValue("This is profile fragment" + fAuth.getCurrentUser().getDisplayName());
    }

    public LiveData<String> getText() {
        return mText;
    }
}