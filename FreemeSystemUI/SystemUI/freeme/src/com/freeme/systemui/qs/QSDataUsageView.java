package com.freeme.systemui.qs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settingslib.net.DataUsageController;
import com.android.systemui.R;

public class QSDataUsageView extends LinearLayout {
    private Context mContext;
    private DataUsageView mDataUsageView;
    private static final int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private TelephonyManager mgr;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            updateVisibility();
        }
    };

    public QSDataUsageView(Context context) {
        super(context);
        mContext = context;
        mgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mDataUsageView = (DataUsageView) LayoutInflater.from(mContext).
                inflate(R.layout.data_usage_view_layout, this, false);
        addView(mDataUsageView);
        updateVisibility();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        mContext.registerReceiver(mReceiver, filter);
    }

    private void updateVisibility() {
        int activeSimCount = getActiveSIMCount();
        mDataUsageView.setVisibility(activeSimCount <= 0 ? View.GONE : View.VISIBLE);
    }

    private int getActiveSIMCount() {
        int count = 0;
        for (int sub = 0; sub < mPhoneCount; sub++) {
            if (TelephonyManager.SIM_STATE_READY == mgr.getSimState()) {
                count++;
            }
        }
        return count;
    }

    public void setDataUsageController(DataUsageController controller) {
        mDataUsageView.setDataUsageController(controller);
    }

    public void onPanelVisible() {
        mDataUsageView.updateUsageInfo();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

}
