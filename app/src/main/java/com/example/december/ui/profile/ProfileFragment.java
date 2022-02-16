package com.example.december.ui.profile;

import static java.lang.Thread.sleep;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.december.LoginActivity;
import com.example.december.R;
import com.example.december.RegisterActivity;
import com.example.december.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ProfileViewModel ProfileViewModel;
    private FragmentProfileBinding binding;
    private TextView mUsername;
    private TextView mEmail;
    private TextView mAdopted;
    private TextView mDonation;
    private TextView mComments;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageRef;
    private LinearLayout mAdoptedLinear;
    private LinearLayout mDonationLinear;
    private LinearLayout mCommentsLinear;
    private Map<String,String> user_comments_group;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mUsername = root.findViewById(R.id.profile_username);
        mEmail = root.findViewById(R.id.profile_email);
        mAdopted = root.findViewById(R.id.profile_Adopted);
        mDonation = root.findViewById(R.id.profile_total_donation);
        mComments = root.findViewById(R.id.profile_comments);
        mAdoptedLinear = root.findViewById(R.id.profile_adopted_linear);
        mDonationLinear = root.findViewById(R.id.profile_donation_linear);
        mCommentsLinear = root.findViewById(R.id.profile_comment_linear);
        mUsername.setText("UserName: "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString());
        mEmail.setText("Email: "+FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Fetching...");
        pd.show();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();

        DocumentReference docRef = db.collection("Users").document(userEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                        Map<String,String> command_group = (Map<String,String>) document.getData().get("Comments");
                        mAdopted.setText("🐕 Adopted Animals: "+adopted_group.size());
                        mDonation.setText("❤ Total Donation: "+document.getData().get("TotalDonation").toString());
                        mComments.setText("📋 Comments: "+command_group.size());
                        if (pd.isShowing()){
                            pd.dismiss();
                        }
                    }

                }
            }
        });






        final Button button = root.findViewById(R.id.profile_sign_out);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("CLICKED");
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity().getApplicationContext(), LoginActivity.class));
            }
        });

        //adopted Linear
        mAdoptedLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Adopted");
                                LinearLayout info_linear = new LinearLayout(getActivity());
                                info_linear.setOrientation(LinearLayout.VERTICAL);

                                List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                                for(int i =0;i<adopted_group.size();i++){
                                    TextView pet = new TextView(getActivity());
                                    pet.setText(i+1+". "+adopted_group.get(i).toString());
                                    pet.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    textParam.setMargins(80,20,80,10);
                                    info_linear.setLayoutParams(textParam);
                                    info_linear.setPadding(80,20,80,10);
                                    TextView line = new TextView(getActivity());
                                    pet.setBackground(getResources().getDrawable(R.drawable.my_border));
                                    line.setText("222222");
                                    line.setVisibility(View.INVISIBLE);
                                    info_linear.addView(line);
                                    info_linear.addView(pet);
                                    String name = adopted_group.get(i).toString();
                                    pet.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            storageRef = storage.getReference("pets/"+(pet.getText().subSequence(3,pet.getText().length())));
                                            storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                @Override
                                                public void onSuccess(ListResult listResult) {
                                                    try {
                                                        File localfile = File.createTempFile("tempfile",".jgp");
                                                        listResult.getItems().get(0).getFile(localfile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                                                Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                                PopupWindow info_pop = new PopupWindow(getActivity());
                                                                info_pop.showAtLocation(pet,Gravity.CENTER,500,500);
                                                                AlertDialog.Builder pet_builder = new AlertDialog.Builder(getActivity());
                                                                pet_builder.setTitle(name);
                                                                LinearLayout info_linear = new LinearLayout(getActivity());
                                                                info_linear.setOrientation(LinearLayout.VERTICAL);
                                                                ImageView pet_img = new ImageView(getActivity());
                                                                pet_img.setImageBitmap(bitmap);
                                                                TextView pet_info = new TextView(getActivity());
                                                                DocumentReference docRef = db.collection("Pets").document(name);
                                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                        if(task.isSuccessful()){
                                                                            DocumentSnapshot document = task.getResult();
                                                                            if(document.exists()) {
                                                                                pet_info.setText(document.getData().get("info").toString());
                                                                            }
                                                                        }
                                                                    }
                                                                });
                                                                LinearLayout.LayoutParams text_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
                                                                text_param.setMargins(10,10,10,10);
                                                                pet_info.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                                pet_info.setPadding(40,40,40,40);
                                                                pet_info.setLayoutParams(text_param);
                                                                info_linear.addView(pet_img);
                                                                info_linear.addView(pet_info);
                                                                pet_builder.setView(info_linear);
                                                                pet_builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                                    }
                                                                });
                                                                pet_builder.show();
                                                            }
                                                        });
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });


                                        }
                                    });

                                }
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setView(info_linear);
                                builder.show();

                            }
                        }
                    }
                });


            }
        });

        //donation Linear
        mDonationLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Total Donations");
                                LinearLayout info_linear = new LinearLayout(getActivity());
                                info_linear.setOrientation(LinearLayout.VERTICAL);

                                List<String> adopted_group = (List<String>) document.getData().get("Donation");
                                for(int i =0;i<adopted_group.size();i++){
                                    TextView donation = new TextView(getActivity());
                                    donation.setText(i+1+". "+adopted_group.get(i).toString());
                                    donation.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    textParam.setMargins(80,20,80,10);
                                    info_linear.setLayoutParams(textParam);
                                    info_linear.setPadding(80,20,80,10);
                                    TextView line = new TextView(getActivity());
                                    donation.setBackground(getResources().getDrawable(R.drawable.my_border));
                                    line.setText("222222");
                                    line.setVisibility(View.INVISIBLE);
                                    info_linear.addView(line);
                                    info_linear.addView(donation);
                                }
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                builder.setView(info_linear);
                                builder.show();

                            }
                        }
                    }
                });


            }
        });

        //comments linear
        mCommentsLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Comments");
                                LinearLayout info_linear = new LinearLayout(getActivity());
                                info_linear.setOrientation(LinearLayout.VERTICAL);

                                Map<String,String> comments_group = (Map<String,String>) document.getData().get("Comments");
                                int count = 1;
                                for (Map.Entry<String, String> entry : comments_group.entrySet()) {
                                    String k = entry.getKey();
                                    String v = entry.getValue();
                                    TextView comment = new TextView(getActivity());
                                    comment.setText(count+". "+v.toString());
                                    count+=1;
                                    comment.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    textParam.setMargins(80,20,80,10);
                                    info_linear.setLayoutParams(textParam);
                                    info_linear.setPadding(80,20,80,10);
                                    TextView line = new TextView(getActivity());
                                    comment.setBackground(getResources().getDrawable(R.drawable.my_border));
                                    line.setText("222222");
                                    line.setVisibility(View.INVISIBLE);
                                    info_linear.addView(line);
                                    info_linear.addView(comment);
                                    comment.setTag(k);

                                    comment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        DocumentSnapshot document = task.getResult();
                                                        if(document.exists()){
                                                            Map<String,String> user_comments_group = (Map<String,String>) document.getData().get("Comments");
                                                            user_comments_group.remove(comment.getTag());
                                                            Map<String, Object> user_comment_list_update = new HashMap<>();
                                                            user_comment_list_update.put("Comments", user_comments_group);
                                                            docRef.update(user_comment_list_update);
                                                            CollectionReference CommentsRef = db.collection("Comments");
                                                            CommentsRef.document(comment.getTag().toString()).delete();
                                                            info_linear.removeAllViews();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        DocumentSnapshot document = task.getResult();
                                        if(document.exists()){

                                        }
                                    }
                                });
                                builder.setView(info_linear);
                                builder.show();
                            }
                        }
                    }
                });

            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}