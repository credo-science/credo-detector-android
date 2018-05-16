//
// Created by Piotr Poznanski on 14/10/2017.
//

#include <jni.h>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif

//science.credo.mobiledetector.detection.CameraPreviewCallbackNative
//external fun calcHistogram (data: ByteArray, analysisData: LongArray, width: Int, height: Int)

JNIEXPORT void JNICALL
Java_science_credo_mobiledetector_detection_CameraPreviewCallbackNative_calcHistogram (
        JNIEnv *env,
        jobject /* this */,
        jbyteArray data,
        jlongArray analysisData,
        jint width,
        jint height,
        jint black) {

    jboolean isCopy;
    char *bufferPtr = (char*) env->GetPrimitiveArrayCritical(data, &isCopy);
    jsize bufferLength = env->GetArrayLength(data);
    jlong *analysisDataPtr = (jlong*) env->GetPrimitiveArrayCritical(analysisData, &isCopy);
    jsize analysisDataLength = env->GetArrayLength(analysisData);

    int histoLength = analysisDataLength - 4;
    int slotSize = 255/histoLength;

    long maxIndex = 0;
    char max = 0;
    char val = 0;
    long sum = 0;
    long zeros = 0;

    for (int i = 0; i < histoLength; i++) analysisDataPtr[i] = 0;
    for (int i = 0; i < width*height; i++) {
        val = bufferPtr[i];
//         with Histogram calculation version
         if(val > 0) {
            sum += val;
            if (val > max) {
                max = val;
                maxIndex = i;
            }
            analysisDataPtr[val / slotSize]++;
        }
        if (val <= black) {
            zeros++;
        }
//        no histogram calculatio version
 /*       sum += val;
        if (val > max) {
            max = val;
            maxIndex = i;
        }*/
    }

    analysisDataPtr[analysisDataLength-1] = (jlong) max;
    analysisDataPtr[analysisDataLength-2] = (jlong) maxIndex;
    analysisDataPtr[analysisDataLength-3] = (jlong) sum;
    analysisDataPtr[analysisDataLength-4] = (jlong) zeros;

    env->ReleasePrimitiveArrayCritical(data, bufferPtr, 0);
    env->ReleasePrimitiveArrayCritical(analysisData, analysisDataPtr, 0);
}


#ifdef __cplusplus
}
#endif