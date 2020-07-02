package com.cs446.housematehub.grouplist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupListManager extends Fragment implements GroupListAdapter.GroupListAdapterInterface {

    private View view;
    private FloatingActionButton addGroupListButton;
    private List<ParseObject> groupListResult, listData;

    private RecyclerView listRecyclerView;
    private RecyclerView.Adapter listAdapter;
    private RecyclerView.LayoutManager listLayoutManager;


    public GroupListManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGroupList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group_list_manager, container, false);
        addGroupListButton = view.findViewById(R.id.addButton);

        listRecyclerView = (RecyclerView) view.findViewById(R.id.listRecyclerView);
        listRecyclerView.setHasFixedSize(true);

        listLayoutManager = new LinearLayoutManager(getContext());
        listRecyclerView.setLayoutManager(listLayoutManager);
        listRecyclerView.addItemDecoration(new DividerItemDecoration(listRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        listData = groupListResult;
        listAdapter = new GroupListAdapter(listData, this);
        listRecyclerView.setAdapter(listAdapter);

        addGroupListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final GroupListDialog dialog = new GroupListDialog();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshList();
                    }
                });
                dialog.show(getChildFragmentManager(), "GroupListDialog");
            }
        });
        return view;
    }

    public void refreshList() {
        listData.clear();
        getGroupList();
        listData.addAll(groupListResult);
        try {
            TimeUnit.MICROSECONDS.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    public void getGroupList() {
        final String houseName = ((HouseMainActivity) getActivity()).houseName;

        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");
        query.whereEqualTo("houseName", houseName);

        try {
            groupListResult = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
