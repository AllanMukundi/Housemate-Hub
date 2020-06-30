package com.cs446.housematehub.expenses;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {

    List<ParseObject> users;
    private Spinner spinner;
    private EditText expenseTitle;
    private CurrencyEditText expenseAmount;
    private ListView listView;
    private String payer;
    private HashMap<String, CurrencyEditText> expenseRecord = new HashMap<String, CurrencyEditText>();
    private long[]  expenseRecordInit; // hacky workaround

    public ExpenseDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_dialog, container, false);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button createButton = view.findViewById(R.id.create_button);
        Button splitButton = view.findViewById(R.id.split_evenly);

        expenseTitle = view.findViewById(R.id.expense_title);
        expenseAmount = view.findViewById(R.id.expense_amount);

        listView = view.findViewById(R.id.user_list);
        users = ((HouseMainActivity) getActivity()).getHouseUsers();
        UserAdapter userAdapter = new UserAdapter(getContext(), new ArrayList<ParseObject>(users), expenseRecord);
        listView.setAdapter(userAdapter);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expenseTitle.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), "Expense must have a title", Toast.LENGTH_SHORT).show();
                } else if (expenseAmount.getRawValue() == 0) {
                    Toast.makeText(getActivity(), "Expense must be greater than $0", Toast.LENGTH_SHORT).show();
                } else if (!equalsTotal(expenseAmount.getRawValue())) {
                    Toast.makeText(getActivity(), "Division must equal the total amount", Toast.LENGTH_SHORT).show();
                } else {
                    commitExpense();
                }
            }
        });

        splitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long amounts[] = new long[users.size()];
                expenseRecordInit = new long[users.size()];

                long roundedDownAmount = expenseAmount.getRawValue() / users.size();
                for (int i = 0; i < amounts.length; ++i) {
                    amounts[i] = roundedDownAmount;
                    expenseRecordInit[i] = roundedDownAmount;
                }

                long centsDifference = expenseAmount.getRawValue() - (roundedDownAmount * users.size());
                int start = 0;
                while (centsDifference > 0) {
                    amounts[start] += 1;
                    expenseRecordInit[start] += 1;
                    start += 1;
                    --centsDifference;
                }
                start = 0;
                for (CurrencyEditText c : expenseRecord.values()) {
                    c.setValue(amounts[start++]);
                }
            }
        });

        initSpinner(view);

        return view;
    }

    public void initSpinner(View view) {
        spinner = view.findViewById(R.id.user_spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> user_names = new ArrayList<String>();

        for (ParseObject user : users) {
            user_names.add(user.get("username").toString());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, user_names);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        payer = users.get(position).get("username").toString();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
    }

    public boolean equalsTotal(long expenseAmount) {
        if (expenseRecord.isEmpty()) {
            return false;
        }
        long total = 0;
        for (CurrencyEditText c : expenseRecord.values()) {
            total += c.getRawValue();
        }
        return total == expenseAmount;
    }

    public void commitExpense() {
        List<ParseObject> houseRes = new ArrayList<ParseObject>();
        ParseObject house;
        ParseQuery<ParseObject> query = ParseQuery.getQuery("House");
        final String houseName = ((HouseMainActivity) getActivity()).houseName;
        query.whereEqualTo("houseName", houseName);

        try {
            houseRes = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (houseRes.size() == 0) {
            throw new RuntimeException("House with the given houseName was not found");
        }

        house = houseRes.get(0); // should only be one matching row
        final int id = (int) house.get("nextExpenseId");


        List<Expense.ExpenseDivision> division = new ArrayList<Expense.ExpenseDivision>();
        for (Map.Entry<String, CurrencyEditText> pair : expenseRecord.entrySet()) {
            String payee = pair.getKey();
            CurrencyEditText amount = pair.getValue();
            if (payee != payer && amount.getRawValue() > 0) {
                division.add(new Expense.ExpenseDivision(amount.getRawValue(), payee, payer));
            }
        }

        if (division.size() > 0) {
            Gson gson = new Gson();
            final Expense expense = new Expense(
                    id,
                    expenseTitle.getText().toString(),
                    expenseAmount.getRawValue(),
                    LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                    division
            );
            JsonElement element = gson.toJsonTree(division, new TypeToken<List<Expense.ExpenseDivision>>() {
            }.getType());
            try {
                final JSONArray jsonArray = new JSONArray(element.getAsJsonArray().toString());
                house.put("nextExpenseId", id + 1);
                house.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        ParseObject expenseEntry = new ParseObject("Expense");
                        expenseEntry.put("houseName", houseName);
                        expenseEntry.put("expenseId", id);
                        expenseEntry.put("expenseTitle", expense.title);
                        expenseEntry.put("expenseAmount", expense.total);
                        expenseEntry.put("expenseDate", expense.date);
                        expenseEntry.put("division", jsonArray);
                        expenseEntry.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    dismiss();
                                } else {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class UserAdapter extends ArrayAdapter<ParseObject> {

        private Context mContext;
        private List<ParseObject> users;

        private ImageView circle;
        private TextView name;
        private CurrencyEditText amount;
        private HashMap<String, CurrencyEditText> expenseRecord;

        public UserAdapter(@NonNull Context context, ArrayList<ParseObject> users, HashMap<String, CurrencyEditText> expenseRecord) {
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
                user_row = LayoutInflater.from(this.mContext).inflate(R.layout.expense_manager_user_row, parent, false);
            }

            ParseObject currUser = users.get(position);

            circle = user_row.findViewById(R.id.user_row_circle);
            name = user_row.findViewById(R.id.user_row_name);
            amount = user_row.findViewById(R.id.user_row_amount);

            name.setText(currUser.get("username").toString());

            GradientDrawable drawable = (GradientDrawable) circle.getDrawable();
            drawable.setColor((int) currUser.get("color"));

            if (expenseRecordInit != null) {
                amount.setValue(expenseRecordInit[position]); // Apparently getView isn't called unless the element is visible lol. Have to manually set invisible rows.
            }

            expenseRecord.put(name.getText().toString(), amount);

            return user_row;
        }


    }
}
