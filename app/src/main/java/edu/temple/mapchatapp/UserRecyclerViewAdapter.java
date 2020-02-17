package edu.temple.mapchatapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
/**
 * Created by dilloncoffman on 2020-02-10
 */
public class UserRecyclerViewAdapter extends RecyclerView.Adapter<UserRecyclerViewAdapter.ViewHolder> {

    private MainActivity mParentActivity;
    private Context mContext;
    private ArrayList<User> mUsers;
    private boolean mDoublePane;
    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };
    private static final String TAG = "UserRecyclerViewAdapter"; // TAG for debugging

    public UserRecyclerViewAdapter(MainActivity parent, Context context, ArrayList<User> users, boolean doublePane) {
        this.mParentActivity = parent;
        this.mContext = context;
        this.mUsers = users;
        this.mDoublePane = doublePane;
    }

    /*
        Responsible for inflating the view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.user_list_item, parent, false);
        return new ViewHolder(view);
    }

    /*
        Called every time a new item is added to the recycler view list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        // Set user names
        holder.userName.setText(mUsers.get(position).getName());
        holder.itemView.setTag(mUsers.get(position));
        // Set click listener for each item in user recycler view
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User userClicked = (User) view.getTag();
                if (mDoublePane) {
                    Bundle arguments = new Bundle();
                    arguments.putParcelable(MapFragment.ARG_USER, userClicked);
                    MapFragment mapFragment = new MapFragment();
                    mapFragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_map_container, MapFragment.newInstance(userClicked, mUsers, mDoublePane))
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra(MapFragment.ARG_USER, userClicked);
                    intent.putExtra("users", mUsers);
                    intent.putExtra("doublePane", mDoublePane);

                    context.startActivity(intent);
                }
                Toast.makeText(mContext, mUsers.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    // Holds widgets in memory for each recycler view entry
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        RelativeLayout parentLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Attach widgets to their IDs
            userName = itemView.findViewById(R.id.userName);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }
    }
}
