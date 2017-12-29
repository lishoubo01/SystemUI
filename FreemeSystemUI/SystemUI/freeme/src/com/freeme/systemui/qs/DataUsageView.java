package com.freeme.systemui.qs;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.SubscriptionManager;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.android.settingslib.net.DataUsageController;
import com.android.settingslib.net.DataUsageController.DataUsageInfo;
import com.android.systemui.R;

import java.util.Calendar;

public class DataUsageView extends TextView {
    private int mCurrentDataSubId;
    private DataUsageController mDataController;
    private String mDataUsage;
    private long mEnd;
    private Handler mHandler;
    private long mStart;
    private Thread mThread;
    Runnable mUpdateRunnable = new Runnable() {
        public void run() {
            if (mDataUsage != null) {
                setText(mDataUsage);
            }
            mThread = null;
        }
    };

    public DataUsageView(Context context) {
        super(context);
        initView();
    }

    public DataUsageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public DataUsageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public DataUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        setText(mContext.getString(R.string.quick_panel_data_usage) + " : 0 B");
        mHandler = new Handler();
    }

    public void setDataUsageController(DataUsageController controller) {
        mDataController = controller;
    }

    public void updateUsageInfo() {
        if (mThread == null) {
            mThread = new Thread() {
                public void run() {
                    if (mDataController != null) {
                        DataUsageInfo info = mDataController.getDataUsageInfo();
                        if (info != null) {
                            long usageLevel = info.usageLevel;
                            long limitLevel = info.limitLevel;
                            usageLevel = getTotalUsedData(info);
                            limitLevel = getLimitBytes();
                            String content = Formatter.formatFileSize(mContext, usageLevel);
                            if (limitLevel > 0) {
                                content = content + "/" + Formatter.formatFileSize(mContext, limitLevel);
                            }
                            mDataUsage = mContext.getString(R.string.quick_panel_data_usage) + " : " + content;
                        } else {
                            mDataUsage = mContext.getString(R.string.quick_panel_data_usage) + " : 0 B";
                        }
                    }
                    mHandler.post(mUpdateRunnable);
                }
            };
            mThread.start();
            return;
        }
    }

    public boolean isLimitSet() {
        int switchOn;
        String limitBytesString = "set_data_limit";
        try {
            switchOn = Settings.System.getInt(mContext.getContentResolver(), "switch_traffic_settings" + mCurrentDataSubId);
        } catch (SettingNotFoundException e) {
            switchOn = 0;
        }
        if (switchOn == 0) {
            return false;
        }
        String sLimit = Settings.System.getString(mContext.getContentResolver(), limitBytesString + mCurrentDataSubId);
        if (sLimit == null || sLimit.equals("") || sLimit.equals(" ") || sLimit.equals("max")) {
            return false;
        }
        return true;
    }

    public long getLimitBytes() {
        String limitBytesString = "set_data_limit";
        String sLimit = Settings.System.getString(mContext.getContentResolver(), limitBytesString + mCurrentDataSubId);
        if (isLimitSet()) {
            try {
                long limitTraffic = (long) (Double.parseDouble(sLimit) * 1048576.0d);
                return limitTraffic;
            } catch (Exception e) {
                Log.e("DataUsageView", "Exception : " + e);
                return 0;
            }
        }
        return 0;
    }

    private int getStartDay() {
        int iStart;
        try {
            iStart = Settings.System.getInt(mContext.getContentResolver(), "set_package_start_date_value" + mCurrentDataSubId);
        } catch (SettingNotFoundException e) {
            Log.e("DataUsageView", "Exception : " + e);
            iStart = 1;
        }
        if (iStart < 1 || iStart > 31) {
            return 1;
        }
        return iStart;
    }

    private void getBounds(int dayStart) {
        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        Calendar start = Calendar.getInstance();
        start.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), dayStart, 0, 0, 0);
        Calendar end = Calendar.getInstance();
        if (currentDay >= dayStart) {
            mStart = start.getTimeInMillis();
            end.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, dayStart, 0, 0, 0);
            mEnd = end.getTimeInMillis();
            return;
        }
        start.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), dayStart, 0, 0, 0);
        mEnd = start.getTimeInMillis();
        end = Calendar.getInstance();
        end.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH) - 1, dayStart, 0, 0, 0);
        mStart = end.getTimeInMillis();
    }

    private void updateCurrentDataSubId() {
        mCurrentDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
    }

    private long getConfirmTime() {
        long lConfirmTime;
        try {
            lConfirmTime = Settings.System.getLong(mContext.getContentResolver(), "check_time" + mCurrentDataSubId);
        } catch (SettingNotFoundException e) {
            Log.e("DataUsageView", "Exception : " + e);
            lConfirmTime = 0;
        }
        if (lConfirmTime < 0) {
            return 0;
        }
        return lConfirmTime;
    }

    private long getConfirmTraffic() {
        String confirmTrafficString = "data_used_by_check_time";
        String sConfirmTraffic = Settings.System.getString(mContext.getContentResolver(), confirmTrafficString + mCurrentDataSubId);
        if (sConfirmTraffic == null || sConfirmTraffic.equals("") || sConfirmTraffic.equals(" ")) {
            return 0;
        }
        try {
            long confirmTraffic = (long) (Double.parseDouble(sConfirmTraffic) * 1048576.0d);
            return confirmTraffic;
        } catch (Exception e) {
            Log.e("DataUsageView", "Exception : " + e);
            return 0;
        }
    }

    private long getTotalUsedData(DataUsageInfo info) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        long startTraffic = 0;
        long realTraffic = 0;
        String realTrafficString = "middle_real_value";
        long totalUsedBytes = info.usageLevel;
        updateCurrentDataSubId();
        getBounds(getStartDay());
        long confirmTime = getConfirmTime();
        if (confirmTime >= mStart && confirmTime <= currentTime && confirmTime != 0) {
            startTraffic = getConfirmTraffic();
            try {
                realTraffic = Settings.System.getLong(mContext.getContentResolver(), realTrafficString + mCurrentDataSubId);
            } catch (SettingNotFoundException e) {
                Log.e("DataUsageView", "Exception : " + e);
                realTraffic = 0;
            }
        }
        long totalTraffic = (totalUsedBytes - realTraffic) + startTraffic;
        return totalTraffic;
    }

}
