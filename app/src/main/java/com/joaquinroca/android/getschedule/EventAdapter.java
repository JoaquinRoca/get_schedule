package com.joaquinroca.android.getschedule;

/*
  Created by joaquinroca on 8/14/17.
  swipe to dismiss inspired by:
  https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.joaquinroca.android.getschedule.DateTransforms.dateAsString;


class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {

    final private static int PENDING_REMOVAL_TIMEOUT = 1500;
    final private List<CalendarEvent> eventList;
    final private List<CalendarEvent> eventsPendingRemoval;
    final private Context context;
    // hanlder for running delayed runnables
    final private Handler handler = new Handler();
    // map of items to pending runnables, so we can cancel a removal if need be
    final private HashMap<CalendarEvent, Runnable> pendingRunnables = new HashMap<>();


    // Constructor for EventAdapter
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    EventAdapter(List<CalendarEvent> calEventList, Context context) {
        eventsPendingRemoval = new ArrayList<>();
        this.eventList = calEventList;
        this.context = context;
    }


    // Subclass MyViewHolder
    class MyViewHolder extends RecyclerView.ViewHolder {
        // undoButton to un-swipe
        final Button undoButton;
        // the event row
        final View currentView;
        // the recycler view that contains the event row
        final ViewParent parentView;
        // the view that contains the start time of the event
        final TextView start;
        // the view that contains the entire event description
        final TextView fullDescription;

        // Constructor for MyViewHolder
        MyViewHolder(View view) {
            super(view);
            currentView = view;
            parentView = view.getParent();
            fullDescription = view.findViewById(R.id.fullDescription);
            start = view.findViewById(R.id.start);
            undoButton = view.findViewById(R.id.undo_button);
        }

    }


    // method to initialize event rows to be able to swipe
    static void setUpItemTouchHelper(
            final Activity thisActivity,
            final RecyclerView recyclerView,
            final Context context
    ) {
        // creating colors for backgrounds during swipe
        final int materialGreen = ContextCompat.getColor(context, R.color.materialGreen);
        final int materialRed = ContextCompat.getColor(context, R.color.materialRed);

        // callbacks for drag (0 -- no drag events) and swipe (LEFT | RIGHT)
        // gives directions for what to do when swiped
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {

            // we want to cache these and not allocate anything repeatedly in the onChildDraw method
            // background exposed as swipe is happening
            Drawable background;
            // the mark that shows up as swipe is happening
            Drawable xMark;
            // The margins needed for the xMark -- the width, essentially, of the xMark
            int xMarkMargin;
            // whether or not the eventRow has been initiated with this callback (I think...)
            boolean initiated;

            // set eventRow as initialized
            private void init() {
                initiated = true;
            }

            // Method to choose which mark to use depending on swipe left or right
            // swipe left dX < 0; swipe right dX > 0
            private Drawable getxMark(float dX) {
                if (dX < 0) {
                    xMark = ContextCompat.getDrawable(thisActivity, R.drawable.ic_clear_24dp);
                } else {
                    xMark = ContextCompat.getDrawable(thisActivity, R.drawable.ic_add_circle_outline_black_24dp);
                }
                return xMark;
            }

            // Method to draw the background exposed as swipe is occuring
            // swipe left dX < 0; swipe right dX > 0
            private void drawBackground(float dX, View itemView, Canvas c) {
                if (dX < 0) {
                    // give a red background and place the xMark to the right if swipe left
                    background = new ColorDrawable(materialRed);
                    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                } else {
                    // give a green background and place the xMark to the left if swipe right
                    background = new ColorDrawable(materialGreen);
                    background.setBounds(itemView.getLeft() + (int) dX, itemView.getTop(), itemView.getLeft(), itemView.getBottom());
                }
                // draw the background on the canvas
                background.draw(c);
            }


            // this is one method that handles both defining the xMark and drawing the background
            // this method uses the two methods above to handle most of the work
            private void handleBackgroundForSwipe(float dX, View itemView, Canvas c) {
                // Method to draw the background exposed as swipe is occuring
                drawBackground(dX, itemView, c);

                // Method to choose which mark to use depending on swipe left or right
                xMark = getxMark(dX);
                // The margins needed for the xMark
                // Defined as 16dp currently in res -> values -> dimens
                // Retrieve a dimensional for a particular resource ID.
                // https://developer.android.com/reference/android/content/res/Resources.html#getDimension(int)
                xMarkMargin = (int) thisActivity.getResources().getDimension(R.dimen.ic_clear_margin);
                // Display the xMark in white
                // PorterDuff explains how to handle overlapping images, in this case:
                // Discards the source pixels that do not cover destination pixels.
                // https://developer.android.com/reference/android/graphics/PorterDuff.Mode.html
                xMark.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

                // draw x mark
                // the height of the eventRow
                int itemHeight = itemView.getBottom() - itemView.getTop();
                // width of the xMark
                int intrinsicWidth = xMark.getIntrinsicWidth();
                // height of the xMark
                int intrinsicHeight = xMark.getIntrinsicWidth();
                // margin to the left of the xMark
                int xMarkLeft;
                // margin to the right of the xMark
                int xMarkRight;
                if (dX < 0) {
                    // distance from the left of the eventRow
                    // start from the right side of the eventRow
                    // subtract the dimensional we set in res -> values -> dimens as a margin
                    // subtract the width of the xMark
                    xMarkLeft = itemView.getRight() - xMarkMargin - intrinsicWidth;
                    // distance from the right of the eventRow
                    // start from the right side of the eventRow
                    // subtract the dimensional we set in res -> values -> dimens as a margin
                    xMarkRight = itemView.getRight() - xMarkMargin;
                } else {
                    // distance from the right of the eventRow
                    // start from the left side of the eventRow
                    // add the dimensional we set in res -> values -> dimens as a margin
                    // add the width of the xMark
                    xMarkRight = itemView.getLeft() + xMarkMargin + intrinsicWidth;
                    // distance from the left of the eventRow
                    // start from the left side of the eventRow
                    // add the dimensional we set in res -> values -> dimens as a margin
                    xMarkLeft = itemView.getLeft() + xMarkMargin;
                }

                // distance from the top of the eventRow
                // start from the top of the eventRow
                // add the difference between the row height and the height of the mark divided by two
                // this will place the xMark equidistant from the top and bottom
                int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                // distance from the bottom of the eventRow
                // the distance from the top plus the height of the xMark
                int xMarkBottom = xMarkTop + intrinsicHeight;
                // place the xMark starting point
                xMark.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                // draw the mark
                xMark.draw(c);
            }


            // we aren't dragging and dropping so we'll set this to false
            // Called when ItemTouchHelper wants to move the dragged item from its old position to the new position.
            // https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.Callback.html#onMove(android.support.v7.widget.RecyclerView, android.support.v7.widget.RecyclerView.ViewHolder, android.support.v7.widget.RecyclerView.ViewHolder)
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }


            // Returns the swipe directions for the provided ViewHolder.
            // Default implementation returns the swipe directions that was set via constructor or setDefaultSwipeDirs(int).
            // recyclerView	RecyclerView: The RecyclerView to which the ItemTouchHelper is attached to.
            // viewHolder	RecyclerView.ViewHolder: The RecyclerView for which the swipe direction is queried.
            // returns : int	A binary OR of direction flags.
            // https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.html#DOWN
            // https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.SimpleCallback.html#getSwipeDirs(android.support.v7.widget.RecyclerView, android.support.v7.widget.RecyclerView.ViewHolder)
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                // Returns the Adapter position of the item represented by this ViewHolder.
                // gives us the position in the EventAdapter, for instance, of this viewHolder, which contains our event_row
                // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.ViewHolder.html#getAdapterPosition()
                int position = viewHolder.getAdapterPosition();
                // Returns the adapter currently in use in this ListView.
                // The returned adapter might not be the same adapter passed to setAdapter(ListAdapter) but might be a WrapperListAdapter.
                // https://developer.android.com/reference/android/widget/ListView.html#getAdapter()
                // Extended Adapter that is the bridge between a ListView and the data that backs the list.
                // Frequently that data comes from a Cursor, but that is not required.
                // The ListView can display any data provided that it is wrapped in a ListAdapter.
                // https://developer.android.com/reference/android/widget/ListAdapter.html
                EventAdapter eventAdapter = (EventAdapter) recyclerView.getAdapter();
                // returns a true false value to determine if the event is pending removal
                if (eventAdapter.isPendingRemoval(position)) {
                    // this is a binary indicating false --
                    // no direction given because this move is pending
                    return 0;
                }
                // LEFT = 4
                // RIGHT = 8
                // if the move is no longer pending, this will return the direction of the swipe
                return super.getSwipeDirs(recyclerView, viewHolder);
            }


            // This method is going to queue up the eventRow for removal -- waiting for undo action of the right amount of time
            // If you are returning relative directions (START , END) from the getMovementFlags(RecyclerView, ViewHolder) method,
            // this method will also use relative directions. Otherwise, it will use absolute directions.
            // Called when a ViewHolder is swiped by the user.
            // viewHolder	RecyclerView.ViewHolder: The ViewHolder which has been swiped by the user.
            // direction	int: The direction to which the ViewHolder is swiped.
            // https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.Callback.html#onSwiped(android.support.v7.widget.RecyclerView.ViewHolder, int)
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Returns the Adapter position of the item represented by this ViewHolder.
                // Note that this might be different than the getLayoutPosition() if there are pending adapter
                // updates but a new layout pass has not happened yet.
                // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.ViewHolder.html#getAdapterPosition()
                int swipedPosition = viewHolder.getAdapterPosition();
                // Returns the adapter currently in use in this ListView. The returned adapter might not be the same adapter
                // passed to setAdapter(ListAdapter) but might be a WrapperListAdapter.
                // https://developer.android.com/reference/android/widget/ListView.html#getAdapter()
                EventAdapter adapter = (EventAdapter) recyclerView.getAdapter();
                // method created for queueing up an eventRow for removal
                adapter.pendingRemoval(swipedPosition);
            }


            // Draws the green or red background and places the xMark
            // Initializes eventRow if it isn't initialized
            // Called by ItemTouchHelper on RecyclerView's onDraw callback.
            // If you would like to customize how your View's respond to user interactions, this is a good place to override.
            // c	Canvas: The canvas which RecyclerView is drawing its children
            // recyclerView	RecyclerView: The RecyclerView to which ItemTouchHelper is attached to
            // viewHolder	RecyclerView.ViewHolder: The ViewHolder which is being interacted by the User or it was interacted and simply animating to its original position
            // dX	float: The amount of horizontal displacement caused by user's action
            // dY	float: The amount of vertical displacement caused by user's action
            // actionState	int: The type of interaction on the View. Is either ACTION_STATE_DRAG or ACTION_STATE_SWIPE.
            // isCurrentlyActive	boolean: True if this view is currently being controlled by the user or false it is simply animating back to its original state.
            // https://developer.android.com/reference/android/support/v7/widget/helper/ItemTouchHelper.Callback.html#onChildDraw(android.graphics.Canvas, android.support.v7.widget.RecyclerView, android.support.v7.widget.RecyclerView.ViewHolder, float, float, int, boolean)
            @Override
            public void onChildDraw(
                    Canvas c,
                    RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    float dX,
                    float dY,
                    int actionState,
                    boolean isCurrentlyActive
            ) {

                // this is the eventRow
                // itemView is the Public Constructor for RecyclerView.ViewHolder
                // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.ViewHolder.html#itemView
                View itemView = viewHolder.itemView;

                // if this viewholder is not in the adapter (e.g., it has been remove), don't do anything
                // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.ViewHolder.html#getAdapterPosition()
                // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html#NO_POSITION
                // -1 corresponds to NO_POSITION
                if (viewHolder.getAdapterPosition() == -1) {
                    // not interested in those
                    return;
                }

                // if this eventRow has not been initiated, initiate it
                if (!initiated) {
                    init();
                }

                // this is one method that handles both defining the xMark and drawing the background
                handleBackgroundForSwipe(dX, itemView, c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

        };
        // create a new ItemTouchHelper using the simpleItemTouchCallback
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        // attach this new helper to the recyclerView
        mItemTouchHelper.attachToRecyclerView(recyclerView);

    }



    // Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
    // in this case each item will be an eventRow
    // parent	ViewGroup: The ViewGroup into which the new View will be added after it is bound to an adapter position.
    // viewType	int: The view type of the new View. -- The default implementation of this method returns 0,
    // making the assumption of a single view type for the adapter. Unlike ListView adapters, types need not be contiguous.
    // Consider using id resources to uniquely identify item view types.
    // you could also create a class method overriding getType()
    // https://stackoverflow.com/questions/34889264/recyclerview-add-emptyview/34988696#34988696
    // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#onCreateViewHolder(android.view.ViewGroup, int)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this is going to inflate a new eventRow
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_row, parent, false);
        return new MyViewHolder(itemView);
    }



    // Called by RecyclerView to display the data at the specified position.
    // This method should update the contents of the itemView to reflect the item at the given position.
    // holder	VH: The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
    // position	int: The position of the item within the adapter's data set.
    // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#onBindViewHolder(VH, int)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        // get the event from the given position in the eventList
        final CalendarEvent event = eventList.get(position);

        // define the secondaryDark color
        int secondaryDark = ContextCompat.getColor(context, R.color.secondaryDarkColor);

        // get the event_border_drawables
        Drawable scheduledBackground = ContextCompat.getDrawable(context, R.drawable.event_border_standard);
        Drawable placeholderBackground = ContextCompat.getDrawable(context, R.drawable.event_border_placeholder);

        // set the fullDescription from the eventRow by using the toString() method for an event
        holder.fullDescription.setText(event.toString());
        // get the dateAsString for this event
        String dateAsString = dateAsString(event.getBegin());
        // set the startDate for the eventRow
        holder.start.setText(dateAsString);


        // if this is a pending removal show the pending undo state
        // otherwise show the normal state of the event row
        if (eventsPendingRemoval.contains(event)) {
            // set the eventRow background color to secondary when it is pending removal
            holder.itemView.setBackgroundColor(secondaryDark);
            // remove the description when pending removal
            holder.fullDescription.setVisibility(View.GONE);
            // set the font color to white
            holder.start.setTextColor(Color.WHITE);
            // show the undo button
            holder.undoButton.setVisibility(View.VISIBLE);
            // set a click listener for the undo button
            // Register a callback to be invoked when this view is clicked. If this view is not clickable, it becomes clickable.
            // l	View.OnClickListener: The callback that will run
            // This value may be null.
            // https://developer.android.com/reference/android/view/View.html#setOnClickListener(android.view.View.OnClickListener)
            holder.undoButton.setOnClickListener(buttonClickListener(event));
        } else {
            // set the eventRow background color
            // if the event is a placeholder, show a different color than an event that is already scheduled
            if (event.isPlaceholder()) {
                // set the background color to placeholderBackground
                holder.itemView.setBackground(placeholderBackground);
            } else {
                // set the background color to scheduledBackground
                holder.itemView.setBackground(scheduledBackground);
            }
            // we need to show the "normal" state
            holder.fullDescription.setVisibility(View.VISIBLE);
            holder.fullDescription.setText(event.toString());
            holder.undoButton.setVisibility(View.GONE);
            holder.undoButton.setOnClickListener(null);

        }
        if (event.isHidden()) {
            holder.currentView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

    }



    private View.OnClickListener buttonClickListener(final CalendarEvent event) {
        View.OnClickListener listener;
        listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user wants to undo the removal, let's cancel the pending task
                Runnable pendingRemovalRunnable = pendingRunnables.get(event);
                pendingRunnables.remove(event);
                if (pendingRemovalRunnable != null) handler.removeCallbacks(pendingRemovalRunnable);
                eventsPendingRemoval.remove(event);
                // this will rebind the row in "normal" state
                notifyItemChanged(eventList.indexOf(event));
            }
        };
        return listener;
    }




    @Override
    public int getItemCount() {
        return eventList.size();
    }



    // method created for queueing up an eventRow for removal
    private void pendingRemoval(int position) {
        // get the calendar event pending removal
        final CalendarEvent event = eventList.get(position);
        // if it is not already in the eventsPendingRemoval list, add it
        if (!eventsPendingRemoval.contains(event)) {
            eventsPendingRemoval.add(event);
        }

        // this will redraw row in "undo" state
        // Notify any registered observers (e.g., our holder via onBindViewHolder, I think) that the item at position has changed
        // This is an item change event, not a structural change event.
        // It indicates that any reflection of the data at position is out of date and should be updated.
        // The item at position retains the same identity.
        // There are two different classes of data change events, item changes and structural changes.
        // Item changes are when a single item has its data updated but no positional changes have occurred.
        // Structural changes are when items are inserted, removed or moved within the data set.
        // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#notifyItemChanged(int)
        notifyItemChanged(position);

        // create a runable that will remove the event from the eventList
        Runnable pendingRemovalRunnable = new Runnable() {
            @Override
            public void run() {
                remove(eventList.indexOf(event));
            }
        };

        // queue this runnable to take place PENDING_REMOVAL_TIMEOUT milliseconds from now (unless cancelled)
        // https://developer.android.com/reference/android/os/Handler.html#postDelayed(java.lang.Runnable, long)
        handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);

        // register this runnable and event with pendingRunnables
        pendingRunnables.put(event, pendingRemovalRunnable);
    }



    // method to remove a calendar event from the event list
    private void remove(int position) {
        // pick the event we're looking to remove
        CalendarEvent event = eventList.get(position);
        // if this event is on the eventsPendingRemoval list, remove it (as we are about to remove the event)
        if (eventsPendingRemoval.contains(event)) {
            eventsPendingRemoval.remove(event);
        }
        // if the event is still contained in eventList, remove it from the list
        if (eventList.contains(event)) {
            eventList.remove(position);
            // Notify any registered observers that the item previously located at position has been removed from the data set.
            // The items previously located at and after position may now be found at oldPosition - 1.
            // https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html#notifyItemRemoved(int)
            notifyItemRemoved(position);
        }
    }

    // return a true false value to determine if the event is pending removal
    private boolean isPendingRemoval(int position) {
        // retrieve the calendar event from the list
        CalendarEvent event = eventList.get(position);
        // return a boolean indicating if the eventsPendingRemoval list contains the event
        return eventsPendingRemoval.contains(event);
    }


}