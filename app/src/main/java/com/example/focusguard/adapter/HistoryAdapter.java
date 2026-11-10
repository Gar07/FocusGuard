package com.example.focusguard.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.focusguard.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public static class HistoryItem {
        public final String title;
        public final String badgeText;
        public final String timestamp;
        public final String subtitle;
        public final String detailLog;
        public final int badgeType; // 0 = COMPLETED, 1 = INTERRUPTED, 2 = SENTRY ALERT

        public HistoryItem(String title, String badgeText, String timestamp, String subtitle, String detailLog, int badgeType) {
            this.title = title;
            this.badgeText = badgeText;
            this.timestamp = timestamp;
            this.subtitle = subtitle;
            this.detailLog = detailLog;
            this.badgeType = badgeType;
        }
    }

    private final List<HistoryItem> items = new ArrayList<>();

    public void setItems(List<HistoryItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvBadge.setText(item.badgeText);
        holder.tvTimestamp.setText(item.timestamp);
        holder.tvSubtitle.setText(item.subtitle);
        holder.tvDetailLog.setText(item.detailLog);

        if (item.badgeType == 0) {
            holder.tvBadge.setTextColor(Color.parseColor("#10B981")); // Emerald
            holder.tvDetailLog.setTextColor(Color.parseColor("#06B6D4")); // Cyan
        } else if (item.badgeType == 1) {
            holder.tvBadge.setTextColor(Color.parseColor("#F59E0B")); // Amber
            holder.tvDetailLog.setTextColor(Color.parseColor("#F59E0B"));
        } else {
            holder.tvBadge.setTextColor(Color.parseColor("#EF4444")); // Crimson
            holder.tvDetailLog.setTextColor(Color.parseColor("#F8FAFC"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvBadge;
        final TextView tvTimestamp;
        final TextView tvSubtitle;
        final TextView tvDetailLog;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvBadge = itemView.findViewById(R.id.tvItemBadge);
            tvTimestamp = itemView.findViewById(R.id.tvItemTimestamp);
            tvSubtitle = itemView.findViewById(R.id.tvItemSubtitle);
            tvDetailLog = itemView.findViewById(R.id.tvItemDetailLog);
        }
    }
}
