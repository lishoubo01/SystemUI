package com.freeme.systemui.qs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class FreemeFloatToolkit {

    private static final int NETWORK_SWITCH_DIALOG_LONG_TIMEOUT = 4000;

    private Handler mHandler = new Handler();
    private Dialog mDialog;

    public FreemeFloatToolkit() {}

    public Dialog create(Context context, int layoutId) {
        if (mDialog == null) {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layoutId);
            dialog.setCanceledOnTouchOutside(true);
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
            dialog.getWindow().getAttributes().privateFlags |=
                    WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mDialog = null;
                }
            });

            mDialog = dialog;
        }
        return mDialog;
    }

    public void show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
        }
        mHandler.removeCallbacks(mDialogRunnable);
        mHandler.postDelayed(mDialogRunnable, NETWORK_SWITCH_DIALOG_LONG_TIMEOUT);
    }

    private Runnable mDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
        }
    };

}
