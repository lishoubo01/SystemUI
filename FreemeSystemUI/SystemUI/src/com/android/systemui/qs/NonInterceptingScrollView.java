/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * ScrollView that disallows intercepting for touches that can cause scrolling.
 */
public class NonInterceptingScrollView extends ScrollView {

    public NonInterceptingScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //*/ freeme.zhongkai.zhu. 20170830. optimize detail scroll
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(event);
    }
    /*/
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (canScrollVertically(1)) {
                    requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.onTouchEvent(ev);
    }
    //*/
}
