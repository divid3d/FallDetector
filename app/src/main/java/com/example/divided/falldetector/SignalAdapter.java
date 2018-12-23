package com.example.divided.falldetector;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class SignalAdapter extends RecyclerView.Adapter<SignalAdapter.MyViewHolder> {

    private List<File> signals;

    public SignalAdapter(List<File> signals) {
        this.signals = signals;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.signal_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        File signal = signals.get(position);
        holder.signalName.setText(signal.getName().replace(".csv",""));
    }

    @Override
    public int getItemCount() {
        return signals.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView signalName;

        public MyViewHolder(View view) {
            super(view);
            signalName = view.findViewById(R.id.signal_name);
        }
    }
}
