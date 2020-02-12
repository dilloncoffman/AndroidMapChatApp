package edu.temple.mapchatapp;

import android.content.Context;
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

    private static final String TAG = "UserRecyclerViewAdapter"; // TAG for debugging

    private ArrayList<String> mUserNames;
    private Context mContext;

    public UserRecyclerViewAdapter(Context context, ArrayList<String> userNames) {
        this.mContext = context;
        this.mUserNames = userNames;
    }

    /*
        Responsible for inflating the view
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /*
        Called every time a new item is added to the recycler view list
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        // Set user names
        holder.userName.setText(mUserNames.get(position));
        // Set click listener for each item in user recycler view
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on: " + mUserNames.get(position));
                Toast.makeText(mContext, mUserNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUserNames.size();
    }

    // Holds widgets in memory for each recycler view entry
    public class ViewHolder extends RecyclerView.ViewHolder {

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
