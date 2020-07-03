package com.cs446.housematehub;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.List;

public class HousematesListAdapter extends RecyclerView.Adapter<HousematesListAdapter.HousemmatesViewHolder> {

    private WeakReference<Context> mContextReference;

    private List<ParseUser> mHousemates;

    public HousematesListAdapter(List<ParseUser> data, Context context) {
        mHousemates = data;
        mContextReference = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public HousemmatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContextReference.get());
        return new HousemmatesViewHolder(inflater, parent);
    }

    @Override
    public void onBindViewHolder(@NonNull HousemmatesViewHolder holder, int position) {
        holder.bind(mHousemates.get(position));
    }

    @Override
    public int getItemCount() {
        return mHousemates.size();
    }

    public List<ParseUser> getData() {
        return mHousemates;
    }

    public static class HousemmatesViewHolder extends RecyclerView.ViewHolder {

        private ImageView mHousemateCircle;
        private TextView mHousemateName;

        public HousemmatesViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_housemmate, parent, false));
            mHousemateCircle = itemView.findViewById(R.id.housemate_circle_row_item);
            mHousemateName = itemView.findViewById(R.id.housemate_name_row_item);
        }

        public void bind(@NonNull ParseUser housemate) {
            ((GradientDrawable) mHousemateCircle.getDrawable()).setColor((int) housemate.get("color"));
            mHousemateName.setText(housemate.getUsername());
        }
    }

}
