package com.cs446.housematehub.expenses;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs446.housematehub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExpenseManager extends Fragment {

    private View view;
    private FloatingActionButton addExpenseButton;

    public ExpenseManager() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_expense_manager, container, false);
        addExpenseButton = view.findViewById(R.id.addButton);

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpenseDialog dialog = new ExpenseDialog();
                dialog.show(getFragmentManager(), "ExpenseDialog");
            }
        });
        return view;
    }
}
