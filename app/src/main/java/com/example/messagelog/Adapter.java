package com.example.messagelog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MessageLogViewHolder> {

    private List<Model> messageLogDataList;

    public Adapter(List<Model> messageLogDataList) {
        this.messageLogDataList = messageLogDataList;
    }

    @NonNull
    @Override
    public MessageLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_row, parent, false);
        return new MessageLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageLogViewHolder holder, int position) {
        Model messageLogData = messageLogDataList.get(position);
        holder.bind(messageLogData);
    }

    @Override
    public int getItemCount() {
        return messageLogDataList.size();
    }

    public static class MessageLogViewHolder extends RecyclerView.ViewHolder {

        private TextView tvAddress, tvBody, tvDate;

        public MessageLogViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddress = itemView.findViewById(R.id.text_view_address);
            tvBody = itemView.findViewById(R.id.text_view_body);
            tvDate = itemView.findViewById(R.id.text_view_date);
        }

        public void bind(Model messageLogData) {
            tvAddress.setText(messageLogData.getAddress());
            tvBody.setText(messageLogData.getBody());
            String formattedDate = formatDate(Long.parseLong(messageLogData.getDate()));
            tvDate.setText(formattedDate);
        }

        private String formatDate(long timeInMillis) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
            Date date = new Date(timeInMillis);
            return sdf.format(date);
        }
    }
}
