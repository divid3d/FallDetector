package com.example.divided.falldetector;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.divided.falldetector.model.TestSignal;

import java.util.List;

public class SignalAdapter extends RecyclerView.Adapter<SignalAdapter.MyViewHolder> {

    private List<TestSignal> signals;

    SignalAdapter(List<TestSignal> signals) {
        this.signals = signals;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.signal_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TestSignal signal = signals.get(position);
        holder.signalName.setText(signal.getPath().getName().replace(".csv", ""));
        holder.id.setText(String.valueOf(position + 1));
        holder.testResult.setText("Test result:\t" + signal.getTestResult());

        if (signal.getTestResult().equals("true")) {
            holder.icon.setImageResource(R.drawable.ic_11015_falling_man);
        } else {
            holder.icon.setImageResource(R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return signals.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView id;
        TextView signalName;
        TextView testResult;
        ImageView icon;

        MyViewHolder(View view) {
            super(view);
            signalName = view.findViewById(R.id.text_view_signal_name);
            id = view.findViewById(R.id.text_view_id);
            testResult = view.findViewById(R.id.text_view_test_result);
            icon = view.findViewById(R.id.icon);
        }
    }
}
