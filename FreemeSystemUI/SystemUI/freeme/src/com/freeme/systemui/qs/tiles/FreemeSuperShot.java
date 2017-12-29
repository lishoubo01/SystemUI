package com.freeme.systemui.qs.tiles;

import android.content.Intent;
import android.os.Handler;

import com.android.internal.logging.MetricsProto;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.State;

public class FreemeSuperShot extends QSTile<QSTile.State> {
    private static final String TAG = "FreemeSuperShot";

    private Handler mHandler = new Handler();

    public FreemeSuperShot(Host host) {
        super(host);
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
        return mContext.getString(R.string.freeme_super_shot);
    }

    @Override
    protected void handleClick() {
        mHost.collapsePanels();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScreenRecorder();
            }
        }, 500);
    }

    private void startScreenRecorder() {
        Intent service = new Intent("com.freeme.supershot.MainMenu");
        service.setPackage("com.freeme.supershot");
        mContext.startService(service);
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        state.icon = ResourceIcon.get(R.drawable.freeme_ic_qs_supershot_off);
        state.label = mContext.getString(R.string.freeme_super_shot);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.QS_PANEL;
    }

}