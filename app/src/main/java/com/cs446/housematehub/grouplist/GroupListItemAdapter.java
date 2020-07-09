package com.cs446.housematehub.grouplist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cs446.housematehub.R;
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

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class GroupListItemAdapter extends RecyclerView.Adapter<GroupListItemAdapter.GroupListItemViewHolder> {
    private List<GroupList.GroupListItem> listData;
    private String objectId;
    private GroupListItemAdapterInterface groupListItemListener;

    public static class GroupListItemViewHolder extends RecyclerView.ViewHolder {

        private TextView itemTitle;
        private ImageButton itemDelete;
        private CheckBox itemDone;
        public GroupListItemViewHolder(View v) {
            super(v);
            itemTitle = (TextView) v.findViewById(R.id.list_item_title);
            itemDone = (CheckBox) v.findViewById(R.id.list_item_checkBox);
            itemDelete = (ImageButton) v.findViewById(R.id.list_item_delete);
        }
    }

    @Override
    public GroupListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.fragment_group_list_item_adapter, parent, false);
        return new GroupListItemViewHolder(view);
    }

    public GroupListItemAdapter(List<GroupList.GroupListItem> listDataSet, String listObjectId, GroupListItemAdapterInterface listener) {
        listData = listDataSet;
        objectId = listObjectId;
        groupListItemListener = listener;
    }

    @Override
    public void onBindViewHolder(final GroupListItemViewHolder holder, final int position) {
        holder.itemTitle.setText(listData.get(position).title);
        holder.itemDone.setChecked(listData.get(position).isDone);

        holder.itemDone.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                completeListItem(position);
            }
        });

        holder.itemDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteListItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public interface GroupListItemAdapterInterface{
        public void refreshList();
    }

    public void completeListItem(int position) {
        GroupList.GroupListItem listItem = listData.get(position);
        listItem.setIsDone(!listData.get(position).isDone);
        listData.set(position, listItem);

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
                                    groupListItemListener.refreshList();
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

    public void deleteListItem(int position) {
        listData.remove(position);

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
                                    groupListItemListener.refreshList();
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


}
