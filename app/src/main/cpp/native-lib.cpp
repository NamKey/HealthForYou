#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
using namespace std;
using namespace cv;

extern "C"{
    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_Fragment_1meas_redDetection(JNIEnv *env, jobject instance,jlong matAddrInput,jlong matAddrResult) {

        int sum=0;
        Mat &matInput = *(Mat *) matAddrInput;
        Mat &matResult = * (Mat *)matAddrResult;

        cvtColor(matInput,matResult,CV_RGB2BGR);
        for(int i=0;i<matResult.rows;i++)
        {
            for(int j=0;j<matResult.cols;j++)
            {
                //sum+=matInput.at<cv::Vec3b>(i,j)[2];//Red
                sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green
                //sum+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
            }
        }
        // TODO
        return sum;// TODO
    }
}

extern "C"{
    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_Fragment_1meas_moveDetection(JNIEnv *env, jobject instance, jlong previous, jlong current) {

        Mat &previous_frame = *(Mat *) previous;
        Mat &current_frame = *(Mat *) current;
        Mat differ = previous_frame;
        cvtColor(previous_frame,previous_frame,CV_RGB2GRAY);
        cvtColor(current_frame,current_frame,CV_RGB2GRAY);
        absdiff(previous_frame,current_frame,differ);
        threshold(differ,differ,50,255,THRESH_BINARY);
        int cnt=0;
        for(int i=0;i<differ.rows;i++)
        {
            for(int j=0;j<differ.cols;j++)
            {
                if(differ.at<int>(i,j)>50)
                {
                    cnt++;
                }
            }
        }
        return cnt;// TODO
    }
}