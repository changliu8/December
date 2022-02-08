package com.example.december.ui.announcement;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.december.R;
import com.example.december.databinding.FragmentAnnouncementBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AnnouncementFragment extends Fragment {

    private AnnouncementViewModel AnnouncementViewModel;
    private FragmentAnnouncementBinding binding;
    private LinearLayout commentsLinear;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    private TextView commentText;
    private Button submitCommentButton;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AnnouncementViewModel =
                new ViewModelProvider(this).get(AnnouncementViewModel.class);

        binding = FragmentAnnouncementBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        commentsLinear = root.findViewById(R.id.commentsLinear);
        commentText = root.findViewById(R.id.comment_text);
        submitCommentButton = root.findViewById(R.id.submit_comment_button);
        db.collection("Comments").orderBy("time").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot document : task.getResult()){

                        LinearLayout comment_linear = new LinearLayout(getActivity());
                        comment_linear.setOrientation(LinearLayout.VERTICAL);

                        LinearLayout client_info_linear = new LinearLayout(getActivity());
                        client_info_linear.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        params.setMargins(10,5,10,5);
                        comment_linear.setPadding(10,5,10,5);

                        comment_linear.setLayoutParams(params);
                        comment_linear.setBackground(getResources().getDrawable(R.drawable.my_border));

                        TextView user_name_textview = new TextView(getActivity());
                        TextView time_textview = new TextView(getActivity());
                        time_textview.setText(document.get("time").toString());
                        RelativeLayout.LayoutParams textViewLayoutParams = new RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                        time_textview.setLayoutParams(textViewLayoutParams);
                        time_textview.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);


                        user_name_textview.setText(document.get("name").toString());
                        client_info_linear.addView(user_name_textview);
                        client_info_linear.addView(time_textview);

                        TextView user_comment_textview = new TextView(getActivity());
                        user_comment_textview.setText("  "+document.get("content").toString());

                        comment_linear.addView(client_info_linear);
                        comment_linear.addView(user_comment_textview);
                        commentsLinear.addView(comment_linear);
                    }
                }
            }
        });

        submitCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> comment = new HashMap<>();
                String comment_content = commentText.getText().toString();
                comment.put("content",comment_content);
                comment.put("name", FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toString());
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                comment.put("time", formatter.format(date).toString());
                db.collection("Comments").add(comment).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        commentsLinear.removeAllViews();
                        db.collection("Comments").orderBy("time").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(QueryDocumentSnapshot document : task.getResult()){

                                        LinearLayout comment_linear = new LinearLayout(getActivity());
                                        comment_linear.setOrientation(LinearLayout.VERTICAL);

                                        LinearLayout client_info_linear = new LinearLayout(getActivity());
                                        client_info_linear.setOrientation(LinearLayout.HORIZONTAL);

                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                        params.setMargins(10,5,10,5);
                                        comment_linear.setPadding(10,5,10,5);

                                        comment_linear.setLayoutParams(params);
                                        comment_linear.setBackground(getResources().getDrawable(R.drawable.my_border));

                                        TextView user_name_textview = new TextView(getActivity());
                                        TextView time_textview = new TextView(getActivity());
                                        time_textview.setText(document.get("time").toString());
                                        RelativeLayout.LayoutParams textViewLayoutParams = new RelativeLayout.LayoutParams(
                                                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                                        time_textview.setLayoutParams(textViewLayoutParams);
                                        time_textview.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                                        user_name_textview.setText(document.get("name").toString());
                                        client_info_linear.addView(user_name_textview);
                                        client_info_linear.addView(time_textview);

                                        TextView user_comment_textview = new TextView(getActivity());
                                        user_comment_textview.setText("  "+document.get("content").toString());

                                        comment_linear.addView(client_info_linear);
                                        comment_linear.addView(user_comment_textview);
                                        commentsLinear.addView(comment_linear);
                                    }
                                }
                            }
                        });
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