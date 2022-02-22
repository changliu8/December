package com.example.december.ui.pets;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.lang.UCharacter;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.MenuPopupWindow;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.december.R;
import com.example.december.databinding.FragmentPetsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.SettingsConfig;
import com.paypal.checkout.createorder.CreateOrder;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.CaptureOrderResult;
import com.paypal.checkout.order.OnCaptureComplete;
import com.paypal.checkout.order.Order;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PayPalButton;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PetsFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private PetsViewModel petsViewModel;
    private FragmentPetsBinding binding;
    private int num_dog,num_row_required;
    private boolean addition_row_required = false;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    ArrayList<Bitmap> dogs_img = new ArrayList<>();
    private ArrayList<Button> donate_buttons;
    private PayPalButton ppButton;
    private static final String YOUR_CLIENT_ID = "AR0CNQVSwCIFL0oS9V42Jo0VWMeWDjMZusuXFS7Ab_f7_F5kJ3nnVi_H94X6lWHoF2odzKPvTpZIFbka";
    Boolean Payment_success = false;
    String pets_name = "";
    private Spinner pets_collection;
    private TableLayout petsLayout;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        num_dog = 10;
        binding = FragmentPetsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        petsLayout = root.findViewById(R.id.pets_layout);
        ppButton = root.findViewById(R.id.petsPayPalButton);
        pets_collection = root.findViewById(R.id.pets_collections);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.pets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pets_collection.setAdapter(adapter);
        pets_collection.setOnItemSelectedListener(this);

        /*
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("🐕 Getting our new members 🐈");
        pd.show();

        storageRef = storage.getReference("pets");
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    prefix.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for(StorageReference pic : listResult.getItems()){
                                if(pic.toString().contains("cat")){
                                    try {
                                    System.out.println(pic);
                                    File localfile = File.createTempFile("tempfile",".jgp");
                                    pic.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                            LinearLayout pet_linear = new LinearLayout(getActivity());
                                            pet_linear.setOrientation(LinearLayout.HORIZONTAL);
                                            pet_linear.setGravity(Gravity.CENTER);
                                            pet_linear.setBackgroundColor(Color.WHITE);
                                            LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            pet_linear.setLayoutParams(Params);
                                            ImageView pet_img = new ImageView(getActivity());
                                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, 400);
                                            pet_img.setLayoutParams(layoutParams);
                                            pet_img.setImageBitmap(bitmap);

                                            LinearLayout.LayoutParams text_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
                                            text_param.setMargins(10,10,10,10);
                                            //new add
                                            LinearLayout pet_info_linear = new LinearLayout(getActivity());
                                            pet_info_linear.setOrientation(LinearLayout.VERTICAL);
                                            pet_info_linear.setPadding(10,10,10,10);
                                            pet_info_linear.setBackground(getResources().getDrawable(R.drawable.my_border));
                                            pet_info_linear.setLayoutParams(text_param);
                                            //new add done
                                            TextView pet_name = new TextView(getActivity());
                                            pet_name.setGravity(Gravity.CENTER);
                                            pet_name.setLayoutParams(text_param);
                                            //pet_name.setBackground(getResources().getDrawable(R.drawable.my_border));
                                            //pet_name.setPadding(10,10,10,10);
                                            TextView pet_age = new TextView(getActivity());
                                            pet_age.setGravity(Gravity.CENTER);
                                            pet_age.setLayoutParams(text_param);

                                            TextView pet_type = new TextView(getActivity());
                                            pet_type.setGravity(Gravity.CENTER);
                                            pet_type.setLayoutParams(text_param);

                                            TextView pet_gender = new TextView(getActivity());
                                            pet_gender.setGravity(Gravity.CENTER);
                                            pet_gender.setLayoutParams(text_param);

                                            pet_info_linear.addView(pet_name);
                                            pet_info_linear.addView(pet_age);
                                            pet_info_linear.addView(pet_type);
                                            pet_info_linear.addView(pet_gender);

                                            int count = 0;
                                            String name = "";
                                            for(int i =0;i<pic.toString().getBytes().length;i++){
                                                if(pic.toString().charAt(i)=='/'){
                                                    count++;
                                                }
                                                if(count==4){
                                                    name+=pic.toString().charAt(i);
                                                }
                                            }
                                            name = name.substring(1);
                                            DocumentReference docRef = db.collection("Pets").document(name);
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            pet_name.setText("Name : "+document.getData().get("Name").toString());
                                                            pet_age.setText("Age : "+document.getData().get("Age").toString());
                                                            pet_type.setText("Type : "+document.getData().get("Type").toString());
                                                            pet_gender.setText("Gender : "+document.getData().get("Gender").toString());
                                                        } else {
                                                            pet_name.setText("JESUS");
                                                        }
                                                    } else {
                                                        pet_name.setText(task.getException().toString());
                                                    }
                                                }
                                            }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(pd.isShowing()){
                                                        pd.dismiss();
                                                    }
                                                }
                                            });
                                            //pet_name.setText(name);
                                            TextView left = new TextView(getActivity());
                                            TextView right = new TextView(getActivity());
                                            left.setText("5555");
                                            right.setText("5555");
                                            right.setVisibility(View.INVISIBLE);
                                            left.setVisibility(View.INVISIBLE);
                                            pet_linear.addView(left);
                                            pet_linear.addView(pet_img);
                                            pet_linear.addView(pet_info_linear);
                                            pet_linear.addView(right);
                                            petsLayout.addView(pet_linear);
                                            TextView invisible = new TextView(getActivity());
                                            invisible.setText("1222222222222222222222");
                                            invisible.setVisibility(View.INVISIBLE);
                                            petsLayout.addView(invisible);
                                            dogs_img.add(bitmap);
                                            String finalName = name;
                                            pet_linear.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    PopupWindow info_pop = new PopupWindow(getActivity());
                                                    info_pop.showAtLocation(pet_linear,Gravity.CENTER,500,500);
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle(finalName);
                                                    LinearLayout info_linear = new LinearLayout(getActivity());
                                                    info_linear.setOrientation(LinearLayout.VERTICAL);
                                                    ImageView pet_img = new ImageView(getActivity());
                                                    pet_img.setImageBitmap(bitmap);
                                                    TextView pet_info = new TextView(getActivity());
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
                                                    pet_info.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                    pet_info.setPadding(40,40,40,40);
                                                    Button button = new Button(getActivity());
                                                    button.setText("DONATE");
                                                    button.setTag(finalName);
                                                    button.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            pets_name=finalName;
                                                            ppButton.callOnClick();
                                                        }
                                                    });
                                                    pet_info.setLayoutParams(text_param);
                                                    info_linear.addView(pet_img);
                                                    info_linear.addView(pet_info);
                                                    info_linear.addView(button);
                                                    builder.setView(info_linear);
                                                    builder.show();
                                                }
                                            });
                                        }

                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }}
                    });
                }
            }
        });

         */

        petsViewModel = new ViewModelProvider(this).get(PetsViewModel.class);

        CheckoutConfig config = new CheckoutConfig(
                getActivity().getApplication(),
                YOUR_CLIENT_ID,
                Environment.SANDBOX,
                String.format("%s://paypalpay", "com.example.december"),
                CurrencyCode.CAD,
                UserAction.PAY_NOW,
                new SettingsConfig(true,true)
        );
        PayPalCheckout.setConfig(config);

        ppButton.setup(
                new CreateOrder() {
                    @Override
                    public void create(@NotNull CreateOrderActions createOrderActions) {
                        ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<>();
                        purchaseUnits.add(
                                new PurchaseUnit.Builder()
                                        .amount(
                                                new Amount.Builder()
                                                        .currencyCode(CurrencyCode.CAD)
                                                        .value("12.99")
                                                        .build()
                                        )
                                        .build()
                        );
                        Order order = new Order(
                                OrderIntent.CAPTURE,
                                new AppContext.Builder()
                                        .userAction(UserAction.PAY_NOW)
                                        .build(),
                                purchaseUnits
                        );
                        createOrderActions.create(order, (CreateOrderActions.OnOrderCreated) null);
                    }
                },
                new OnApprove() {
                    @Override
                    public void onApprove(@NotNull Approval approval) {
                        ProgressDialog ad = new ProgressDialog(getActivity());
                        ad.setMessage("Processing...");
                        ad.show();
                        approval.getOrderActions().capture(new OnCaptureComplete() {
                            @Override
                            public void onCaptureComplete(@NotNull CaptureOrderResult result) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_information,null);
                                builder.setView(view);
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                                if(ad.isShowing()){
                                    ad.dismiss();
                                }
                                builder.show();
                                DocumentReference userRef = db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            if(document.exists()){
                                                List<String> adopted_group = (List<String>) document.getData().get("Adopted");
                                                if(!adopted_group.contains(pets_name)) {
                                                    adopted_group.add(pets_name);
                                                    Map<String, Object> adopted_list_upadte = new HashMap<>();
                                                    adopted_list_upadte.put("Adopted", adopted_group);
                                                    userRef.update(adopted_list_upadte);
                                                }

                                                List<String> donation_group = (List<String>) document.getData().get("Donation");
                                                donation_group.add("10.00");
                                                Map<String, Object> hopperUpdates = new HashMap<>();
                                                hopperUpdates.put("Donation", donation_group);
                                                userRef.update(hopperUpdates);
                                                float total_donation = 0.00f;
                                                for(String donation : donation_group){
                                                    total_donation+=Double.parseDouble(donation);
                                                }
                                                Map<String, Object> totaldonationupdate = new HashMap<>();
                                                totaldonationupdate.put("TotalDonation", ""+total_donation);
                                                userRef.update(totaldonationupdate);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
        );
        //huge.setOrientation(LinearLayout.VERTICAL);
        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        String choice = pets_collection.getSelectedItem().toString();
        petsLayout.removeAllViews();
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("🐕 Getting our new members 🐈");
        pd.show();
        storageRef = storage.getReference("pets");
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for (StorageReference prefix : listResult.getPrefixes()) {
                    prefix.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            ArrayList<Bitmap> curr_pet_image = new ArrayList<>();
                            for(StorageReference pic : listResult.getItems()) {
                                if (pic.toString().toUpperCase().contains(choice.toUpperCase(Locale.ROOT))) {
                                    if (pic.toString().toUpperCase().contains("Cover".toUpperCase())) {
                                        System.out.println(choice);
                                        try {
                                            System.out.println(pic);
                                            File localfile = File.createTempFile("tempfile", ".jgp");
                                            pic.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                    LinearLayout pet_linear = new LinearLayout(getActivity());
                                                    pet_linear.setOrientation(LinearLayout.HORIZONTAL);
                                                    pet_linear.setGravity(Gravity.CENTER);
                                                    pet_linear.setBackgroundColor(Color.WHITE);
                                                    LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                    pet_linear.setLayoutParams(Params);
                                                    ImageView pet_img = new ImageView(getActivity());
                                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, 400);
                                                    pet_img.setLayoutParams(layoutParams);
                                                    pet_img.setImageBitmap(bitmap);

                                                    LinearLayout.LayoutParams text_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                                    text_param.setMargins(10, 10, 10, 10);
                                                    //new add
                                                    LinearLayout pet_info_linear = new LinearLayout(getActivity());
                                                    pet_info_linear.setOrientation(LinearLayout.VERTICAL);
                                                    pet_info_linear.setPadding(10, 10, 10, 10);
                                                    pet_info_linear.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                    pet_info_linear.setLayoutParams(text_param);
                                                    //new add done
                                                    TextView pet_name = new TextView(getActivity());
                                                    pet_name.setGravity(Gravity.CENTER);
                                                    pet_name.setLayoutParams(text_param);
                                                    //pet_name.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                    //pet_name.setPadding(10,10,10,10);
                                                    TextView pet_age = new TextView(getActivity());
                                                    pet_age.setGravity(Gravity.CENTER);
                                                    pet_age.setLayoutParams(text_param);

                                                    TextView pet_type = new TextView(getActivity());
                                                    pet_type.setGravity(Gravity.CENTER);
                                                    pet_type.setLayoutParams(text_param);

                                                    TextView pet_gender = new TextView(getActivity());
                                                    pet_gender.setGravity(Gravity.CENTER);
                                                    pet_gender.setLayoutParams(text_param);

                                                    pet_info_linear.addView(pet_name);
                                                    pet_info_linear.addView(pet_age);
                                                    pet_info_linear.addView(pet_type);
                                                    pet_info_linear.addView(pet_gender);

                                                    int count = 0;
                                                    String name = "";
                                                    for (int i = 0; i < pic.toString().getBytes().length; i++) {
                                                        if (pic.toString().charAt(i) == '/') {
                                                            count++;
                                                        }
                                                        if (count == 4) {
                                                            name += pic.toString().charAt(i);
                                                        }
                                                    }

                                                    name = name.substring(1);
                                                    DocumentReference docRef = db.collection("Pets").document(name);
                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    pet_name.setText("Name : " + document.getData().get("Name").toString());
                                                                    pet_age.setText("Age : " + document.getData().get("Age").toString());
                                                                    pet_type.setText("Type : " + document.getData().get("Type").toString());
                                                                    pet_gender.setText("Gender : " + document.getData().get("Gender").toString());
                                                                } else {
                                                                    pet_name.setText("JESUS");
                                                                }
                                                            } else {
                                                                pet_name.setText(task.getException().toString());
                                                            }
                                                        }
                                                    }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (pd.isShowing()) {
                                                                pd.dismiss();
                                                            }
                                                        }
                                                    });
                                                    //pet_name.setText(name);
                                                    TextView left = new TextView(getActivity());
                                                    TextView right = new TextView(getActivity());
                                                    left.setText("5555");
                                                    right.setText("5555");
                                                    right.setVisibility(View.INVISIBLE);
                                                    left.setVisibility(View.INVISIBLE);
                                                    pet_linear.addView(left);
                                                    pet_linear.addView(pet_img);
                                                    pet_linear.addView(pet_info_linear);
                                                    pet_linear.addView(right);
                                                    petsLayout.addView(pet_linear);
                                                    TextView invisible = new TextView(getActivity());
                                                    invisible.setText("1222222222222222222222");
                                                    invisible.setVisibility(View.INVISIBLE);
                                                    petsLayout.addView(invisible);
                                                    dogs_img.add(bitmap);
                                                    String finalName = name;
                                                    pet_linear.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            PopupWindow info_pop = new PopupWindow(getActivity());
                                                            info_pop.showAtLocation(pet_linear, Gravity.CENTER, 500, 500);
                                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                            builder.setTitle(finalName);
                                                            LinearLayout info_linear = new LinearLayout(getActivity());
                                                            info_linear.setOrientation(LinearLayout.VERTICAL);
                                                            ImageView pet_img = new ImageView(getActivity());
                                                            pet_img.setImageBitmap(bitmap);
                                                            TextView pet_info = new TextView(getActivity());
                                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                    if (task.isSuccessful()) {
                                                                        DocumentSnapshot document = task.getResult();
                                                                        if (document.exists()) {
                                                                            pet_info.setText(document.getData().get("info").toString());
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                            pet_info.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                            pet_info.setPadding(40, 40, 40, 40);
                                                            Button button = new Button(getActivity());
                                                            button.setText("DONATE");
                                                            button.setTag(finalName);
                                                            button.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    pets_name = finalName;
                                                                    ppButton.callOnClick();
                                                                }
                                                            });
                                                            pet_info.setLayoutParams(text_param);
                                                            info_linear.addView(pet_img);
                                                            pet_img.setOnClickListener(new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    pet_img.setImageBitmap(curr_pet_image.get(0));
                                                                    curr_pet_image.add(curr_pet_image.get(0));
                                                                    curr_pet_image.remove(0);
                                                                }
                                                            });
                                                            info_linear.addView(pet_info);
                                                            info_linear.addView(button);
                                                            builder.setView(info_linear);
                                                            builder.show();
                                                        }
                                                    });
                                                }

                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else{
                                        try {
                                            File localfile = File.createTempFile("tempfile", ".jgp");
                                            pic.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                    curr_pet_image.add(bitmap);
                                                }
                                            });
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                } else if (choice.equals("All")) {
                                    if (pic.toString().toUpperCase().contains("Cover".toUpperCase())) {
                                    try {
                                        System.out.println(pic);
                                        File localfile = File.createTempFile("tempfile", ".jgp");
                                        pic.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                                LinearLayout pet_linear = new LinearLayout(getActivity());
                                                pet_linear.setOrientation(LinearLayout.HORIZONTAL);
                                                pet_linear.setGravity(Gravity.CENTER);
                                                pet_linear.setBackgroundColor(Color.WHITE);
                                                LinearLayout.LayoutParams Params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                                pet_linear.setLayoutParams(Params);
                                                ImageView pet_img = new ImageView(getActivity());
                                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, 400);
                                                pet_img.setLayoutParams(layoutParams);
                                                pet_img.setImageBitmap(bitmap);

                                                LinearLayout.LayoutParams text_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                                                text_param.setMargins(10, 10, 10, 10);
                                                //new add
                                                LinearLayout pet_info_linear = new LinearLayout(getActivity());
                                                pet_info_linear.setOrientation(LinearLayout.VERTICAL);
                                                pet_info_linear.setPadding(10, 10, 10, 10);
                                                pet_info_linear.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                pet_info_linear.setLayoutParams(text_param);
                                                //new add done
                                                TextView pet_name = new TextView(getActivity());
                                                pet_name.setGravity(Gravity.CENTER);
                                                pet_name.setLayoutParams(text_param);
                                                //pet_name.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                //pet_name.setPadding(10,10,10,10);
                                                TextView pet_age = new TextView(getActivity());
                                                pet_age.setGravity(Gravity.CENTER);
                                                pet_age.setLayoutParams(text_param);

                                                TextView pet_type = new TextView(getActivity());
                                                pet_type.setGravity(Gravity.CENTER);
                                                pet_type.setLayoutParams(text_param);

                                                TextView pet_gender = new TextView(getActivity());
                                                pet_gender.setGravity(Gravity.CENTER);
                                                pet_gender.setLayoutParams(text_param);

                                                pet_info_linear.addView(pet_name);
                                                pet_info_linear.addView(pet_age);
                                                pet_info_linear.addView(pet_type);
                                                pet_info_linear.addView(pet_gender);

                                                int count = 0;
                                                String name = "";
                                                for (int i = 0; i < pic.toString().getBytes().length; i++) {
                                                    if (pic.toString().charAt(i) == '/') {
                                                        count++;
                                                    }
                                                    if (count == 4) {
                                                        name += pic.toString().charAt(i);
                                                    }
                                                }
                                                name = name.substring(1);
                                                DocumentReference docRef = db.collection("Pets").document(name);
                                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot document = task.getResult();
                                                            if (document.exists()) {
                                                                pet_name.setText("Name : " + document.getData().get("Name").toString());
                                                                pet_age.setText("Age : " + document.getData().get("Age").toString());
                                                                pet_type.setText("Type : " + document.getData().get("Type").toString());
                                                                pet_gender.setText("Gender : " + document.getData().get("Gender").toString());
                                                            } else {
                                                                pet_name.setText("JESUS");
                                                            }
                                                        } else {
                                                            pet_name.setText(task.getException().toString());
                                                        }
                                                    }
                                                }).addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (pd.isShowing()) {
                                                            pd.dismiss();
                                                        }
                                                    }
                                                });
                                                //pet_name.setText(name);
                                                TextView left = new TextView(getActivity());
                                                TextView right = new TextView(getActivity());
                                                left.setText("5555");
                                                right.setText("5555");
                                                right.setVisibility(View.INVISIBLE);
                                                left.setVisibility(View.INVISIBLE);
                                                pet_linear.addView(left);
                                                pet_linear.addView(pet_img);
                                                pet_linear.addView(pet_info_linear);
                                                pet_linear.addView(right);
                                                petsLayout.addView(pet_linear);
                                                TextView invisible = new TextView(getActivity());
                                                invisible.setText("1222222222222222222222");
                                                invisible.setVisibility(View.INVISIBLE);
                                                petsLayout.addView(invisible);
                                                dogs_img.add(bitmap);
                                                String finalName = name;
                                                pet_linear.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        PopupWindow info_pop = new PopupWindow(getActivity());
                                                        info_pop.showAtLocation(pet_linear, Gravity.CENTER, 500, 500);
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                        builder.setTitle(finalName);
                                                        LinearLayout info_linear = new LinearLayout(getActivity());
                                                        info_linear.setOrientation(LinearLayout.VERTICAL);
                                                        ImageView pet_img = new ImageView(getActivity());
                                                        pet_img.setImageBitmap(bitmap);
                                                        TextView pet_info = new TextView(getActivity());
                                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    DocumentSnapshot document = task.getResult();
                                                                    if (document.exists()) {
                                                                        pet_info.setText(document.getData().get("info").toString());
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        pet_info.setBackground(getResources().getDrawable(R.drawable.my_border));
                                                        pet_info.setPadding(40, 40, 40, 40);
                                                        Button button = new Button(getActivity());
                                                        button.setText("DONATE");
                                                        button.setTag(finalName);
                                                        button.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                pets_name = finalName;
                                                                ppButton.callOnClick();
                                                            }
                                                        });
                                                        pet_info.setLayoutParams(text_param);
                                                        info_linear.addView(pet_img);
                                                        info_linear.addView(pet_info);
                                                        info_linear.addView(button);
                                                        builder.setView(info_linear);
                                                        builder.show();
                                                    }
                                                });
                                            }

                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            }}
                    });
                }
            }
        });

    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }
}