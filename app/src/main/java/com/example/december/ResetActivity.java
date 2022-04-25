package com.example.december;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetActivity extends AppCompatActivity {

    private Button mResetButton,mSuccessOKButton;
    private TextView mEmail,mResetHint,mResetText;
    private LinearLayout mResetLinear,mSuccessLinear;
    private ImageView img;
    FirebaseAuth fAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        mResetButton = findViewById(R.id.reset_button);
        mSuccessOKButton = findViewById(R.id.success_ok);
        mEmail = findViewById(R.id.reset_email_text);
        mResetHint = findViewById(R.id.reset_hint);
        mResetText = findViewById(R.id.reset_text);
        fAuth = FirebaseAuth.getInstance();
        mResetLinear = findViewById(R.id.reset_linear);
        img = findViewById(R.id.pic);
        mSuccessLinear = findViewById(R.id.success_linear);

        Animation disappear = new AlphaAnimation(1.0f,0);
        disappear.setDuration(1000);
        disappear.setRepeatCount(0);

        Animation appear = new AlphaAnimation(0,1.0f);
        appear.setDuration(1000);
        appear.setRepeatCount(0);

        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String email = mEmail.getText().toString().trim();
                if(!email.equals("")) {
                    //send the user an email
                    if(Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        fAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    //animation
                                    mResetText.startAnimation(disappear);
                                    mResetText.setVisibility(View.GONE);
                                    mResetLinear.startAnimation(disappear);
                                    mResetLinear.setVisibility(View.GONE);
                                    //img.setVisibility(View.VISIBLE);
                                    mSuccessLinear.startAnimation(appear);
                                    mSuccessLinear.setVisibility(View.VISIBLE);
                                }
                                else{
                                    Toast.makeText(ResetActivity.this,"Incorrect Email, Please Register",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(ResetActivity.this,"Please provide a VALID email",Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(ResetActivity.this,"Please provide a VALID email",Toast.LENGTH_LONG).show();
                }


            }
        });
        mSuccessOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}