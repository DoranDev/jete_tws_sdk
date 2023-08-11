#include <jni.h>
#include <cassert>
#include <pthread.h>

#include "picosha2.h"
#include "aes.hpp"

#include "ab_log.h"

#define JNI_MODULE_PACKAGE  "com/bluetrum/abmate/auth"
#define JNI_CLASS_NAME      "com/bluetrum/abmate/auth/ABAuthenticator"

#ifndef NELEM
#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

static JavaVM* g_jvm;

typedef struct fields_t {
    pthread_mutex_t mutex;
    jclass clazz;
} fields_t;
static fields_t g_clazz;

// ----------------------------------------------------------------------------

static jbyteArray get6(JNIEnv *env, jobject instance)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID random_fid = env->GetFieldID(cls, "r", "[B");
    return (jbyteArray) env->GetObjectField(instance, random_fid);
}

// ----------------------------------------------------------------------------

static jbyteArray getSK(JNIEnv *env, jobject instance)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID sk_fid = env->GetFieldID(cls, "sk", "[B");
    return (jbyteArray) env->GetObjectField(instance, sk_fid);
}

static jbyteArray getR(JNIEnv *env, jobject instance)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID random_fid = env->GetFieldID(cls, "r", "[B");
    return (jbyteArray) env->GetObjectField(instance, random_fid);
}

static void setR(JNIEnv *env, jobject instance, jbyteArray random)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID random_fid = env->GetFieldID(cls, "r", "[B");
    env->SetObjectField(instance, random_fid, random);
}

static jbyteArray getRP(JNIEnv *env, jobject instance)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID random_fid = env->GetFieldID(cls, "rp", "[B");
    return (jbyteArray) env->GetObjectField(instance, random_fid);
}

static void setRP(JNIEnv *env, jobject instance, jbyteArray random_prime)
{
    jclass cls = env->GetObjectClass(instance);
    jfieldID random_prime_fid = env->GetFieldID(cls, "rp", "[B");
    env->SetObjectField(instance, random_prime_fid, random_prime);
}

// ----------------------------------------------------------------------------

/**
 * 使用蓝牙经典地址生成Secret Key并返回。
 * 蓝牙地址填充前6字节，后10字节使用0x80填充，并进行SHA256运算，取前16字节
 */
static jbyteArray getSecretKey(JNIEnv *env, jobject instance,
                               jbyteArray btAddress)
{
    jsize addr_len = env->GetArrayLength(btAddress);
    jbyte *addr_bytes = env->GetByteArrayElements(btAddress, nullptr);

    std::vector<unsigned char> message;
    // 地址放在前面
    message.assign(addr_bytes, addr_bytes + addr_len);
    // 后面填充0x80
    message.insert(message.end(), 10, 0x80);

    env->ReleaseByteArrayElements(btAddress, addr_bytes, 0);

    // SHA256
    unsigned char digest[picosha2::k_digest_size];
    picosha2::hash256(message, digest, digest + picosha2::k_digest_size);

    // 取前16字节作为Secret Key，返回到Java层
    jbyteArray secretKey = env->NewByteArray(16);
    env->SetByteArrayRegion(secretKey, 0, 16, reinterpret_cast<const jbyte *>(digest));

    return secretKey;
}

/**
 * 生成随机数填充random，用key加密random并返回
 */
static jbyteArray getRandom(JNIEnv *env, jobject instance)
{
    jbyteArray key = getSK(env, instance);
    // r = new byte[16];
    jbyteArray random = env->NewByteArray(16);

    // 调用SecureRandom生成随机数
    jclass secureRandom_cls = env->FindClass("java/security/SecureRandom");
    jmethodID secureRandom_constructor_mid = env->GetMethodID(secureRandom_cls, "<init>", "()V");
    jobject secureRandom = env->NewObject(secureRandom_cls, secureRandom_constructor_mid);
    jmethodID nextBytes_mid = env->GetMethodID(secureRandom_cls, "nextBytes", "([B)V");
    env->CallVoidMethod(secureRandom, nextBytes_mid, random);

    setR(env, instance, random);

    jbyte *random_array = env->GetByteArrayElements(random, nullptr);
    jbyte *key_array = env->GetByteArrayElements(key, nullptr);

    // 加密随机数
    struct AES_ctx ctx{};
    AES_init_ctx(&ctx, reinterpret_cast<const uint8_t *>(key_array));

    unsigned char plain[16];
    memcpy(plain, random_array, 16);

    AES_ECB_encrypt(&ctx, plain);

    env->ReleaseByteArrayElements(random, random_array, 0);
    env->ReleaseByteArrayElements(key, key_array, 0);

    // 返回到Java层
    jbyteArray challenge = env->NewByteArray(16);
    env->SetByteArrayRegion(challenge, 0, 16, reinterpret_cast<const jbyte *>(plain));

    return challenge;
}

/**
 * 校验设备回复信息.
 * 使用Secret Key解密（解密后数据由Java层保存作为R'），前8字节与random的前后8字节异或值对比，并返回结果
 */
