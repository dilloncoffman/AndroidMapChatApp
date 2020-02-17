package edu.temple.mapchatapp;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserRecyclerViewFragment.OnUserSelectedInterface} interface
 * to handle interaction events.
 * Use the {@link UserRecyclerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserRecyclerViewFragment extends Fragment {
    private ArrayList<User> mUsers;
    private final static String USERS_KEY = "users";
    private final static String DOUBLE_PANE_KEY = "doublePane";

    private static final String TAG = "UserRecyclerViewFragment";
    private boolean mDoublePane;


    private OnUserSelectedInterface mListener;

    public UserRecyclerViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param users ArrayList<User>.
     * @param doublePane boolean.
     * @return A new instance of fragment UserRecyclerViewFragment.
     */
    public static UserRecyclerViewFragment newInstance(ArrayList<User> users, boolean doublePane) {
        UserRecyclerViewFragment userRecyclerViewFragment = new UserRecyclerViewFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(USERS_KEY, users);
        args.putBoolean(DOUBLE_PANE_KEY, doublePane);
        userRecyclerViewFragment.setArguments(args);
        return userRecyclerViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mUsers = args.getParcelableArrayList(USERS_KEY);
            mDoublePane = args.getBoolean(DOUBLE_PANE_KEY);
        }
        Log.d(TAG, "onCreate: mDoublePane in Fragment is: " + mDoublePane);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_recycler_view, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.userNameRecyclerView);
        UserRecyclerViewAdapter userRecyclerViewAdapter = new UserRecyclerViewAdapter((MainActivity) getActivity(), getContext(), mUsers, mDoublePane);
        recyclerView.setAdapter(userRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUserSelectedInterface) {
            mListener = (OnUserSelectedInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUserSelectedInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnUserSelectedInterface {
        void userSelected(int position);
    }

    public ArrayList<User> getUsers() {
        return this.mUsers;
    }
}
