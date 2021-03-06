package com.example.december.ui.profile;

import static android.app.Activity.RESULT_OK;
import static java.lang.Thread.sleep;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.ContentResolver;

import com.example.december.LoginActivity;
import com.example.december.R;
import com.example.december.RegisterActivity;
import com.example.december.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ProfileFragment extends Fragment{

    private final int PICK_IMAGE_REQUEST = 22;

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
    private LinearLayout mInfoLinear;
    private Map<String,String> user_comments_group;
    private ImageView user_icon;
    private Uri filePath;
    StorageReference storageReference;

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
        mInfoLinear = root.findViewById(R.id.personal_linear);
        mUsername.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString());
        mEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
        user_icon = root.findViewById(R.id.user_icon);
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("We are carefully checking your information");
        pd.show();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();


        //retrieve data from firebase
        DocumentReference docRef = db.collection("Users").document(userEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                        Map<String,String> command_group = (Map<String,String>) document.getData().get("Comments");
                        mAdopted.setText("???? Adopted Pets: "+adopted_group.size());
                        mDonation.setText("??? Total Donation: "+document.getData().get("TotalDonation").toString());
                        mComments.setText("???? Comments: "+command_group.size());
                        if(document.getData().get("icon").toString().contains("1")){
                            StorageReference httpsReference = storage.getReferenceFromUrl("gs://december-eltbj.appspot.com/icon/"+document.getData().get("id").toString()+".jpg");
                            try {
                                File localfile = File.createTempFile("tempfile", ".jgp");
                                httpsReference.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                        user_icon.setImageBitmap(bitmap);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (pd.isShowing()){
                            pd.dismiss();
                        }
                    }

                }
            }
        });

        user_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });







        final Button button = root.findViewById(R.id.profile_sign_out);

        //sign out and to the login page
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
                                ScrollView info_scroll = new ScrollView(getActivity());
                                info_scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1));


                                List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                                for(int i =0;i<adopted_group.size();i++){
                                    TextView pet = new TextView(getActivity());
                                    pet.setText(i+1+". "+adopted_group.get(i).toString());
                                    pet.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                                info_scroll.addView(info_linear);
                                builder.setView(info_scroll);
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
                                ScrollView info_scroll = new ScrollView(getActivity());
                                info_scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1));


                                List<String> adopted_group = (List<String>) document.getData().get("Donation");
                                for(int i =0;i<adopted_group.size();i++){
                                    TextView donation = new TextView(getActivity());
                                    donation.setText(i+1+". "+adopted_group.get(i).toString());
                                    donation.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                                info_scroll.addView(info_linear);
                                builder.setView(info_scroll);
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
                                ScrollView info_scroll = new ScrollView(getActivity());
                                info_scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200,1));
                                Map<String,String> comments_group = (Map<String,String>) document.getData().get("Comments");
                                int count = 1;
                                for (Map.Entry<String, String> entry : comments_group.entrySet()) {
                                    String k = entry.getKey();
                                    String v = entry.getValue();
                                    TextView comment = new TextView(getActivity());
                                    comment.setText(count+". "+v.toString());
                                    count+=1;
                                    comment.setTextSize(20);
                                    LinearLayout.LayoutParams textParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    textParam.setMargins(80,20,80,10);
                                    info_linear.setLayoutParams(textParam);
                                    info_linear.setPadding(80,20,80,10);
                                    TextView line = new TextView(getActivity());
                                    comment.setBackground(getResources().getDrawable(R.drawable.my_border));
                                    line.setText("222222");
                                    line.setVisibility(View.INVISIBLE);
                                    info_linear.addView(line);
                                    info_linear.addView(comment);
                                    LinearLayout button_linear = new LinearLayout(getActivity());
                                    button_linear.setOrientation(LinearLayout.HORIZONTAL);
                                    button_linear.setGravity(Gravity.CENTER);
                                    LinearLayout.LayoutParams button_linear_para = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    button_linear.setLayoutParams(button_linear_para);
                                    button_linear_para.setMargins(0,20,0,20);
                                    LinearLayout.LayoutParams button_para = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    button_para.setMargins(10,0,10,0);
                                    Button edit_button = new Button(getActivity());
                                    Button delete_button = new Button(getActivity());
                                    edit_button.setText("Edit");
                                    edit_button.setBackgroundColor(Color.GREEN);
                                    delete_button.setText("Delete");
                                    delete_button.setBackgroundColor(Color.RED);
                                    edit_button.setLayoutParams(button_para);
                                    delete_button.setLayoutParams(button_para);
                                    button_linear.addView(edit_button);
                                    button_linear.addView(delete_button);
                                    info_linear.addView(button_linear);
                                    delete_button.setTag(k);

                                    int finalCount = count-1;
                                    delete_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        DocumentSnapshot document = task.getResult();
                                                        if(document.exists()){
                                                            Map<String,String> user_comments_group = (Map<String,String>) document.getData().get("Comments");
                                                            user_comments_group.remove(delete_button.getTag());
                                                            Map<String, Object> user_comment_list_update = new HashMap<>();
                                                            user_comment_list_update.put("Comments", user_comments_group);
                                                            docRef.update(user_comment_list_update);
                                                            CollectionReference CommentsRef = db.collection("Comments");
                                                            CommentsRef.document(delete_button.getTag().toString()).delete();
                                                            info_linear.removeView(comment);
                                                            info_linear.removeView(button_linear);
                                                            int tmp = Integer.parseInt(mComments.getText().toString().substring(13));
                                                            tmp-=1;
                                                            mComments.setText("???? Comments: "+tmp);
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    edit_button.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        DocumentSnapshot document = task.getResult();
                                                        if(document.exists()){
                                                            Map<String,String> user_comments_group = (Map<String,String>) document.getData().get("Comments");
                                                            AlertDialog.Builder edit_builder = new AlertDialog.Builder(getActivity());
                                                            edit_builder.setTitle("Edit Comment");
                                                            LinearLayout comment_edit_window = new LinearLayout(getActivity());
                                                            comment_edit_window.setOrientation(LinearLayout.VERTICAL);
                                                            EditText comment_text_for_edit = new EditText(getActivity());
                                                            comment_text_for_edit.setText(user_comments_group.get(delete_button.getTag()));
                                                            comment_edit_window.addView(comment_text_for_edit);
                                                            edit_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {

                                                                }
                                                            });
                                                            edit_builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    //not null
                                                                    if(comment_text_for_edit.getText().toString().length()>0){
                                                                        user_comments_group.put(delete_button.getTag().toString(),comment_text_for_edit.getText().toString());
                                                                        Map<String, Object> user_comment_list_update = new HashMap<>();
                                                                        user_comment_list_update.put("Comments", user_comments_group);
                                                                        docRef.update(user_comment_list_update);
                                                                        CollectionReference CommentsRef = db.collection("Comments");
                                                                        CommentsRef.document(delete_button.getTag().toString()).update("content",comment_text_for_edit.getText().toString());
                                                                        String old_comment_content = comment.getText().toString();
                                                                        int comment_counter = 0;
                                                                        for(int i =0;i<old_comment_content.length();i++){
                                                                            if(old_comment_content.charAt(i)!='.'){
                                                                                comment_counter++;
                                                                            }
                                                                            else{
                                                                                break;
                                                                            }
                                                                        }
                                                                        comment.setText(old_comment_content.substring(0,comment_counter)+". "+comment_text_for_edit.getText().toString());
                                                                    }
                                                                    else{
                                                                        Map<String,String> user_comments_group = (Map<String,String>) document.getData().get("Comments");
                                                                        user_comments_group.remove(delete_button.getTag());
                                                                        Map<String, Object> user_comment_list_update = new HashMap<>();
                                                                        user_comment_list_update.put("Comments", user_comments_group);
                                                                        docRef.update(user_comment_list_update);
                                                                        CollectionReference CommentsRef = db.collection("Comments");
                                                                        CommentsRef.document(delete_button.getTag().toString()).delete();
                                                                        info_linear.removeView(comment);
                                                                        info_linear.removeView(button_linear);
                                                                        int tmp = Integer.parseInt(mComments.getText().toString().substring(13));
                                                                        tmp-=1;
                                                                        mComments.setText("???? Comments: "+tmp);
                                                                    }
                                                                }
                                                            });

                                                            edit_builder.setView(comment_edit_window);
                                                            edit_builder.show();

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
                                info_scroll.addView(info_linear);
                                builder.setView(info_scroll);
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                //getting image from gallery
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), filePath);

                //Setting image to ImageView
                user_icon.setImageBitmap(bitmap);
                uploadImage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    //upload image to firebase
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading

            // Defining the child of storageReference
            StorageReference storageRef = storage.getReference();

            DocumentReference docRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            StorageReference ref
                                    = storageRef
                                    .child("icon/"+document.getData().get("id").toString()+".jpg");
                            ref.putFile(filePath)
                                    .addOnSuccessListener(
                                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(
                                                        UploadTask.TaskSnapshot taskSnapshot)
                                                {
                                                    docRef.update("icon","1");
                                                }
                                            })

                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e)
                                        {
                                            System.out.println("I am in failure");
                                        }
                                    })
                                    .addOnProgressListener(
                                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                                // Progress Listener for loading
                                                // percentage on the dialog box
                                                @Override
                                                public void onProgress(
                                                        UploadTask.TaskSnapshot taskSnapshot)
                                                {
                                                    double progress
                                                            = (100.0
                                                            * taskSnapshot.getBytesTransferred()
                                                            / taskSnapshot.getTotalByteCount());
                                                }
                                            });

                        }

                    }
                }
            });


            // adding listeners on upload
            // or failure of image

        }
    }


}