static jboolean checkResponse(JNIEnv *env, jobject instance,
                                jbyteArray response)
{
    jbyteArray random = getR(env, instance);
    jbyteArray key = getSK(env, instance);

    jbyte *random_array = env->GetByteArrayElements(random, nullptr);
    jbyte *response_array = env->GetByteArrayElements(response, nullptr);
    jbyte *key_array = env->GetByteArrayElements(key, nullptr);

    // 解密response
    struct AES_ctx ctx{};
    AES_init_ctx(&ctx, reinterpret_cast<const uint8_t *>(key_array));
    AES_ECB_decrypt(&ctx, reinterpret_cast<uint8_t *>(response_array));

    // 前后8字节异或
    unsigned char r1[8];
    for (int i = 0; i < 8; i++) {
        r1[i] = (unsigned char)random_array[i] ^ (unsigned char)random_array[i + 8];
    }

    // 对比前8个字节
    int result = memcmp(r1, response_array, 8);

    env->ReleaseByteArrayElements(random, random_array, 0);
    env->ReleaseByteArrayElements(key, key_array, 0);

    // 返回response到Java层
    env->ReleaseByteArrayElements(response, response_array, 0);

    // rp = response
    setRP(env, instance, response);

    return result == 0;
}

/**
 * 生成Challenge。
 * 将R'取反码得到R''，并返回。
 */
static jbyteArray getChallenge(JNIEnv *env, jobject instance)
{
    jbyteArray rp = getRP(env, instance);
    jbyteArray key = getSK(env, instance);

    jbyte *rp_array = env->GetByteArrayElements(rp, nullptr);
    jbyte *key_array = env->GetByteArrayElements(key, nullptr);

    unsigned char rpp_array[16];
    // 将R'取反
    for (int i = 0; i < 16; i++) {
        rpp_array[i] = ~(unsigned char)rp_array[i];
    }

    // 加密R''
    struct AES_ctx ctx{};
    AES_init_ctx(&ctx, reinterpret_cast<const uint8_t *>(key_array));
    AES_ECB_encrypt(&ctx, rpp_array);

    env->ReleaseByteArrayElements(rp, rp_array, 0);
    env->ReleaseByteArrayElements(key, key_array, 0);

    // 返回到Java层
    jbyteArray rpp = env->NewByteArray(16);
    env->SetByteArrayRegion(rpp, 0, 16, reinterpret_cast<const jbyte *>(rpp_array));

    return rpp;
}

/**
 * 生成Beacon Key，并返回。
 */
static jbyteArray getBeaconKey(JNIEnv *env, jobject instance,
                               jbyteArray rp)
{
    jbyteArray key = getSK(env, instance);

    jbyte *rp_array = env->GetByteArrayElements(rp, nullptr);
    jbyte *key_array = env->GetByteArrayElements(key, nullptr);

    unsigned char bk_array[16];
    // 异或R'和Secret Key
    for (int i = 0; i < 16; i++) {
        bk_array[i] = (unsigned char)rp_array[i] ^ (unsigned char)key_array[i];
    }

    env->ReleaseByteArrayElements(rp, rp_array, 0);
    env->ReleaseByteArrayElements(key, key_array, 0);

    // 返回到Java层
    jbyteArray bk = env->NewByteArray(16);
    env->SetByteArrayRegion(bk, 0, 16, reinterpret_cast<const jbyte *>(bk_array));

    return bk;
}

/**
 * 解密Beacon Data，并返回。
 * 其实就只是AES128解密。
 */
static jbyteArray decryptBeacon(JNIEnv *env, jobject instance,
                                jbyteArray beacon)
{
    jbyteArray key = getSK(env, instance);

    jbyte *beacon_array = env->GetByteArrayElements(beacon, nullptr);
    jbyte *key_array = env->GetByteArrayElements(key, nullptr);

    // 解密Beacon
    struct AES_ctx ctx{};
    AES_init_ctx(&ctx, reinterpret_cast<const uint8_t *>(key_array));

    unsigned char decrypted_array[16];
    memcpy(decrypted_array, beacon_array, 16);

    AES_ECB_decrypt(&ctx, reinterpret_cast<uint8_t *>(decrypted_array));

    env->ReleaseByteArrayElements(beacon, beacon_array, 0);
    env->ReleaseByteArrayElements(key, key_array, 0);

    // 返回到Java层
    jbyteArray decrypted = env->NewByteArray(16);
    env->SetByteArrayRegion(decrypted, 0, 16, reinterpret_cast<const jbyte *>(decrypted_array));

    return decrypted;
}

// ----------------------------------------------------------------------------

static JNINativeMethod g_methods[] = {

        /* ---------------- 下面方法是旧的，暂时保留 ---------------- */
        { "gSK",           "([B)[B",       (void *) getSecretKey },
        { "gR",            "()[B",         (void *) getRandom },
        { "cR",            "([B)Z",        (void *) checkResponse },
        { "gC",            "()[B",         (void *) getChallenge },
        { "gBK",           "([B)[B",       (void *) getBeaconKey },
        { "dB",            "([B)[B",       (void *) decryptBeacon },
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv* env = nullptr;

    g_jvm = vm;
    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != nullptr);

    pthread_mutex_init(&g_clazz.mutex, nullptr );

    // FindClass returns LocalReference
    jclass clazz = env->FindClass(JNI_CLASS_NAME);
    if (clazz == nullptr) {
        ALOGE("Native registration unable to find class '%s'", JNI_CLASS_NAME);
        return JNI_FALSE;
    }
    g_clazz.clazz = (jclass)env->NewGlobalRef(clazz);

    if (env->RegisterNatives(g_clazz.clazz, g_methods, NELEM(g_methods)) < 0) {
        ALOGE("RegisterNatives failed for '%s'", JNI_CLASS_NAME);
        return JNI_FALSE;
    }

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *jvm, void *reserved)
{
    pthread_mutex_destroy(&g_clazz.mutex);
}
