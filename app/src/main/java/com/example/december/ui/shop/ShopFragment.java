package com.example.december.ui.shop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.december.R;
import com.example.december.databinding.FragmentShopBinding;
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
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.approve.Approval;
import com.paypal.checkout.approve.OnApprove;
import com.paypal.checkout.cancel.OnCancel;
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
import java.util.Map;

public class ShopFragment extends Fragment {

    private ShopViewModel ShopViewModel;
    private FragmentShopBinding binding;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageRef;
    private LinearLayout itemsLinear;
    private TextView mTotalText;
    private ArrayList<TextView> number_textview;
    private PayPalButton ppButton;
    private static final String YOUR_CLIENT_ID = "AR0CNQVSwCIFL0oS9V42Jo0VWMeWDjMZusuXFS7Ab_f7_F5kJ3nnVi_H94X6lWHoF2odzKPvTpZIFbka";



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ShopViewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        number_textview = new ArrayList<TextView>();
        binding = FragmentShopBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        itemsLinear = root.findViewById(R.id.item_linear);
        storageRef = storage.getReference("store");
        mTotalText = root.findViewById(R.id.total);
        ppButton = root.findViewById(R.id.payPalButton);
        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("New inventory is on the way...");
        pd.show();
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference prefix : listResult.getPrefixes()){
                    prefix.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for(StorageReference pic : listResult.getItems()){
                                try {
                                    File localfile = File.createTempFile("tempfile",".jgp");
                                    System.out.println(localfile);
                                    pic.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                                            LinearLayout Item_layout = new LinearLayout(getActivity());
                                            Item_layout.setOrientation(LinearLayout.HORIZONTAL);
                                            Item_layout.setGravity(Gravity.CENTER);
                                            Item_layout.setBackground(getResources().getDrawable(R.drawable.my_border));
                                            LinearLayout.LayoutParams item_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
                                            item_param.setMargins(30,30,30,30);
                                            Item_layout.setLayoutParams(item_param);
                                            Item_layout.setPadding(10,10,10,10);
                                            ImageView item_img = new ImageView(getActivity());
                                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(400, 400);
                                            item_img.setLayoutParams(layoutParams);
                                            item_img.setImageBitmap(bitmap);

                                            LinearLayout info_layout = new LinearLayout(getActivity());
                                            info_layout.setOrientation(LinearLayout.VERTICAL);
                                            info_layout.setGravity(Gravity.CENTER);
                                            TextView name_text = new TextView(getActivity());
                                            name_text.setWidth(300);
                                            String item_name = "";
                                            int count = 0;
                                            for(int i =0;i<pic.toString().getBytes().length;i++){
                                                if(pic.toString().charAt(i)=='/'){
                                                    count++;
                                                }
                                                if(count==4){
                                                    item_name+=pic.toString().charAt(i);
                                                }
                                            }
                                            item_name = item_name.substring(1).replace("%20"," ");
                                            name_text.setText(item_name);
                                            TextView price_text = new TextView(getActivity());
                                            DocumentReference docRef = db.collection("store").document(item_name);
                                            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        if (document.exists()) {
                                                            price_text.setText(document.getData().get("price").toString());
                                                        } else {
                                                            price_text.setText("");
                                                        }
                                                    } else {
                                                        price_text.setText(task.getException().toString());
                                                    }
                                                }
                                            });
                                            info_layout.addView(name_text);
                                            info_layout.addView(price_text);

                                            LinearLayout counter_layout = new LinearLayout(getActivity());
                                            counter_layout.setOrientation(LinearLayout.HORIZONTAL);
                                            counter_layout.setGravity(Gravity.CENTER);
                                            TextView number = new TextView(getActivity());
                                            number.setText("0");
                                            Button minus = new Button(getActivity());
                                            minus.setText("-");
                                            Button add = new Button(getActivity());
                                            add.setText("+");
                                            minus.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(Integer.parseInt(number.getText().toString())>0){
                                                        int current_number = Integer.parseInt(number.getText().toString());
                                                        int after_add = current_number-1;
                                                        String finalResult = ""+after_add;
                                                        number.setText(finalResult);
                                                        Float item_price = Float.parseFloat(price_text.getText().toString());
                                                        Float total_price = Float.parseFloat(mTotalText.getText().toString().replace("total : ",""));
                                                        String total_price_string = "total : "+String.format("%.2f", total_price-item_price);
                                                        mTotalText.setText(total_price_string);
                                                    }
                                                }
                                            });
                                            add.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    int current_number = Integer.parseInt(number.getText().toString());
                                                    int after_add = current_number+1;
                                                    String finalResult = ""+after_add;
                                                    number.setText(finalResult);
                                                    Float item_price = Float.parseFloat(price_text.getText().toString());
                                                    Float total_price = Float.parseFloat(mTotalText.getText().toString().replace("total : ",""));
                                                    System.out.println(total_price);
                                                    String total_price_string = "total : "+String.format("%.2f", total_price+item_price);
                                                    mTotalText.setText(total_price_string);
                                                }
                                            });

                                            number_textview.add(number);
                                            counter_layout.addView(minus);
                                            counter_layout.addView(number);
                                            counter_layout.addView(add);

                                            Item_layout.addView(item_img);
                                            Item_layout.addView(info_layout);
                                            Item_layout.addView(counter_layout);
                                            itemsLinear.addView(Item_layout);

                                            if (pd.isShowing()){
                                                pd.dismiss();
                                            }

                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
        //paypal method
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

        //paypal,creating order
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
                                                        .value(mTotalText.getText().toString().replace("total : ",""))
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
                //paypal, update firebase if the payment went through
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
                                                List<String> donation_group = (List<String>) document.getData().get("Donation");
                                                donation_group.add(mTotalText.getText().toString().replace("total : ",""));
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

                                                for(TextView textview: number_textview){
                                                    textview.setText("0");
                                                }
                                                mTotalText.setText("total : 0.00");
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                }


        );
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}