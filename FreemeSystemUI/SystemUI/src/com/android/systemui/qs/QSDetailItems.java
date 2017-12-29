/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.R;

/**
 * Quick settings common detail view with line items.
 */
public class QSDetailItems extends FrameLayout {
    private static final String TAG = "QSDetailItems";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    private final Context mContext;
    private final H mHandler = new H();
    private final Adapter mAdapter = new Adapter();

    private String mTag;
    private Callback mCallback;
    private boolean mItemsVisible = true;
    //*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
    private ViewGroup mItemList;
    /*/
    private AutoSizingList mItemList;
    //*/
    private View mEmpty;
    private TextView mEmptyText;
    private ImageView mEmptyIcon;

    private Item[] mItems;

    public QSDetailItems(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTag = TAG;
    }

    public static QSDetailItems convertOrInflate(Context context, View convert, ViewGroup parent) {
        if (convert instanceof QSDetailItems) {
            return (QSDetailItems) convert;
        }
        //*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
        return (QSDetailItems) LayoutInflater.from(context).inflate(R.layout.freeme_qs_detail_items, parent, false);
        /*/
        return (QSDetailItems) LayoutInflater.from(context).inflate(R.layout.qs_detail_items,
                parent, false);
        //*/
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
        mItemList = (ViewGroup) findViewById(android.R.id.list);
        /*/
        mItemList = (AutoSizingList) findViewById(android.R.id.list);
        //*/
        mItemList.setVisibility(GONE);
        /*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
        mItemList.setAdapter(mAdapter);
        //*/
        mEmpty = findViewById(android.R.id.empty);
        mEmpty.setVisibility(GONE);
        mEmptyText = (TextView) mEmpty.findViewById(android.R.id.title);
        mEmptyIcon = (ImageView) mEmpty.findViewById(android.R.id.icon);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FontSizeUtils.updateFontSize(mEmptyText, R.dimen.qs_detail_empty_text_size);
        int count = mItemList.getChildCount();
        for (int i = 0; i < count; i++) {
            View item = mItemList.getChildAt(i);
            FontSizeUtils.updateFontSize(item, android.R.id.title,
                    R.dimen.qs_detail_item_primary_text_size);
            FontSizeUtils.updateFontSize(item, android.R.id.summary,
                    R.dimen.qs_detail_item_secondary_text_size);
        }
    }

    public void setTagSuffix(String suffix) {
        mTag = TAG + "." + suffix;
    }

    public void setEmptyState(int icon, int text) {
        mEmptyIcon.setImageResource(icon);
        mEmptyText.setText(text);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) Log.d(mTag, "onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) Log.d(mTag, "onDetachedFromWindow");
        mCallback = null;
    }

    public void setCallback(Callback callback) {
        mHandler.removeMessages(H.SET_CALLBACK);
        mHandler.obtainMessage(H.SET_CALLBACK, callback).sendToTarget();
    }

    public void setItems(Item[] items) {
        mHandler.removeMessages(H.SET_ITEMS);
        mHandler.obtainMessage(H.SET_ITEMS, items).sendToTarget();
    }

    public void setItemsVisible(boolean visible) {
        mHandler.removeMessages(H.SET_ITEMS_VISIBLE);
        mHandler.obtainMessage(H.SET_ITEMS_VISIBLE, visible ? 1 : 0, 0).sendToTarget();
    }

    private void handleSetCallback(Callback callback) {
        mCallback = callback;
    }

    private void handleSetItems(Item[] items) {
        final int itemCount = items != null ? items.length : 0;
        mEmpty.setVisibility(itemCount == 0 ? VISIBLE : GONE);
        mItemList.setVisibility(itemCount == 0 ? GONE : VISIBLE);
        mItems = items;
        //*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
        for (int i = 0; i < itemCount; i++) {
            View childAt = i < mItemList.getChildCount() ? mItemList.getChildAt(i) : null;
            View newView = mAdapter.getView(i, childAt, mItemList);
            if (newView != childAt) {
                if (childAt != null) {
                    mItemList.removeView(childAt);
                }
                mItemList.addView(newView, i);
            }
        }
        while (mItemList.getChildCount() > itemCount) {
            mItemList.removeViewAt(mItemList.getChildCount() - 1);
        }
        /*/
        mAdapter.notifyDataSetChanged();
        //*/
    }

    private void handleSetItemsVisible(boolean visible) {
        if (mItemsVisible == visible) return;
        mItemsVisible = visible;
        for (int i = 0; i < mItemList.getChildCount(); i++) {
            mItemList.getChildAt(i).setVisibility(mItemsVisible ? VISIBLE : INVISIBLE);
        }
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItems != null ? mItems.length : 0;
        }

        @Override
        public Object getItem(int position) {
            return mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            final Item item = mItems[position];
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.qs_detail_item, parent,
                        false);
            }
            view.setVisibility(mItemsVisible ? VISIBLE : INVISIBLE);
            final ImageView iv = (ImageView) view.findViewById(android.R.id.icon);
            iv.setImageResource(item.icon);
            iv.getOverlay().clear();
            if (item.overlay != null) {
                item.overlay.setBounds(0, 0, item.overlay.getIntrinsicWidth(),
                        item.overlay.getIntrinsicHeight());
                iv.getOverlay().add(item.overlay);
            }
            final TextView title = (TextView) view.findViewById(android.R.id.title);
            title.setText(item.line1);
            final TextView summary = (TextView) view.findViewById(android.R.id.summary);
            final boolean twoLines = !TextUtils.isEmpty(item.line2);
            title.setMaxLines(twoLines ? 1 : 2);
            summary.setVisibility(twoLines ? VISIBLE : GONE);
            summary.setText(twoLines ? item.line2 : null);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onDetailItemClick(item);
                    }
                }
            });
            final ImageView disconnect = (ImageView) view.findViewById(android.R.id.icon2);
            disconnect.setVisibility(item.canDisconnect ? VISIBLE : GONE);
            disconnect.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onDetailItemDisconnect(item);
                    }
                }
            });
            return view;
        }
    };

    private class H extends Handler {
        private static final int SET_ITEMS = 1;
        private static final int SET_CALLBACK = 2;
        private static final int SET_ITEMS_VISIBLE = 3;

        public H() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SET_ITEMS) {
                handleSetItems((Item[]) msg.obj);
            } else if (msg.what == SET_CALLBACK) {
                handleSetCallback((QSDetailItems.Callback) msg.obj);
            } else if (msg.what == SET_ITEMS_VISIBLE) {
                handleSetItemsVisible(msg.arg1 != 0);
            }
        }
    }

    public static class Item {
        public int icon;
        public Drawable overlay;
        public CharSequence line1;
        public CharSequence line2;
        public Object tag;
        public boolean canDisconnect;
    }

    public interface Callback {
        void onDetailItemClick(Item item);
        void onDetailItemDisconnect(Item item);
    }
}
