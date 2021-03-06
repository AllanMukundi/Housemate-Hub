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
import com.cs446.housematehub.common.Utils;
import com.cs446.housematehub.account.AccountDetails;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.cs446.housematehub.LoggedInBaseActivity.objectToJSONArray;

public class ExpenseLog extends Fragment {

    private static String userMe;
    private String userOther;
    private Object userOtherColor;
    private String houseName;
    private HashMap<String, List<Expense>> expenseRecord = new HashMap<>();
    private TextView loggedInVerb;
    private CurrencyEditText loggedInAmount;
    private ContainerAdapter containerAdapter = null;

    public ExpenseLog(String userOther, Object userOtherColor, String userMe, String houseName) {
        this.userOther = userOther;
        this.userOtherColor = userOtherColor;
        ExpenseLog.userMe = userMe;
        this.houseName = houseName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense_log, container, false);

        TextView userFromText = view.findViewById(R.id.expense_from);
        userFromText.setText(userOther);

        ImageView circle = view.findViewById(R.id.user_from_circle);
        GradientDrawable drawable = (GradientDrawable) circle.getDrawable();
        drawable.setColor((int) userOtherColor);

        loggedInVerb = view.findViewById(R.id.log_expense_verb);
        loggedInAmount = view.findViewById(R.id.user_from_amount);

        updateExpenseRecord();

        ListView listView = view.findViewById(R.id.expense_container_list);
        containerAdapter = new ContainerAdapter(getContext(), new ArrayList<>(expenseRecord.keySet()), expenseRecord, this);
        listView.setAdapter(containerAdapter);

        ((HouseMainActivity) getActivity()).enableBack();

