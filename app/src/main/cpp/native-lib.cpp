#include <jni.h>
#include <string>
#include <android/log.h>
#include <time.h>

#ifdef __cplusplus
extern "C" {
#endif

float* calibrationAvg = 0;
int* calibrationMin = 0;
int* calibrationMax = 0;
int frame = 0;
int stage = 0;
float* calibrationFullAvg = 0;
float* calibrationFullAvgMin = 0;
float* calibrationFullAvgMax = 0;
float* calibrationFullMin = 0;
float* calibrationFullMax = 0;
int width = 0;
int height = 0;

static double now_ms(void) {

    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;

}

void clear() {
    if (calibrationAvg) delete[] calibrationAvg;
    if (calibrationMin) delete[] calibrationMin;
    if (calibrationMax) delete[] calibrationMax;

    if (calibrationFullAvg) delete[] calibrationFullAvg;
    if (calibrationFullAvgMin) delete[] calibrationFullAvgMin;
    if (calibrationFullAvgMax) delete[] calibrationFullAvgMax;
    if (calibrationFullMin) delete[] calibrationFullMin;
    if (calibrationFullMax) delete[] calibrationFullMax;

    calibrationAvg = 0;
    calibrationMin = 0;
    calibrationMax = 0;
    calibrationFullAvg = 0;
    calibrationFullAvgMin = 0;
    calibrationFullAvgMax = 0;
    calibrationFullMin = 0;
    calibrationFullMax = 0;

    frame = 0;
    stage = 0;
}

JNIEXPORT int JNICALL
Java_science_credo_mobiledetector_detection_CameraPreviewAlgorithm2_analysing (
        JNIEnv* env,
        jobject thiz,
        jint width,
        jint height,
        jbyteArray NV21FrameData
) {

    double start = now_ms();
    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "Start");

    jboolean isCopy;
    char *bufferPtr = (char*) env->GetPrimitiveArrayCritical(NV21FrameData, &isCopy);
    jsize bufferLength = env->GetArrayLength(NV21FrameData);

    if (::width != width || ::height != height) {
        ::width = width;
        ::height = height;
        clear();
    }

    if (frame == 0) {
        calibrationAvg = new float[width * height];
        calibrationMin = new int[width * height];
        calibrationMax = new int[width * height];

        float* pavg = calibrationAvg;
        int* pmin = calibrationMin;
        int* pmax = calibrationMax;
        char* ppixel = bufferPtr;

        for (int i = 0; i < width*height; i++) {
            char pixel = *(ppixel++);
            *(pavg++) = pixel;
            *(pmin++) = pixel;
            *(pmax++) = pixel;
        }
    } else {
        float* pavg = calibrationAvg;
        int* pmin = calibrationMin;
        int* pmax = calibrationMax;
        char* ppixel = bufferPtr;

        for (int i = 0; i < width*height; i++) {
            char pixel = *(ppixel++);
            float avg = *pavg;
            *pavg = (avg * frame + pixel) / (frame + 1);
            if (*pmin > pixel) {
                *pmin = pixel;
            }
            if (*pmax < pixel) {
                *pmax = pixel;
            }
            pavg++;
            pmin++;
            pmax++;
        }
    }
    frame++;

    env->ReleasePrimitiveArrayCritical(NV21FrameData, bufferPtr, 0);
    double end = now_ms();
    __android_log_print(ANDROID_LOG_DEBUG, "JNI", "Finish: %d", int(end - start));
    return frame;
}


union RGBA
{
    jint dword;
    unsigned char RGBA[4];
    struct RGBAstruct
    {
        unsigned char b;
        unsigned char g;
        unsigned char r;
        unsigned char a;
    };
};


int to_rgba(u_char pixel)  {
    RGBA rgb;
    rgb.RGBA[0] = pixel;
    rgb.RGBA[1] = pixel;
    rgb.RGBA[2] = pixel;
    rgb.RGBA[3] = 0xff;
    return rgb.dword;
}


JNIEXPORT void JNICALL
Java_science_credo_mobiledetector_detection_CameraPreviewAlgorithm2_export (
        JNIEnv* env,
        jobject thiz,
        jintArray outPixels,
        jint file
) {
    jboolean isCopy;
    int *bufferPtr = (int*) env->GetPrimitiveArrayCritical(outPixels, &isCopy);
    switch (file) {
        case 0:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationAvg[i]);
            }
            break;

        case 1:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationMin[i]);
            }
            break;

        case 2:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationMax[i]);
            }
            break;

        case 3:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullAvg[i]);
            }
            break;

        case 4:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullAvgMin[i]);
            }
            break;

        case 5:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullAvgMax[i]);
            }
            break;

        case 6:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullMin[i]);
            }
            break;

        case 7:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullMax[i]);
            }
            break;

        case 8:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationMax[i] - (u_char)calibrationMin[i]);
            }
            break;

        case 9:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullAvgMax[i] - (u_char)calibrationFullAvgMin[i]);
            }
            break;

        case 10:
            for (int i = 0; i < width*height; i++) {
                bufferPtr[i] = to_rgba((u_char)calibrationFullMax[i] - (u_char)calibrationFullMin[i]);
            }
            break;

    }
    env->ReleasePrimitiveArrayCritical(outPixels, bufferPtr, 0);
}

JNIEXPORT void JNICALL
Java_science_credo_mobiledetector_detection_CameraPreviewAlgorithm2_clear (
        JNIEnv* env,
        jobject thiz
) {
    clear();
}

JNIEXPORT void JNICALL
Java_science_credo_mobiledetector_detection_CameraPreviewAlgorithm2_finishStage (
        JNIEnv* env,
        jobject thiz
) {
    if (stage == 0) {
        calibrationFullAvg = new float[width * height];
        calibrationFullAvgMin = new float[width * height];
        calibrationFullAvgMax = new float[width * height];
        calibrationFullMin = new float[width * height];
        calibrationFullMax = new float[width * height];

        for (int i = 0; i < width * height; i++) {
            calibrationFullAvg[i] = calibrationAvg[i];
            calibrationFullAvgMin[i] = calibrationAvg[i];
            calibrationFullAvgMax[i] = calibrationAvg[i];
            calibrationFullMin[i] = calibrationMin[i];
            calibrationFullMax[i] = calibrationMax[i];
        }
    } else {
        for (int i = 0; i < width * height; i++) {
            calibrationFullAvg[i] = (calibrationFullAvg[i] * stage + calibrationAvg[i]) / (stage + 1);
            if (calibrationFullAvgMin[i] > calibrationAvg[i]) calibrationFullAvgMin[i] = calibrationAvg[i];
            if (calibrationFullAvgMax[i] < calibrationAvg[i]) calibrationFullAvgMax[i] = calibrationAvg[i];
            calibrationFullMin[i] = (calibrationFullMin[i] * stage + calibrationMin[i]) / (stage + 1);
            calibrationFullMax[i] = (calibrationFullMax[i] * stage + calibrationMax[i]) / (stage + 1);
        }
    }

    delete[] calibrationAvg;
    delete[] calibrationMin;
    delete[] calibrationMax;

    calibrationAvg = 0;
    calibrationMin = 0;
    calibrationMax = 0;

    frame = 0;
    stage++;
}

#ifdef __cplusplus
}
#endif
