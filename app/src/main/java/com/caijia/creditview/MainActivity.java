package com.caijia.creditview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private CreditView mCreditView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCreditView = (CreditView) findViewById(R.id.credit_view);

        mCreditView.setProgress(90);
        mCreditView.start();
    }
}
