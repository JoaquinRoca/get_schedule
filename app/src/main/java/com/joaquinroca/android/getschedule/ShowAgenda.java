package com.joaquinroca.android.getschedule;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


import java.util.List;

public class ShowAgenda extends AppCompatActivity {



    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // A background activity (an activity that is not visible to the user and has been paused) is no longer critical,
        // so the system may safely kill its process to reclaim memory for other foreground or visible processes.
        // If its process needs to be killed, when the user navigates back to the activity (making it visible on the screen again),
        // its onCreate(Bundle) method will be called with the savedInstanceState it had previously supplied in
        // onSaveInstanceState(Bundle) so that it can restart itself in the same state as the user last left it.
        // https://developer.android.com/reference/android/app/Activity.html
        super.onCreate(savedInstanceState);

        Activity thisActivity = this;

        // Set the activity content to an explicit view. This view is placed directly into the activity's view hierarchy.
        // It can itself be a complex view hierarchy. When calling this method, the layout parameters of the specified view are ignored.
        // Both the width and the height of the view are set by default to MATCH_PARENT.
        // To use your own layout parameters, invoke setContentView(android.view.View, android.view.ViewGroup.LayoutParams) instead.
        // This finds the layout resource that we are using as the main context for onCreate()
        // https://developer.android.com/reference/android/app/Activity.html#setContentView(android.view.View)
        setContentView(R.layout.activity_show_agenda);


        // JOAQUIN: Could be more parsimoniously written as:
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html
        // int recyclerViewId = R.id.recycler_view;
        // View viewToCastAsRecycler = findViewById(recyclerViewId);
        // RecyclerView recyclerView = (RecyclerView) viewToCastAsRecycler;
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        // Return the context of the single, global Application object of the current process.
        // https://developer.android.com/reference/android/content/Context.html#getApplicationContext()
        Context context = getApplicationContext();

        // create a calendar event list from the CalendarService class
        // this will pull all calendar events from our calendar and put placeholder events into the list where no events currently exist
        List<CalendarEvent> calEventList = CalendarService.getCalendarEvents(context, thisActivity);

        // This event adapter will be passed to recyclerView.setAdapter(mAdapter) below to create child views on demand
        // create a new event adapter with the constructor calendar event list and the current context
        // this is a RecyclerView.Adapter<EventAdapter.MyViewHolder>
        // Base class for an Adapter
        // Adapters provide a binding from an app-specific data set to views that are displayed within a RecyclerView.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html
        EventAdapter mAdapter = new EventAdapter(calEventList, context);

        // A LayoutManager is responsible for measuring and positioning item views within a RecyclerView as
        // well as determining the policy for when to recycle item views that are no longer visible to the user.
        // By changing the LayoutManager a RecyclerView can be used to implement a standard vertically scrolling list,
        // a uniform grid, staggered grids, horizontally scrolling collections and more.
        // Several stock layout managers are provided for general use.
        // If the LayoutManager specifies a default constructor or one with the signature (Context, AttributeSet, int, int),
        // RecyclerView will instantiate and set the LayoutManager when being inflated. Most used properties can be then
        // obtained from getProperties(Context, AttributeSet, int, int). In case a LayoutManager specifies both constructors,
        // the non-default constructor will take precedence.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.LayoutManager.html
        RecyclerView.LayoutManager mRecycleLayoutManager = new LinearLayoutManager(getApplicationContext());
        // Set the RecyclerView.LayoutManager that this RecyclerView will use.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html#setLayoutManager(android.support.v7.widget.RecyclerView.LayoutManager)
        recyclerView.setLayoutManager(mRecycleLayoutManager);

        // Sets the RecyclerView.ItemAnimator that will handle animations involving changes to the items in this RecyclerView.
        // By default, RecyclerView instantiates and uses an instance of DefaultItemAnimator.
        // Whether item animations are enabled for the RecyclerView depends on the ItemAnimator
        // and whether the LayoutManager supports item animations.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html#setItemAnimator(android.support.v7.widget.RecyclerView.ItemAnimator)
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // Set a new adapter to provide child views on demand.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html#setAdapter(android.support.v7.widget.RecyclerView.Adapter)
        recyclerView.setAdapter(mAdapter);


        // Setting up swipe left, swipe right

        // method to initialize event rows enable custom swipe
        EventAdapter.setUpItemTouchHelper(thisActivity, recyclerView, context);

    }


}
