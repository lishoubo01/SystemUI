# Freeme for SystemUI

LOCAL_SRC_FILES += \
    $(call all-java-files-under, freeme/src)

LOCAL_RESOURCE_DIR += \
    $(LOCAL_PATH)/freeme/res \

LOCAL_FULL_LIBS_MANIFEST_FILES += \
    $(LOCAL_PATH)/freeme/AndroidManifest.xml