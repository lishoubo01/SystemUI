package com.freeme.systemui.qs.tiles;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.telephony.PhoneConstants;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.R;
import com.mediatek.systemui.PluginManager;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.qs.QSTile.SignalState;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSIconView;

/// M: add DataUsage in quicksetting @{
/// add DataUsage in quicksetting @}

/** Quick settings tile: Cellular **/
public class FreemeCellularTile extends com.android.systemui.qs.QSTile<com.android.systemui.qs.QSTile.SignalState> {

    // / M: For debug @{
    private static final String TAG = "CellularTile";
    private static final boolean DBG = true;
    // @}

    static final Intent CELLULAR_SETTINGS = new Intent().setComponent(new ComponentName(
            "com.android.phone", "com.android.phone.MobileNetworkSettings"));

    private final NetworkController mController;
    private final DataUsageController mDataController;
    private final CellularDetailAdapter mDetailAdapter;

    /// M: add DataUsage for operator @{
    private com.mediatek.systemui.ext.IQuickSettingsPlugin mQuickSettingsPlugin;
    private boolean mDisplayDataUsage;
    private com.android.systemui.qs.QSTile.Icon mIcon;
    /// add DataUsage for operator @}

    // M: Disable other sub's data when enable default sub's data
    private TelephonyManager mTelephonyManager;

    private final CellSignalCallback mSignalCallback = new CellSignalCallback();

    public FreemeCellularTile(Host host) {
        super(host);
        mController = host.getNetworkController();
        mDataController = mController.getMobileDataController();
        mDetailAdapter = new CellularDetailAdapter();
        /// M: add DataUsage for operator @{
        mQuickSettingsPlugin = PluginManager
                .getQuickSettingsPlugin(mContext);
        mDisplayDataUsage = mQuickSettingsPlugin.customizeDisplayDataUsage(false);
        mIcon = com.android.systemui.qs.QSTile.ResourceIcon.get(R.drawable.ic_qs_data_usage);
        /// add DataUsage for operator @}

        // M: Disable other sub's data when enable default sub's data
        mTelephonyManager = TelephonyManager.from(mContext);
    }

    @Override
    public SignalState newTileState() {
        return new com.android.systemui.qs.QSTile.SignalState();
    }

    @Override
    public DetailAdapter getDetailAdapter() {
        return mDetailAdapter;
    }

    @Override
    public void setListening(boolean listening) {
        if (listening) {
            mController.addSignalCallback(mSignalCallback);
        } else {
            mController.removeSignalCallback(mSignalCallback);
        }
    }

    @Override
    public QSIconView createTileView(Context context) {
        return new com.android.systemui.qs.SignalTileView(context);
    }

    @Override
    public Intent getLongClickIntent() {
        return CELLULAR_SETTINGS;
    }

    @Override
    protected void handleClick() {
        if (!com.mediatek.systemui.statusbar.util.SIMHelper.isSimInsertedBySlot(mContext,
                PhoneConstants.SIM_ID_1)
                && !com.mediatek.systemui.statusbar.util.SIMHelper.isSimInsertedBySlot(mContext,
                PhoneConstants.SIM_ID_2)) {
            return;
        }
        // M: Don't turn on/off data when default SIM isn't set @{
        if (!isDefaultDataSimExist()) {
            handleShowSIMSwitcherDialog();
            return;
        }
        // @}
        boolean dataEnabled = mDataController.isMobileDataSupported()
                && mDataController.isMobileDataEnabled();
        MetricsLogger.action(mContext, MetricsEvent.QS_CELLULAR_TOGGLE, !dataEnabled);
        mDataController.setMobileDataEnabled(!dataEnabled);
        // M: Disable other sub's data when enable default sub's data @{
        if (!dataEnabled) disableDataForOtherSubscriptions();
        // @}
    }

