package com.freeme.systemui.statusbar;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Slog;

import com.android.systemui.statusbar.CommandQueue;
import com.freeme.systemui.tint.TintManager;

public class FreemeCommandQueue extends CommandQueue {
    private static final String TAG = "FreemeCommandQueue";
    private static final boolean DEBUG = false;

    private static final int UPDATE_BARBG_COLOR = 1;
    private static final int MSG_SET_SYSTEMUI_COLOR = 101;
    private static Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_BARBG_COLOR:
                    Bundle data = msg.getData();
                    if (data != null && data.containsKey("isFreemeStyle")) {
                        TintManager.getInstance().updateBarBgColor(msg.getData().getInt("isFreemeStyle"),
                                msg.arg1, msg.arg2, data.containsKey("isFreemeLightStyle") ? data.getInt("isFreemeLightStyle") : 0);
                        break;
                    }
            }
            super.handleMessage(msg);
        }
    };

    public FreemeCommandQueue(Callbacks callbacks) {
        super(callbacks);
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case MSG_SET_SYSTEMUI_COLOR:
                if (DEBUG) {
                    Slog.i(TAG, "updateBarBgColor");
                }
                data.enforceInterface("com.android.internal.statusbar.IStatusBar");
                onBarBgColorChanged(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }

    private void onBarBgColorChanged(int isFreemeStyle, int statusBarColor, int navigationColor, int isFreemeLightStyle) {
        if (DEBUG) {
            Slog.d(TAG, "CommandQueue, setBarBackgroundColor");
        }
        mHandler.removeMessages(UPDATE_BARBG_COLOR);
        Message msg = mHandler.obtainMessage(UPDATE_BARBG_COLOR);
        Bundle data = msg.getData();
        if (data == null) {
            data = new Bundle();
        }
        data.putInt("isFreemeStyle", isFreemeStyle);
        data.putInt("isFreemeLightStyle", isFreemeLightStyle);
        msg.setData(data);
        msg.arg1 = statusBarColor;
        msg.arg2 = navigationColor;
        mHandler.sendMessage(msg);
    }
}
