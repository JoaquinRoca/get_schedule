package com.joaquinroca.android.getschedule;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {
    private final Activity thisActivity = this;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        super.onCreate(savedInstanceState);
        GetPermissions.checkCalendarPermission(thisActivity);
        setContentView(R.layout.activity_main);
    }

    public void getAgenda(View view) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "get_agenda");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Clicked Get Agenda");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        Intent intent = new Intent(this, ShowAgenda.class);
        startActivity(intent);
    }

}