        View.OnClickListener userDetailsListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HouseMainActivity) getActivity()).changeFragments(HouseMainActivity.TAB_EXPENSE, AccountDetails.newInstance(userOther), true, true);
            }
        };
        userFromText.setOnClickListener(userDetailsListener);
        circle.setOnClickListener(userDetailsListener);

        return view;
    }

    public void updateExpenseRecord() {
        List<ParseObject> expenseRes = new ArrayList<ParseObject>();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Expense");
        query.whereEqualTo("houseName", houseName);

        try {
            expenseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        long netTotal = 0;

        expenseRecord = new HashMap<>();

        for (ParseObject expense : expenseRes) {
            int id = (int) expense.get("expenseId");
            String title = (String) expense.get("expenseTitle");
            long total = Long.valueOf((int) expense.get("expenseAmount"));
            String date = (String) expense.get("expenseDate");

            Gson gson = new Gson();
            JSONArray ja = objectToJSONArray(expense.get("division"));
            Expense.ExpenseDivision[] divisions = gson.fromJson(ja.toString(), Expense.ExpenseDivision[].class);
            for (Expense.ExpenseDivision division : divisions) {
                if (division.to.equals(userMe) && division.from.equals(userOther)) {
                    List<Expense> tempExpenses = expenseRecord.get(date);
                    if (tempExpenses == null) {
                        tempExpenses = new ArrayList<Expense>();
                    }

                    ArrayList<Expense.ExpenseDivision> singleton = new ArrayList<>();
                    singleton.add(division);
                    Expense currExpense = new Expense(id, title, total, date, singleton);

                    tempExpenses.add(currExpense);
                    expenseRecord.put(date, tempExpenses);
                    netTotal += division.amount;
                } else if (division.to.equals(userOther) && division.from.equals(userMe)) {
                    List<Expense> tempExpenses = expenseRecord.get(date);
                    if (tempExpenses == null) {
                        tempExpenses = new ArrayList<Expense>();
                    }

                    ArrayList<Expense.ExpenseDivision> singleton = new ArrayList<>();
                    singleton.add(division);
                    Expense currExpense = new Expense(id, title, total, date, singleton);

                    tempExpenses.add(currExpense);
                    expenseRecord.put(date, tempExpenses);
                    netTotal -= division.amount;
                }
            }
        }

        loggedInAmount.setValue(Math.abs(netTotal));
        if (netTotal < 0) {
            loggedInAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDebtRed));
            loggedInVerb.setText(" is owed");
        } else if (netTotal > 0) {
            loggedInAmount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOwedGreen));
            loggedInVerb.setText(" owes you");
        } else {
            loggedInAmount.setTextColor(Color.BLACK);
        }

        if (containerAdapter != null) {
            containerAdapter.setExpenseRecord(expenseRecord);
            containerAdapter.notifyDataSetChanged();
        }
    }

    public void deleteExpense(Expense expense, Expense.ExpenseDivision division) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Expense");
        String houseName = (String) ((HouseMainActivity) getActivity()).getCurrentHouse().get("houseName");

        query.whereEqualTo("houseName", houseName);
        query.whereEqualTo("expenseId", expense.id);
        List<ParseObject> expenseRes = new ArrayList<>();

        try {
            expenseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (expenseRes.size() == 0) {
            throw new RuntimeException("Expense with the given id was not found");
        }

        ParseObject editExpense = expenseRes.get(0); // should only be one matching row

        Gson gson = new Gson();

        JSONArray ja = objectToJSONArray(editExpense.get("division"));
        Expense.ExpenseDivision[] divisions = gson.fromJson(ja.toString(), Expense.ExpenseDivision[].class);

        List<Expense.ExpenseDivision> fullDivision = new ArrayList<>(Arrays.asList(divisions));


        if (fullDivision.size() == 1) { // can delete whole expense entirely
            try {
                editExpense.delete();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else { // only delete one division
            for (int i = 0; i < fullDivision.size(); ++i) {
                Expense.ExpenseDivision temp = fullDivision.get(i);
                if (temp.amount == division.amount && temp.from.equals(division.from) && temp.to.equals(division.to)) {
                    fullDivision.remove(temp);
                }
            }

            JsonElement element = gson.toJsonTree(fullDivision, new TypeToken<List<Expense.ExpenseDivision>>() {
            }.getType());
            try {
                final JSONArray jsonArray = new JSONArray(element.getAsJsonArray().toString());
                editExpense.put("expenseAmount", expense.total - division.amount);
                editExpense.put("division", jsonArray);
                try {
                    editExpense.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        updateExpenseRecord();
    }


    public static class ContainerAdapter extends ArrayAdapter<String> {

        ExpenseLog parent;
        private Context mContext;
        private List<String> dates;
        private HashMap<String, List<Expense>> expenseRecord;

        public ContainerAdapter(@NonNull Context context, ArrayList<String> dates, HashMap<String, List<Expense>> expenseRecord, ExpenseLog parent) {
            super(context, R.layout.expense_log_item_container, dates);
            this.mContext = context;
            this.dates = dates;
            this.expenseRecord = expenseRecord;
            this.parent = parent;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View container = convertView;

            if (container == null) {
                container = LayoutInflater.from(this.mContext).inflate(R.layout.expense_log_item_container, parent, false);
            }

            String date = dates.get(position);
            List<Expense> expenses = expenseRecord.get(date);

            if (expenses != null) {
                System.out.println("test");
                container.setVisibility(View.VISIBLE);
                TextView dateText = container.findViewById(R.id.expense_date);
                ListView listView = container.findViewById(R.id.expense_list);

                dateText.setText(date);

                ListItemAdapter listItemAdapter = new ListItemAdapter(getContext(), new ArrayList<Expense>(expenses), this.parent);
                listView.setAdapter(listItemAdapter);
                Utils.updateListViewHeight(listView);
            } else {
                return new View(getContext());
            }

            return container;
        }

        public void setExpenseRecord(HashMap<String, List<Expense>> expenseRecord) {
            this.expenseRecord.clear();
            this.expenseRecord.putAll(expenseRecord);
        }
    }

    public static class ListItemAdapter extends ArrayAdapter<Expense> {

        ExpenseLog parent;
        private Context mContext;
        private List<Expense> expenses;

        public ListItemAdapter(@NonNull Context context, ArrayList<Expense> expenses, ExpenseLog parent) {
            super(context, R.layout.expense_log_item_row, expenses);
            this.mContext = context;
            this.expenses = expenses;
            this.parent = parent;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View expenseRow = convertView;

            if (expenseRow == null) {
                expenseRow = LayoutInflater.from(this.mContext).inflate(R.layout.expense_log_item_row, parent, false);
            }

            final Expense expense = expenses.get(position);

            TextView textView = expenseRow.findViewById(R.id.expense_item_name);
            CurrencyEditText amount = expenseRow.findViewById(R.id.expense_item_amount);
            ImageView edit = expenseRow.findViewById(R.id.expense_item_edit);
            ImageView delete = expenseRow.findViewById(R.id.expense_item_delete);

            textView.setText(expense.title);

            final Expense.ExpenseDivision division = expense.division.get(0);

            if (division.from.equals(userMe)) {
                amount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDebtRed));
            } else {
                amount.setTextColor(ContextCompat.getColor(getContext(), R.color.colorOwedGreen));
            }

            amount.setValue(division.amount);

            final ExpenseLog parentFinal = this.parent;

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentFinal.deleteExpense(expense, division);
                }
            });

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("isEdit", true);
                    bundle.putLong("expenseId", expense.id);
                    ExpenseDialog dialog = new ExpenseDialog();
                    dialog.setArguments(bundle);

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            parentFinal.updateExpenseRecord();
                        }
                    });
                    dialog.show(parentFinal.getChildFragmentManager(), "ExpenseDialog");
                }
            });

            return expenseRow;
        }
    }
}
