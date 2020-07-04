package com.cs446.housematehub.expenses;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cs446.housematehub.LoggedInBaseActivity.objectToJSONArray;

public class ExpenseManager extends Fragment {

    private ParseUser currentUser = ParseUser.getCurrentUser();
    private String currentUserName = currentUser.getUsername();
    private HashMap<String, Long> expenseRecord = new HashMap<>();
    private UserAdapter userAdapter = null;
    private TextView loggedInVerb;
    private CurrencyEditText loggedInAmount;

    public ExpenseManager() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_manager, container, false);
        FloatingActionButton addExpenseButton = view.findViewById(R.id.addButton);

        addExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExpenseDialog dialog = new ExpenseDialog();
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        updateExpenseRecord();
                        if (userAdapter != null) {
                            userAdapter.notifyDataSetChanged();
                        }
                    }
                });
                dialog.show(getChildFragmentManager(), "ExpenseDialog");
            }
        });

        ListView listView = view.findViewById(R.id.overview_user_list);

        ImageView circle = view.findViewById(R.id.logged_in_user_circle);
        GradientDrawable drawable = (GradientDrawable) circle.getDrawable();
        drawable.setColor((int) currentUser.get("color"));

        loggedInVerb = view.findViewById(R.id.expense_verb);
        loggedInAmount = view.findViewById(R.id.logged_in_user_amount);

        List<ParseObject> users = ((HouseMainActivity) getActivity()).getHouseUsers(false);

        for (ParseObject user : users) {
            String username = user.get("username").toString();
            expenseRecord.put(username, (long) 0);
        }

        updateExpenseRecord();

        userAdapter = new UserAdapter(getContext(), new ArrayList<>(users), expenseRecord);
        listView.setAdapter(userAdapter);

        return view;
    }

    public void updateExpenseRecord() {
        List<ParseObject> expenseRes = new ArrayList<ParseObject>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Expense");
        final String houseName = (String) ((HouseMainActivity) getActivity()).getCurrentHouse().get("houseName");
        query.whereEqualTo("houseName", houseName);

        try {
            expenseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long total = 0;

        for (ParseObject expense : expenseRes) {
            Gson gson = new Gson();
            JSONArray ja = objectToJSONArray(expense.get("division"));
            Expense.ExpenseDivision[] divisions = gson.fromJson(ja.toString(), Expense.ExpenseDivision[].class);
            for (Expense.ExpenseDivision division : divisions) {
                if (division.to.equals(currentUserName)) {
                    long old_amount = expenseRecord.get(division.from);
                    expenseRecord.put(division.from, old_amount + division.amount);
                    total += division.amount;
                } else if (division.from.equals(currentUserName)) {
                    long old_amount = expenseRecord.get(division.to);
                    expenseRecord.put(division.to, old_amount - division.amount);
                    total -= division.amount;
                }
            }
        }

        loggedInAmount.setValue(Math.abs(total));
        if (total < 0) {
            loggedInAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDebtRed));
            loggedInVerb.setText("You owe");
        } else if (total > 0) {
            loggedInAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOwedGreen));
            loggedInVerb.setText("You are owed");
        } else {
            loggedInAmount.setTextColor(Color.BLACK);
        }
    }

    public static class UserAdapter extends ArrayAdapter<ParseObject> {

        private Context mContext;
        private List<ParseObject> users;

        private HashMap<String, Long> expenseRecord;

        public UserAdapter(@NonNull Context context, ArrayList<ParseObject> users, HashMap<String, Long> expenseRecord) {
            super(context, R.layout.expense_manager_user_row, users);
            this.mContext = context;
            this.users = users;
            this.expenseRecord = expenseRecord;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View user_row = convertView;
            if (user_row == null) {
                user_row = LayoutInflater.from(this.mContext).inflate(R.layout.expense_manager_overview_user_row, parent, false);
            }

            ParseObject currUser = users.get(position);

            ImageView circle = user_row.findViewById(R.id.overview_user_row_circle);
            TextView name = user_row.findViewById(R.id.overview_user_row_name);
            CurrencyEditText amount = user_row.findViewById(R.id.overview_user_row_amount);
            ImageView see_more = user_row.findViewById(R.id.overview_user_see_more_image);

            String username = currUser.get("username").toString();

            name.setText(username);

            GradientDrawable drawable = (GradientDrawable) circle.getDrawable();
            drawable.setColor((int) currUser.get("color"));

            if (expenseRecord.containsKey(username)) {
                long amount_val = expenseRecord.get(username);
                amount.setValue(Math.abs(amount_val));
                if (amount_val < 0) {
                    amount.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorDebtRed));
                } else if (amount_val > 0) {
                    amount.setTextColor(ContextCompat.getColor(this.mContext, R.color.colorOwedGreen));
                } else {
                    see_more.setVisibility(View.GONE);
                }
            }

            return user_row;
        }
    }
}
