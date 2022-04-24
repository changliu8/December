package com.example.december;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {
    private Button mRegisterButton;
    private TextView mName, mEmail, mPassword;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser fUser;
    FirebaseAuth fAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegisterButton = findViewById(R.id.register_button);
        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);

        fAuth = FirebaseAuth.getInstance();
        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String name = mName.getText().toString();
                String password = mPassword.getText().toString();
                fAuth.createUserWithEmailAndPassword(email,password);
                fAuth.signInWithEmailAndPassword(email,password);
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build();
                fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        fUser = fAuth.getCurrentUser();
                        fUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    CollectionReference users = db.collection("Users");
                                    Map<String, Object> data1 = new HashMap<>();
                                    data1.put("name", name);
                                    data1.put("id", fUser.getUid());
                                    data1.put("Adopted",new ArrayList<String>());
                                    data1.put("Donation",new ArrayList<String>());
                                    data1.put("TotalDonation","0.00");
                                    data1.put("Comments",new HashMap<String,String>());
                                    data1.put("icon","0");
                                    users.document(fUser.getEmail()).set(data1);
                                }
                                else{
                                    System.out.println(task.getException());
                                }
                            }
                        });
                    }
                });





            }

        });
    }
}