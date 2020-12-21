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

#define RGBToLuma(red, green, blue)  (0.2126f * (red) + 0.7152f * (green) + 0.0722f * (blue))
#define coordsToIndex(width, i, j)  ((i) * (width) + (j))
#define min(a, b)  ((a) < (b) ? (a) : (b))

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
    int bb;

    for (int i = 0; i < size; ++i) {
        // Alpha is ignored, it should always be 0xff
        bb = (int) *(b + (i << 2) + 1);
        int red = (bb >> 8) & 0xff;
        bb = (int) *(b + (i << 2) + 2);
        int green = (bb >> 8) & 0xff;
        bb = (int) *(b + (i << 2) + 3);
        int blue = (bb >> 8) & 0xff;

        bb = RGBToLuma(red, green, blue);

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
                                                                             jintArray blackLevelArray) {
    long int size = width * height;
    jbyte *b = (*env)->GetByteArrayElements(env, bytes, JNI_FALSE);
    jbyte *address = b;
    jbyte *bA = (*env)->GetIntArrayElements(env, blackLevelArray, JNI_FALSE);
    jbyte *bAAddress = bA;
    long int sum = 0;
    long int max = 0;
    long int maxIndex = 0;
    long int blacks = 0;
    long int bb;

    blackThreshold = blackThreshold * (whiteLevel - (bA[0] + bA[1] + bA[2] + bA[3]) / 4.) / 255;

    if (colorFilterArrangement < 4) {
        for (int i = 0; i < height; i += 2) {
            for (int j = 0; j < width; j += 2) {
                long red, green, blue;
                long pixel[] = {
                        *((long *)(b + (coordsToIndex(width, i, j) << 1))),
                        *((long *)(b + (coordsToIndex(width, i, j + 1) << 1))),
                        *((long *)(b + (coordsToIndex(width, i + 1, j) << 1))),
                        *((long *)(b + (coordsToIndex(width, i + 1, j + 1) << 1)))
                };

                for (int k = 0; k < 4; ++k) {
                    pixel[k] = ((pixel[k] >> 16) & 0xffff);
                    pixel[k] = pixel[k] - bA[k];
                }

                switch (colorFilterArrangement) {
                    case 0: // RGGB
                        red = pixel[0];
                        green = (pixel[1] + pixel[2]) >> 1;
                        blue = pixel[3];
                        break;

                    case 1: // GRBG
                        red = pixel[1];
                        green = (pixel[0] + pixel[3]) >> 1;
                        blue = pixel[2];
                        break;

                    case 2: // GBRG
                        red = pixel[2];
                        green = (pixel[0] + pixel[3]) >> 1;
                        blue = pixel[1];
                        break;

                    case 3: // BGGR
                        red = pixel[3];
                        green = (pixel[1] + pixel[2]) >> 1;
                        blue = pixel[0];
                        break;
                }

                red = min(red, whiteLevel);
                green = min(green, whiteLevel);
                blue = min(blue, whiteLevel);

                bb = RGBToLuma(red, green, blue);

                if (bb > 0) {
                    sum += bb << 2;
                    if (bb > max) {
                        max = bb;
                        maxIndex = coordsToIndex(width, i, j);
                    }
                }

                if (bb < blackThreshold) {
                    blacks += 4;
                }
            }
        }
    } else if (colorFilterArrangement == 4) { // RGB (untested)
        for (int i = 0; i < size; ++i) {
            bb = *((long *)(b + (i * 6)));
            long red = (bb >> 16) & 0xffff;
            bb = *((long *)(b + (i * 6) + 2));
            long green = (bb >> 16) & 0xffff;
            bb = *((long *)(b + (i * 6) + 4));
            long blue = (bb >> 16) & 0xffff;

            bb = RGBToLuma(red, green, blue);

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
    } else { // MONO or NIR (untested)
        for (int i = 0; i < size; ++i) {
            bb = *((long *)(b + (i << 1)));
            bb = ((bb >> 16) & 0xffff) - bA[0];

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
    }

    (*env)->ReleaseByteArrayElements(env, bytes, address, 0);
    (*env)->ReleaseIntArrayElements(env, blackLevelArray, bAAddress, 0);
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

