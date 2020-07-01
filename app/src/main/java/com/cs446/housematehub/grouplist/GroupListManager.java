package com.cs446.housematehub.grouplist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs446.housematehub.R;
import com.cs446.housematehub.expenses.ExpenseDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;

public class GroupListManager extends Fragment {

    private View view;
    private FloatingActionButton addGroupListButton;

    public GroupListManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group_list_manager, container, false);
        addGroupListButton = view.findViewById(R.id.addButton);

        addGroupListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupListDialog dialog = new GroupListDialog();
                dialog.show(getFragmentManager(), "GroupListDialog");
            }
        });
        return view;
    }
}