    @Override
    protected void handleSecondaryClick() {
        Log.d(TAG, "handleSecondaryClick()");
        /*/ freeme.gouzhouping, 20170912. quick settings tile.
        if (mDisplayDataUsage) {
            handleClick();
        } else {
            // M: Don't turn on/off data when default SIM isn't set @{
            if(!isDefaultDataSimExist()) return;
            // @}
            boolean dataEnabled = mDataController.isMobileDataSupported()
                    && mDataController.isMobileDataEnabled();
            MetricsLogger.action(mContext, MetricsEvent.QS_CELLULAR_TOGGLE, !dataEnabled);
            mDataController.setMobileDataEnabled(!dataEnabled);
            // M: Disable other sub's data when enable default sub's data @{
            if (!dataEnabled) disableDataForOtherSubscriptions();
            // @}
        }
        //*/
        MetricsLogger.action(mContext, getMetricsCategory());
        // M: Start setting activity when default SIM isn't set
        if (mDataController.isMobileDataSupported() && isDefaultDataSimExist()) {
            showDetail(true);
        } else {
            mHost.startActivityDismissingKeyguard(CELLULAR_SETTINGS);
        }
        //*/
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.freeme_data_connection);
    }

    @Override
    protected void handleUpdateState(com.android.systemui.qs.QSTile.SignalState state, Object arg) {
        /// M: add DataUsage for operator @{
        if (mDisplayDataUsage) {
            Log.i(TAG, "customize datausage, displayDataUsage = " + mDisplayDataUsage);
            //state.visible = true;
            /*/ freeme. gouzhouping, 20170908. replace the resources.
            state.icon = mIcon;
            state.label = mContext.getString(R.string.data_usage);
            //*/
            state.contentDescription = mContext.getString(R.string.data_usage);
            return;
        }
        /// add DataUsage for operator @}

        CallbackInfo cb = (CallbackInfo) arg;
        if (cb == null) {
            cb = mSignalCallback.mInfo;
        }

        final Resources r = mContext.getResources();
        final int iconId = cb.noSim ? R.drawable.ic_qs_no_sim
                : !cb.enabled || cb.airplaneModeEnabled ? R.drawable.ic_qs_signal_disabled
                : cb.mobileSignalIconId > 0 ? cb.mobileSignalIconId
                : R.drawable.ic_qs_signal_no_signal;

        final String signalContentDesc = cb.enabled && (cb.mobileSignalIconId > 0)
                ? cb.signalContentDescription
                : r.getString(R.string.accessibility_no_signal);

        if (cb.noSim) {
            /*/ freeme.gouzhouping, 20170908. replace the resources.
            state.contentDescription = state.label;
            /*/
            state.label = mContext.getString(R.string.freeme_data_connection);
            //*/
        } else {
            String enabledDesc = cb.enabled ? r.getString(R.string.accessibility_cell_data_on)
                    : r.getString(R.string.accessibility_cell_data_off);

            state.contentDescription = r.getString(
                    R.string.accessibility_quick_settings_mobile,
                    enabledDesc, signalContentDesc,
                    state.label);
            state.minimalContentDescription = r.getString(
                    R.string.accessibility_quick_settings_mobile,
                    r.getString(R.string.accessibility_cell_data), signalContentDesc,
                    state.label);
        }
        state.contentDescription = state.contentDescription + "," + r.getString(
                R.string.accessibility_quick_settings_open_settings, getTileLabel());
        state.minimalAccessibilityClassName = state.expandedAccessibilityClassName
                = Button.class.getName();
        state.value = mDataController.isMobileDataSupported()
                && mDataController.isMobileDataEnabled() && !cb.airplaneModeEnabled;

        state.icon = com.android.systemui.qs.QSTile.ResourceIcon.get(state.value ? R.drawable.freeme_ic_qs_data_connection_on :
                R.drawable.freeme_ic_qs_data_connection_off);
        state.label = mContext.getString(R.string.freeme_data_connection);

        // /M: Change the icon/label when default SIM isn't set @{
        if (mTelephonyManager.getNetworkOperator() != null
                && !cb.noSim && !isDefaultDataSimExist()) {
            Log.d(TAG, "handleUpdateState(), default data sim not exist");

            state.icon = com.android.systemui.qs.QSTile.ResourceIcon.get(R.drawable.freeme_ic_qs_data_connection_off);
            state.label = mContext.getString(R.string.freeme_data_connection);

            state.overlayIconId = 0;
            state.filter = true;
            state.activityIn = false;
            state.activityOut = false;
        }
        // @}
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_CELLULAR;
    }

    @Override
    public boolean isAvailable() {
        return mController.hasMobileDataFeature();
    }

    // Remove the period from the network name
    public static String removeTrailingPeriod(String string) {
        if (string == null) return null;
        final int length = string.length();
        if (string.endsWith(".")) {
            return string.substring(0, length - 1);
        }
        return string;
    }

    private static final class CallbackInfo {
        boolean enabled;
        boolean wifiEnabled;
        boolean airplaneModeEnabled;
        int mobileSignalIconId;
        String signalContentDescription;
        int dataTypeIconId;
        String dataContentDescription;
        boolean activityIn;
        boolean activityOut;
        String enabledDesc;
        boolean noSim;
        boolean isDataTypeIconWide;
    }

    private final class CellSignalCallback extends com.android.systemui.statusbar.policy.SignalCallbackAdapter {
        private final CallbackInfo mInfo = new CallbackInfo();
        @Override
        public void setWifiIndicators(boolean enabled, com.android.systemui.statusbar.policy.NetworkController.IconState statusIcon, com.android.systemui.statusbar.policy.NetworkController.IconState qsIcon,
                                      boolean activityIn, boolean activityOut, String description) {
            mInfo.wifiEnabled = enabled;
            refreshState(mInfo);
        }
        /// M: Modify to support [Network Type and volte on Statusbar], change the implement methods
        /// add one more parameter for network type.
        @Override
        public void setMobileDataIndicators(com.android.systemui.statusbar.policy.NetworkController.IconState statusIcon, com.android.systemui.statusbar.policy.NetworkController.IconState qsIcon, int statusType,
                                            int networkIcon, int volteIcon, int qsType, boolean activityIn, boolean activityOut,
                                            String typeContentDescription, String description, boolean isWide, int subId) {
            if (qsIcon == null) {
                // Not data sim, don't display.
                Log.d(TAG, "setMobileDataIndicator qsIcon = null, Not data sim, don't display");
                return;
            }
            mInfo.enabled = qsIcon.visible;
            mInfo.mobileSignalIconId = qsIcon.icon;
            mInfo.signalContentDescription = qsIcon.contentDescription;
            mInfo.dataTypeIconId = qsType;
            mInfo.dataContentDescription = typeContentDescription;
            mInfo.activityIn = activityIn;
            mInfo.activityOut = activityOut;
            mInfo.enabledDesc = description;
            mInfo.isDataTypeIconWide = qsType != 0 && isWide;
            if (DBG) {
                Log.d(TAG, "setMobileDataIndicators info.enabled = " + mInfo.enabled +
                    " mInfo.mobileSignalIconId = " + mInfo.mobileSignalIconId +
                    " mInfo.signalContentDescription = " + mInfo.signalContentDescription +
                    " mInfo.dataTypeIconId = " + mInfo.dataTypeIconId +
                    " mInfo.dataContentDescription = " + mInfo.dataContentDescription +
                    " mInfo.activityIn = " + mInfo.activityIn +
                    " mInfo.activityOut = " + mInfo.activityOut +
                    " mInfo.enabledDesc = " + mInfo.enabledDesc +
                    " mInfo.isDataTypeIconWide = " + mInfo.isDataTypeIconWide);
            }
            refreshState(mInfo);
        }

        @Override
        public void setNoSims(boolean show) {
            Log.d(TAG, "setNoSims, noSim = " + show);
            mInfo.noSim = show;
            if (mInfo.noSim) {
                // Make sure signal gets cleared out when no sims.
                mInfo.mobileSignalIconId = 0;
                mInfo.dataTypeIconId = 0;
                // Show a No SIMs description to avoid emergency calls message.
                mInfo.enabled = true;
                mInfo.enabledDesc = mContext.getString(
                        R.string.keyguard_missing_sim_message_short);
                mInfo.signalContentDescription = mInfo.enabledDesc;
            }
            refreshState(mInfo);
        }

        @Override
        public void setIsAirplaneMode(com.android.systemui.statusbar.policy.NetworkController.IconState icon) {
            mInfo.airplaneModeEnabled = icon.visible;
            refreshState(mInfo);
        }

        @Override
        public void setMobileDataEnabled(boolean enabled) {
            mDetailAdapter.setMobileDataEnabled(enabled);
        }
    };

    private final class CellularDetailAdapter implements com.android.systemui.qs.QSTile.DetailAdapter {

        @Override
        public CharSequence getTitle() {
            return mContext.getString(R.string.quick_settings_cellular_detail_title);
        }

        @Override
        public Boolean getToggleState() {
            return mDataController.isMobileDataSupported()
                    ? mDataController.isMobileDataEnabled()
                    : null;
        }

        @Override
        public Intent getSettingsIntent() {
            return CELLULAR_SETTINGS;
        }

        @Override
        public void setToggleState(boolean state) {
            MetricsLogger.action(mContext, MetricsEvent.QS_CELLULAR_TOGGLE, state);
            mDataController.setMobileDataEnabled(state);
            // M: Disable other sub's data when enable default sub's data @{
            if (state) disableDataForOtherSubscriptions();
            // @}
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.QS_DATAUSAGEDETAIL;
        }

        @Override
        public View createDetailView(Context context, View convertView, ViewGroup parent) {
            final com.android.systemui.qs.tiles.DataUsageDetailView v = (com.android.systemui.qs.tiles.DataUsageDetailView) (convertView != null
                    ? convertView
                    : LayoutInflater.from(mContext).inflate(R.layout.data_usage, parent, false));
            final DataUsageController.DataUsageInfo info = mDataController.getDataUsageInfo();
            if (info == null) return v;
            v.bind(info);
            return v;
        }

        public void setMobileDataEnabled(boolean enabled) {
            fireToggleStateChanged(enabled);
        }
    }

    // /M: Change the label when default SIM isn't set @{
    public boolean isDefaultDataSimExist() {
        int[] subList = SubscriptionManager.from(mContext).getActiveSubscriptionIdList();
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        Log.d(TAG, "isDefaultDataSimExist, Default data sub id : " + defaultDataSubId);
        for (int subId : subList) {
            if (subId == defaultDataSubId) {
                return true;
            }
        }
        return false;
    }
    // @}

    // /M: Disable other sub's data when enable default sub's data@{
    private void disableDataForOtherSubscriptions() {
        int[] subList = SubscriptionManager.from(mContext).getActiveSubscriptionIdList();
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        for (int subId : subList) {
            if (subId != defaultDataSubId && mTelephonyManager.getDataEnabled(subId)) {
                Log.d(TAG, "Disable other sub's data : " + subId);
                mTelephonyManager.setDataEnabled(subId, false);
            }
        }
    }
    // @}

    //*/ freeme. gouzhouping, 20170809. get default sim id
    private void handleShowSIMSwitcherDialog() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.sim.SimDialogActivity"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("dialog_type", 4);
        mHost.startActivityDismissingKeyguard(intent);
    }
    //*/
}
