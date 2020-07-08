package com.cs446.housematehub.grouplist;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.cs446.housematehub.expenses.Expense;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Type;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GroupListItemManager extends Fragment implements GroupListItemAdapter.GroupListItemAdapterInterface {
    private View view;

    private List<ParseObject> groupListResult;
    private List<GroupList.GroupListItem> listData, tmpListData;
    private JSONArray jsonListData;

    private RecyclerView listItemRecyclerView;
    private RecyclerView.Adapter listItemAdapter;
    private RecyclerView.LayoutManager listItemLayoutManager;
    private String objectId, houseName, listTitle;
    private HouseMainActivity mainActivity;

    private EditText listItemTitle;
    private ImageButton listItemAdd;


    public GroupListItemManager(String lObjectId) {
        objectId = lObjectId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = ((HouseMainActivity) getActivity());
        houseName = (String) mainActivity.getCurrentHouse().get("houseName");
        getGroupListItem();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_group_list_item_manager, container, false);

        getGroupListItem();

        listTitle = groupListResult.get(0).getString("groupListTitle");
        jsonListData = groupListResult.get(0).getJSONArray("listItems");

        mainActivity.setToolbarTitle(listTitle);
        mainActivity.enableBack();

        listItemTitle = (EditText) view.findViewById(R.id.list_item_title);
        listItemAdd = (ImageButton) view.findViewById(R.id.list_item_add);

        listItemTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    addGroupListItem();
                    return true;
                }
                return false;
            }
        });
        listItemAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listItemTitle.getText().toString().length() != 0) {
                    addGroupListItem();
                }
            }
        });

        listItemRecyclerView = (RecyclerView) view.findViewById(R.id.listItemRecyclerView);

        listItemLayoutManager = new LinearLayoutManager(getContext());
        listItemRecyclerView.setLayoutManager(listItemLayoutManager);

        Gson gson = new Gson();
        Type type = new TypeToken<List<GroupList.GroupListItem>>(){}.getType();
        listData = gson.fromJson(String.valueOf(jsonListData), type);

        listItemAdapter = new GroupListItemAdapter(listData, objectId, this);
        listItemRecyclerView.setAdapter(listItemAdapter);

        return view;
    }

    public void refreshList() {
        listData.clear();
        getGroupListItem();

        jsonListData = groupListResult.get(0).getJSONArray("listItems");

        Gson gson = new Gson();
        Type type = new TypeToken<List<GroupList.GroupListItem>>(){}.getType();
        tmpListData = gson.fromJson(String.valueOf(jsonListData), type);

        listData.addAll(tmpListData);
        if (listItemAdapter != null) {
            listItemAdapter.notifyDataSetChanged();
        }
    }

    public void addGroupListItem() {
        GroupList.GroupListItem listItem = new GroupList.GroupListItem("1", listItemTitle.getText().toString(), false);
        listData.add(listItem);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");;

        Gson gson = new Gson();
        JsonElement listDataJsonElement = gson.toJsonTree(listData, new TypeToken<List<GroupList.GroupListItem>>() {}.getType());

        try {
            final JSONArray listDataJsonArray = new JSONArray(listDataJsonElement.getAsJsonArray().toString());
            query.getInBackground(objectId, new GetCallback<ParseObject>() {
                public void done(ParseObject entity, ParseException e) {
                    if (e == null) {
                        entity.put("listItems", listDataJsonArray);
                        entity.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    refreshList();
                                    listItemTitle.setText("");
                                }
                            }
                        });
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getGroupListItem() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");
        query.whereEqualTo("houseName", houseName);
        query.whereEqualTo("objectId", objectId);
        try {
            groupListResult = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
