/*
 * Copyright (C) 2018 The Android Open Source Project
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
 *
 */
#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <malloc.h>

/* This is a trivial JNI example where we use a native method
 * to return a new VM String. See the corresponding Kotlin source
 * file located at:
 *
 *   app/src/main/java/com/example/kotlin/KotlinJni.kt
 */
JNIEXPORT jstring JNICALL
Java_science_credo_mobiledetector2_detector_old_JniWrapper_calculateOldFrame(JNIEnv *env,
                                                                         jobject thiz,
                                                                         jbyteArray bytes,
                                                                         jint width,
                                                                         jint height,
                                                                         jint blackThreshold) {

    int size = width * height;
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
    jbyte *address = b;
    int sum = 0;
    int max = 0;
    int maxIndex = 0;
    int blacks = 0;
    for (int i = 0; i < size; ++i) {
        int bb = (int) *b++;
        bb = bb & 0xff;
        if (bb > 0) {
            sum += bb;
            if (bb > max) {
                max = bb;
                maxIndex = i;
            }
        }
        if (bb < blackThreshold) {
            ++blacks;
        }
    }
    (*env)->ReleaseByteArrayElements(env,bytes, address, 0);
    char buffer[100];
    sprintf(buffer, "%d;%d;%d;%d;%d", sum / size, blacks,size, max, maxIndex);
    jstring result = (*env)->NewStringUTF(env, buffer);
    return result;

}

JNIEXPORT jstring JNICALL
Java_science_credo_mobiledetector2_detector_old_JniWrapper_calculateRGBFrame(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jintArray pixels,
                                                                             jint width,
                                                                             jint height,
                                                                             jint blackThreshold) {

    int size = width * height;
    jint *b = (*env)->GetIntArrayElements(env, pixels, JNI_FALSE);
    jint *address = b;
    int sum = 0;
    int max = 0;
    int maxIndex = 0;
    int blacks = 0;
    for (int i = 0; i < size; ++i) {
        int bb = *b++;

        // Alpha is ignored, it should always be 0xff
        int red = (bb >> 16) & 0xff;
        int green = (bb >> 8) & 0xff;
        int blue = bb & 0xff;

        // Calculate luma
        bb = (0.2126f * red + 0.7152f * green + 0.0722f * blue);

        if (bb > 0) {
            sum += bb;
            if (bb > max) {
                max = bb;
                maxIndex = i;
            }
        }
        if (bb < blackThreshold) {
            ++blacks;
        }
    }
    (*env)->ReleaseIntArrayElements(env, pixels, address, 0);
    char buffer[100];
    sprintf(buffer, "%d;%d;%d;%d;%d", sum / size, blacks, size, max, maxIndex);
    jstring result = (*env)->NewStringUTF(env, buffer);
    return result;

}

JNIEXPORT jstring JNICALL
Java_science_credo_mobiledetector2_detector_old_JniWrapper_calculateRawSensorFrame(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jbyteArray bytes,
                                                                             jint width,
                                                                             jint height,
                                                                             jint blackThreshold,
                                                                             jint colorFilterArrangement,
                                                                             jint whiteLevel,
                                                                             jintArray blackLevelArray,
                                                                             jbooleanArray hotPixels) {
    long int size = width * height;
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
    jbyte *address = b;
    jbyte *bA = (*env)->GetIntArrayElements(env, blackLevelArray, JNI_FALSE);
    jbyte *bAAddress = bA;
    jbyte *hP = (*env)->GetBooleanArrayElements(env, hotPixels, JNI_FALSE);
    jbyte *hPAddress = hP;
    long int sum = 0;
    long int max = 0;
    long int maxIndex = 0;
    long int blacks = 0;
    char sensorColour;

    for (int i = 0; i < size; ++i) {
        int x = i % width;
        int y = i / width;

        if (y % 2) {
            sensorColour = x % 2 + 2;
        } else {
            sensorColour = x % 2;
        }

        long int bb = *b;
        bb = (bb & 0xffff);
        bb = bb > bA[sensorColour] ? bb - bA[sensorColour] : 0;

        if (bb > 0) {
            long int ceiledBb = bb < whiteLevel ? bb : whiteLevel;

            if (ceiledBb < whiteLevel) {
                hP[i] = 0;
            } else if (hP[i]) {
                continue;
            } else {
                hP[i] = 1;
            }

            sum += ceiledBb;

            if (ceiledBb > max) {
                max = ceiledBb;
                maxIndex = i;
            }
        }

        if (bb < blackThreshold) {
            ++blacks;
        }

        b += 2;
    }

    (*env)->ReleaseByteArrayElements(env, bytes, address, 0);
    (*env)->ReleaseIntArrayElements(env, blackLevelArray, bAAddress, 0);
    (*env)->ReleaseBooleanArrayElements(env, hotPixels, hPAddress, 0);
    char buffer[100];
    sprintf(buffer, "%ld;%ld;%ld;%ld;%ld", sum / size, blacks, size, max, maxIndex);
    jstring result = (*env)->NewStringUTF(env, buffer);
    return result;
}

JNIEXPORT jstring JNICALL
Java_science_credo_mobiledetector2_detector_old_JniWrapper_calculateRawFrame(JNIEnv *env,
                                                                            jobject thiz,
                                                                            jbyteArray bytes,
                                                                            jint width,
                                                                            jint height,
                                                                            jint scaledWidth,
                                                                            jint scaledHeight,
                                                                            jint pixelPrecision) {


    int sum = 0;
    int max = 0;
    int maxIndex = 0;

    int scaleFactorWidth = width / scaledWidth;
    int scaleFactorHeight = height / scaledHeight;
    int scale = scaleFactorWidth * scaleFactorHeight;
    int scaledFrameSize = scaledWidth*scaledHeight;


    int * scaledFrame= (int *)(malloc((sizeof(int))*scaledFrameSize));
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
    jbyte *address = b;
    for (int r = 0; r < height; ++r) {
        int indexRow = r * width * pixelPrecision;
        int scaledIndexRow = r / scaleFactorHeight * scaledWidth;
        int c = 0;
        while (c < width * pixelPrecision) {
            int index = indexRow + c;
            int resultIndex = scaledIndexRow + c / pixelPrecision / scaleFactorWidth;
            int byteValue = *(b+index) & 0xff;
            scaledFrame[resultIndex]= scaledFrame[resultIndex]+byteValue;
            c += pixelPrecision;
        }
    }

    for (int i =0; i<scaledFrameSize;++i){
        int virtualPixelValue = scaledFrame[i] / scale;
        sum+=virtualPixelValue;
        if (virtualPixelValue > max) {
            max = virtualPixelValue;
            maxIndex = i;
        }
    }
    (*env)->ReleaseByteArrayElements(env,bytes, address, 0);
    char buffer[100];
    sprintf(buffer, "%d;%d;%d", sum / scaledFrameSize, max, maxIndex);
    jstring result = (*env)->NewStringUTF(env, buffer);
    free(scaledFrame);
    return result;

}

