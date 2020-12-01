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

