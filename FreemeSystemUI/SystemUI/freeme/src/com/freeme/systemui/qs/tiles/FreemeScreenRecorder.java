package com.freeme.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.android.internal.logging.MetricsProto;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.Host;

public class FreemeScreenRecorder extends QSTile<QSTile.State> {
    private static final String TAG = "FreemeScreenRecorder";

    private static final String ACTION_SCREEN_RECORDER_STATE = "com.freeme.systemui.action.SCREEN_RECORDER_STATE";
    private static boolean mSwitch = false;

    private Handler mHandler = new Handler();

    public FreemeScreenRecorder(Host host) {
        super(host);

        IntentFilter filter =
                new IntentFilter(ACTION_SCREEN_RECORDER_STATE);
        mContext.registerReceiver(new ScreenRecorderReceiver(), filter);
    }

    @Override
    public Intent getLongClickIntent() {
        return new Intent();
    }

    @Override
    public State newTileState() {
        return new State();
    }

    @Override
    public void setListening(boolean listening) {}

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.freeme_screen_recorder);
    }

    @Override
    protected void handleClick() {
        mHost.collapsePanels();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScreenRecorder();
            }
        }, 250);
    }

    private void startScreenRecorder() {
        Intent intent = new Intent("android.intent.action.ScreenRecorder");
        intent.setPackage("com.freeme.screenrecorder");
        mContext.startService(intent);
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        state.label = mContext.getString(R.string.freeme_screen_recorder);
        if(mSwitch) {
            state.icon = ResourceIcon.get(R.drawable.freeme_ic_qs_screenrecorder_on);
        }else{
            state.icon = ResourceIcon.get(R.drawable.freeme_ic_qs_screenrecorder_off);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.QS_PANEL;
    }

    public class ScreenRecorderReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_SCREEN_RECORDER_STATE.equals(intent.getAction())) {
                mSwitch = intent.getBooleanExtra("isStart", false);
                refreshState();
            }
        }
    }

}