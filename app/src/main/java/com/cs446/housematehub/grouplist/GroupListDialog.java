package com.cs446.housematehub.grouplist;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.cs446.housematehub.expenses.Expense;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.security.acl.Group;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class GroupListDialog extends DialogFragment {

    private EditText listTitle;
    private EditText listDescription;
    private CheckBox listSubscribe;

    public GroupListDialog() {
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
        View view = inflater.inflate(R.layout.fragment_group_list_dialog, container, false);

        Button cancelButton = view.findViewById(R.id.cancel_button);
        Button createButton = view.findViewById(R.id.create_button);
        listSubscribe = (CheckBox) view.findViewById(R.id.list_subscription);
        listTitle = view.findViewById(R.id.list_title);
        listDescription = view.findViewById(R.id.list_description);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listTitle.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), "List must have a title", Toast.LENGTH_SHORT).show();
                } else {
                    createGroupList();
                }
            }
        });

        return view;
    }

    public void createGroupList() {
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

        house = houseRes.get(0);
        final int id = (int) house.get("groupListId");

        List<GroupList.GroupListItem> dummyListItems = new ArrayList<>();
        final GroupList groupList = new GroupList(
                id,
                listTitle.getText().toString(),
                listDescription.getText().toString(),
                listSubscribe.isChecked(),
                dummyListItems
        );

        house.put("groupListId", id + 1);
        house.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseObject groupListEntity = new ParseObject("GroupList");
                groupListEntity.put("houseName", houseName);
                groupListEntity.put("groupListId", id);
                groupListEntity.put("groupListTitle", groupList.title);
                groupListEntity.put("groupListDescription", groupList.description);
                groupListEntity.put("groupListSubscription", groupList.isSubscribed);
                groupListEntity.put("listItems", groupList.listItems);
                groupListEntity.saveInBackground(new SaveCallback() {
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
    }
}
