package com.tiktok.paneli;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class PayActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    BillingProcessor bp;
    private TransactionDetails purchaseTransactionDetails = null;
    private static final String TAG = "Odeme";
    Button button;
    LocalDataManager localDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);

        button = findViewById(R.id.button);
        localDataManager = new LocalDataManager();
        localDataManager.setSharedPreference(getApplicationContext(), "point", Integer.toString(500));


        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlGlYs0GL18GkKq71jIJv7l2uaJLDaCKhpjhch4l9vn52XfyOunEnP2POvHc8ZRLrLZlREZtZKjmiwqm64S+TVuwVFsNqGSJr+6PH5w0qwDw1okUkxZz66B49Vm4qrW6C3mMYzztWL4aVJT0PrX93lkU91SxEof0e/C1w5H2sB4VYRG9FMTm4gdBTqliBdNXxL0xe6m6h63vP2iFkyl4juwUv6oxt+CQgSYfIFZJF0mKj+KZ8IZB660qT8qxHoiDS0fQvNK1StYGjh0f+tTpihFh2gTctO4jGrFKxoJDdl74R+l1aG3WqO2PPLT8QRf0PNxF/N++K4K3xs/DhgLQ98QIDAQAB", this);
        bp.initialize();



    }


    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

        String point =  localDataManager.getSharedPreference(getApplicationContext(),"point", "0");

        System.out.println(point);
        int pointInt =  Integer.parseInt(point);
        pointInt = pointInt + 500;
        localDataManager.setSharedPreference(getApplicationContext(), "point", Integer.toString(pointInt));
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }
    private boolean hasSubscription() {

        if (purchaseTransactionDetails != null) {
            return purchaseTransactionDetails.purchaseInfo != null;
        }
        return false;
    }
    @Override
    public void onBillingInitialized() {
        Log.d("Odeme", "onBillingInitialized: ");

        final String milyaroyunparasi1 = "TT Paneli Premium";



        // purchaseTransactionDetails = bp.getSubscriptionTransactionDetails(milyaroyunparasi1);


        bp.loadOwnedPurchasesFromGoogle();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bp.isOneTimePurchaseSupported()) {
                    bp.purchase(PayActivity.this, milyaroyunparasi1);
                } else {
                    Log.d("Odeme", "onBillingInitialized: Subscription updated is not supported");
                }
            }
        });





    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PayActivity.this,Main2Activity.class);
        startActivity(intent);
        finish();
    }

}