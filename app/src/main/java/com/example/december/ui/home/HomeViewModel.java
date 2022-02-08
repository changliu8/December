package com.example.december.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String result = "";

    public HomeViewModel() {
        mText = new MutableLiveData<>();
        /*
        DocumentReference docRef = db.collection("users").document("Sett");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //DOG
                        result = document.getData().get("petname").toString();
                        System.out.println(result);
                    } else {
                    }
                } else {
                }
                DocumentReference secondtime = db.collection("pets").document(result);
                secondtime.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                //name
                                Log.d("TAG", "DocumentSnapshot data: " + document.getData().get("Name"));
                                mText.setValue(document.getData().get("Name").toString());
                            } else {
                                Log.d("TAG", "No such document");
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

         */

    }

    public LiveData<String> getText() {
        return mText;
    }
}