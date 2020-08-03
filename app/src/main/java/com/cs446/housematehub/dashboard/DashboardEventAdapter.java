package com.cs446.housematehub.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cs446.housematehub.R;

import java.lang.ref.WeakReference;
import java.util.List;

public class DashboardEventAdapter extends RecyclerView.Adapter<DashboardEventAdapter.DashboardEventViewHolder> {

    private WeakReference<Context> mContextReference;
    private List<DashboardEvent> mEvents;

    public DashboardEventAdapter(List<DashboardEvent> data, Context context) {
        mEvents = data;
        mContextReference = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public DashboardEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContextReference.get());
        return new DashboardEventViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardEventViewHolder holder, int position) {
        holder.bind(mEvents.get(position));
    }


    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public List<DashboardEvent> getData() {
        return mEvents;
    }

    public static class DashboardEventViewHolder extends RecyclerView.ViewHolder {

        private ImageView mIcon;
        private TextView mEventDescription;
        private TextView mEventOwnerName;

        public DashboardEventViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_dashboard_event, parent, false));
            mIcon = itemView.findViewById(R.id.event_icon);
            mEventDescription = itemView.findViewById(R.id.event_description);
            mEventOwnerName = itemView.findViewById(R.id.event_owner_name);
        }

        public void bind(@NonNull DashboardEvent event) {
            mEventDescription.setText(event.getEventDescription());
            mEventOwnerName.setText("by " + event.eventOwnerName);
            switch (event.type) {
                case EXPENSE:
                    mIcon.setImageResource(R.drawable.ic_baseline_attach_money_24);
                    break;
                case CALENDAR:
                    mIcon.setImageResource(R.drawable.ic_baseline_calendar_today_24);
                    break;
                case GROUP_LIST:
                    mIcon.setImageResource(R.drawable.ic_baseline_format_list_bulleted_24);
                    break;
            }
        }
    }

}
