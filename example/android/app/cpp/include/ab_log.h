#ifndef AB_LOG_H
#define AB_LOG_H

#include <android/log.h>

#define AB_LOG_UNKNOWN     ANDROID_LOG_UNKNOWN
#define AB_LOG_DEFAULT     ANDROID_LOG_DEFAULT

#define AB_LOG_VERBOSE     ANDROID_LOG_VERBOSE
#define AB_LOG_DEBUG       ANDROID_LOG_DEBUG
#define AB_LOG_INFO        ANDROID_LOG_INFO
#define AB_LOG_WARN        ANDROID_LOG_WARN
#define AB_LOG_ERROR       ANDROID_LOG_ERROR
#define AB_LOG_FATAL       ANDROID_LOG_FATAL
#define AB_LOG_SILENT      ANDROID_LOG_SILENT

#define VLOG(level, TAG, ...)    ((void)__android_log_vprint(level, TAG, __VA_ARGS__))
#define ALOG(level, TAG, ...)    ((void)__android_log_print(level, TAG, __VA_ARGS__))

#define AB_LOG_TAG "AB_Native"

#define VLOGV(...)  VLOG(AB_LOG_VERBOSE,   AB_LOG_TAG, __VA_ARGS__)
#define VLOGD(...)  VLOG(AB_LOG_DEBUG,     AB_LOG_TAG, __VA_ARGS__)
#define VLOGI(...)  VLOG(AB_LOG_INFO,      AB_LOG_TAG, __VA_ARGS__)
#define VLOGW(...)  VLOG(AB_LOG_WARN,      AB_LOG_TAG, __VA_ARGS__)
#define VLOGE(...)  VLOG(AB_LOG_ERROR,     AB_LOG_TAG, __VA_ARGS__)

#define ALOGV(...)  ALOG(AB_LOG_VERBOSE,   AB_LOG_TAG, __VA_ARGS__)
#define ALOGD(...)  ALOG(AB_LOG_DEBUG,     AB_LOG_TAG, __VA_ARGS__)
#define ALOGI(...)  ALOG(AB_LOG_INFO,      AB_LOG_TAG, __VA_ARGS__)
#define ALOGW(...)  ALOG(AB_LOG_WARN,      AB_LOG_TAG, __VA_ARGS__)
#define ALOGE(...)  ALOG(AB_LOG_ERROR,     AB_LOG_TAG, __VA_ARGS__)
#define LOG_ALWAYS_FATAL(...)   do { ALOGE(__VA_ARGS__); exit(1); } while (0)

#endif
