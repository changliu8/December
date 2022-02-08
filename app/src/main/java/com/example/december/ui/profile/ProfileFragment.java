package com.example.december.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.december.R;
import com.example.december.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.List;

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

        mUsername.setText("UserName: "+FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString());
        mEmail.setText("Email: "+FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().toString();

        DocumentReference docRef = db.collection("Users").document(userEmail);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                        //List<String> command_group = (List<String>) document.getData().get("Comment");
                        mAdopted.setText("🐕 Adopted Animals: "+adopted_group.size());
                        mDonation.setText("❤ Total Donation: "+document.getData().get("TotalDonation").toString());
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