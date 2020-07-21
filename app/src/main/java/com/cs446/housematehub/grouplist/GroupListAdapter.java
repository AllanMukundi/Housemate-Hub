package com.cs446.housematehub.grouplist;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cs446.housematehub.HouseMainActivity;
import com.cs446.housematehub.R;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.GroupListViewHolder> {
    private List<ParseObject> listData;
    private GroupListAdapterInterface groupListListener;
    private Context context;
    private View view;

    public static class GroupListViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout listContainer;
        private TextView listTitle;
        private ImageButton listSubscribeOff, listSubscribeOn, listDelete;
        public GroupListViewHolder(View v) {
            super(v);
            listContainer = (ConstraintLayout) v.findViewById(R.id.group_list);
            listTitle = (TextView) v.findViewById(R.id.list_title);
            listSubscribeOff = (ImageButton) v.findViewById(R.id.list_subscribe_off);
            listSubscribeOn = (ImageButton) v.findViewById(R.id.list_subscribe_on);
            listDelete = (ImageButton) v.findViewById(R.id.list_delete);
        }
    }

    @Override
    public GroupListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        view = layoutInflater.inflate(R.layout.fragment_group_list_adapter, parent, false);
        return new GroupListViewHolder(view);
    }

    public GroupListAdapter(List<ParseObject> listDataSet, GroupListAdapterInterface listener) {
        listData = listDataSet;
        groupListListener = listener;
    }

    @Override
    public void onBindViewHolder(final GroupListViewHolder holder, final int position) {
        holder.listTitle.setText(listData.get(position).getString("groupListTitle"));

        if (listData.get(position).getBoolean("groupListSubscription")) {
            holder.listSubscribeOff.setVisibility(View.GONE);
            holder.listSubscribeOn.setVisibility(View.VISIBLE);
        } else {
            holder.listSubscribeOff.setVisibility(View.VISIBLE);
            holder.listSubscribeOn.setVisibility(View.GONE);
        }

        holder.listContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Fragment fragment = new GroupListItemManager(listData.get(position).getObjectId());
                ((HouseMainActivity) view.getContext()).changeFragments(HouseMainActivity.TAB_LIST, fragment,true, true);
            }
        });

        holder.listSubscribeOff.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subscribeGroupList(listData.get(position).getObjectId(), !listData.get(position).getBoolean("groupListSubscription"));
                holder.listSubscribeOff.setVisibility(View.GONE);
                holder.listSubscribeOn.setVisibility(View.VISIBLE);
            }
        });
        holder.listSubscribeOn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                subscribeGroupList(listData.get(position).getObjectId(), !listData.get(position).getBoolean("groupListSubscription"));
                holder.listSubscribeOff.setVisibility(View.VISIBLE);
                holder.listSubscribeOn.setVisibility(View.GONE);
            }
        });

        holder.listDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteGroupList(listData.get(position).getObjectId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public interface GroupListAdapterInterface{
        public void refreshList();
    }

    public void deleteGroupList(String objectId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");;

        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject entity, ParseException e) {
                if (e == null) {
                    entity.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                groupListListener.refreshList();
                            }
                        }
                    });
                }
            }
        });
    }

    public void subscribeGroupList(String objectId, final boolean listSubscription) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("GroupList");

        query.getInBackground(objectId, new GetCallback<ParseObject>() {
            public void done(ParseObject entity, ParseException e) {
                if (e == null) {
                    entity.put("groupListSubscription", listSubscription);
                    entity.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                groupListListener.refreshList();
                            }
                        }
                    });
                    groupListListener.refreshList();
                }
            }
        });
    }
}
