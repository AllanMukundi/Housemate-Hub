package com.cs446.housematehub.account;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.HousematesListAdapter;
import com.cs446.housematehub.LoggedInBaseActivity;
import com.cs446.housematehub.R;
import com.cs446.housematehub.common.RecyclerItemClickListener;
import com.cs446.housematehub.common.ViewUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class AccountDetails extends Fragment {

    private ParseUser mUser;
    private ParseObject mHouse;
    private HousematesListAdapter mAdapter;

    private ProgressBar mProgressSpinner;
    private Switch mDoNotDisturbSwitch;
    private Button mArrowButton;
    private ImageView mCircle;
    private RelativeLayout mHousematesContainer;
    private RecyclerView mHousematesRecyclerView;
    private TextView mNameTitleTextView;
    private TextView mNameRowTextView;
    private TextView mEmailTextView;
    private TextView mUserNameTextView;
    private TextView mGroupNameTextView;
    private TextView mGroupCodeTextView;
    private TextView mLogOutTextView;

    public static AccountDetails newInstance(String userName) {
        AccountDetails accountDetailsFragment = new AccountDetails();
        Bundle args = new Bundle();
        args.putString("username", userName);
        accountDetailsFragment.setArguments(args);
        return accountDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentVisibility(false);
        String userName = getArguments().getString("username");
        updateData(userName);
        initialiseViews(view);
        updateUI();
    }

    private void updateUI() {
        mUserNameTextView.setText(mUser.getUsername());
        mNameTitleTextView.setText(mUser.getUsername());
        mNameRowTextView.setText(mUser.getUsername());
        mEmailTextView.setText(mUser.getEmail());
        mGroupNameTextView.setText((String) mHouse.get("houseName"));
        mGroupCodeTextView.setText((String) mHouse.get("houseCode"));
        ((GradientDrawable) mCircle.getDrawable()).setColor((int) mUser.get("color"));
        setContentVisibility(true);
        mHousematesRecyclerView.setVisibility(View.GONE);

        if (!isCurrentUser()) {
            mLogOutTextView.setVisibility(View.GONE);
            mDoNotDisturbSwitch.setEnabled(false);
        }
    }

    private void updateData(String userName) {
        mHouse = ((HouseMainActivity) getActivity()).getCurrentHouse();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", userName);
        try {
            List<ParseUser> response = query.find();
            mUser = response.get(0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<ParseObject> allHouseUsers = ((HouseMainActivity) getActivity()).getHouseUsers(true);
        List<ParseUser> housemates = new ArrayList<>();

        for (ParseObject o : allHouseUsers) {
            ParseUser user = (ParseUser) o;
            if (!user.getUsername().equals(mUser.getUsername())) {
                housemates.add(user);
            }
        }
        mAdapter = new HousematesListAdapter(housemates, getContext());
    }

    private void initialiseViews(View rootView) {
        mDoNotDisturbSwitch = rootView.findViewById(R.id.do_not_disturb_switch);
        mNameTitleTextView = rootView.findViewById(R.id.name_title);
        mNameRowTextView = rootView.findViewById(R.id.name_row);
        mEmailTextView = rootView.findViewById(R.id.email);
        mUserNameTextView = rootView.findViewById(R.id.username);
        mGroupNameTextView = rootView.findViewById(R.id.group_name);
        mGroupCodeTextView = rootView.findViewById(R.id.group_code);
        mProgressSpinner = rootView.findViewById(R.id.progress_spinner);
        mCircle = rootView.findViewById(R.id.user_circle);
        mHousematesContainer = rootView.findViewById(R.id.housemates_container);
        mLogOutTextView = rootView.findViewById(R.id.log_out);

        mHousematesRecyclerView = rootView.findViewById(R.id.housemates_list_recyclerview);
        mHousematesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mHousematesRecyclerView.getContext(),
                ((LinearLayoutManager) mHousematesRecyclerView.getLayoutManager()).getOrientation());
        mHousematesRecyclerView.addItemDecoration(dividerItemDecoration);
        mHousematesRecyclerView.setAdapter(mAdapter);
        ViewGroup.LayoutParams params = mHousematesRecyclerView.getLayoutParams();
        params.height = 225 * mAdapter.getItemCount();
        mHousematesRecyclerView.setLayoutParams(params);
        mHousematesRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), mHousematesRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ParseUser user = mAdapter.getData().get(position);
                        Fragment newAccountDetailsFragment = AccountDetails.newInstance(user.getUsername());
                        ((HouseMainActivity) getActivity()).loadFragment(newAccountDetailsFragment, "account_fragment_tag_" + user.getUsername(), false);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        mArrowButton = rootView.findViewById(R.id.arrow_button);
        View.OnClickListener expandHousematesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHousematesRecyclerView.getVisibility() == View.GONE) {
                    TransitionManager.beginDelayedTransition(mHousematesContainer, new AutoTransition());
                    mHousematesRecyclerView.setVisibility(View.VISIBLE);
                    mArrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                } else {
                    TransitionManager.beginDelayedTransition(mHousematesContainer, new AutoTransition());
                    mHousematesRecyclerView.setVisibility(View.GONE);
                    mArrowButton.setBackgroundResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                }
            }
        };
        mHousematesContainer.setOnClickListener(expandHousematesClickListener);
        mArrowButton.setOnClickListener(expandHousematesClickListener);

        mLogOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LoggedInBaseActivity) getActivity()).logOut();
            }
        });
    }

    private boolean isCurrentUser() {
        return mUser.getUsername().equals(ParseUser.getCurrentUser().getUsername());
    }

    private void setContentVisibility(boolean show) {
        ViewUtils.setViewsVisibility(getView(), show);
        if (mProgressSpinner != null) {
            mProgressSpinner.setVisibility(show ? View.GONE : View.VISIBLE);
            mProgressSpinner.setEnabled(!show);
        }
    }

}
