package com.example.december.ui.pets;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PetsViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PetsViewModel() {
        DocumentReference docRef = db.collection("pets").document("DOG");
        mText = new MutableLiveData<>();
        mText.setValue("This is Pets fragment");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mText.setValue(document.getData().get("ASD").toString());
                    } else {
                        mText.setValue("JESUS");
                    }
                } else {
                        mText.setValue(task.getException().toString());
                }
            }
        });
    }

    public LiveData<String> getText() {
        return mText;
    }
}