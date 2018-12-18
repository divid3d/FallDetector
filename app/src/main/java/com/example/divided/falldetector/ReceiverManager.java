package com.example.divided.falldetector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import java.util.ArrayDeque;
import java.util.Queue;

public class ReceiverManager {

    private Queue<BroadcastReceiver> receivers = new ArrayDeque<>();
    private Context context;

    ReceiverManager(Context context) {
        this.context = context;
    }

    public void registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
        receivers.add(receiver);
        context.registerReceiver(receiver, intentFilter);
    }

    public boolean isReceiverRegistered(BroadcastReceiver receiver) {
        boolean registered = receivers.contains(receiver);
        return registered;
    }

    public void unregisterReceiver(BroadcastReceiver receiver) {
        if (isReceiverRegistered(receiver)) {
            receivers.remove(receiver);
            context.unregisterReceiver(receiver);
        }
    }

    public void unregisterAllReceivers() {
        for (BroadcastReceiver broadcastReceiver : receivers) {
            unregisterReceiver(broadcastReceiver);
        }
    }
}
