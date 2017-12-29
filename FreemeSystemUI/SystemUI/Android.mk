LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := Freeme-SystemUI-proto-tags

LOCAL_SRC_FILES := $(call all-proto-files-under,src) \
    src/com/android/systemui/EventLogTags.logtags

LOCAL_PROTOC_OPTIMIZE_TYPE := nano
LOCAL_PROTO_JAVA_OUTPUT_PARAMS := optional_field_style=accessors

include $(BUILD_STATIC_JAVA_LIBRARY)

# ------------------

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) $(call all-Iaidl-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.mediatek.systemui.ext \
    FreemeKeyguard \
    android-support-v7-recyclerview \
    android-support-v7-preference \
    android-support-v7-appcompat \
    android-support-v14-preference \
    android-support-v17-leanback \
    framework-protos \
    SystemUI-proto-tags

ifneq ($(FREEME_SYSTEMUI_EXT_BUILD_LIBYV12UTIL), yes)
    LOCAL_JNI_SHARED_LIBRARIES := libyv12util
endif

LOCAL_JAVA_LIBRARIES := telephony-common
LOCAL_JAVA_LIBRARIES += mediatek-framework
LOCAL_JAVA_LIBRARIES += ims-common

LOCAL_PACKAGE_NAME := FreemeSystemUI
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_OVERRIDES_PACKAGES := SystemUI

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_RESOURCE_DIR := \
    $(LOCAL_PATH)/../Keyguard/res \
    $(LOCAL_PATH)/../Keyguard/res_ext \
    $(LOCAL_PATH)/res \
    $(LOCAL_PATH)/res_ext \
    frameworks/support/v7/preference/res \
    frameworks/support/v14/preference/res \
    frameworks/support/v7/appcompat/res \
    frameworks/support/v7/recyclerview/res \
    frameworks/support/v17/leanback/res

LOCAL_AAPT_FLAGS := --auto-add-overlay \
    --extra-packages com.android.keyguard:android.support.v7.recyclerview:android.support.v7.preference:android.support.v14.preference:android.support.v7.appcompat \
    --extra-packages android.support.v17.leanback

ifneq ($(SYSTEM_UI_INCREMENTAL_BUILDS),)
    LOCAL_PROGUARD_ENABLED := disabled
    LOCAL_JACK_ENABLED := incremental
endif

# @{ freeme.gouzhouping, 201712056. Freeme.
include $(LOCAL_PATH)/freeme/common_freeme.mk
# @}

# @{ freeme,chenming. 20171218. Faceprint
LOCAL_STATIC_JAVA_AAR_LIBRARIES := faceprint-authenui-library
LOCAL_AAPT_FLAGS += --extra-packages com.freeme.face.ui
# @}

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

ifeq ($(EXCLUDE_SYSTEMUI_TESTS),)
    include $(call all-makefiles-under,$(LOCAL_PATH))
endif
