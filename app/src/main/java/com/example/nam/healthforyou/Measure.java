package com.example.nam.healthforyou;


import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

public class Measure extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }
    static {
        if(!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private static final String TAG = "opencv";
    //private CameraBridgeViewBase mOpenCvCameraView;
    private javaViewCameraControl mOpenCvCameraView;
    ////카메라 영상 받아오는 부분
    private Mat matInput;
    private Mat matResult;
    ////이전이미지와 현재이미지 비교
    private Mat previous=null;
    private Mat current=null;
    int is_moved;

    ////핸들러 메세지 정의
    static final int is_moving=1;
    static final int no_moving=2;
    TextView message;

    public native int redDetection(long matAddrInput, long matAddrResult);
    public native int moveDetection(long previous,long current);

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);
        //////////////OPENCV
        mOpenCvCameraView = (javaViewCameraControl)findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        //////////////////////측정순서 제어
        Button btn_start = (Button)findViewById(R.id.start_measure);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();
            }
        });
        ///측정의 정확성을 위해서 사용자에게 메세지를 알려줄 핸들러
        message = (TextView)findViewById(R.id.message);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        matInput = inputFrame.rgba();
        /////빨간색을 받아오는 부분
        if ( matResult != null ) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        int sum=redDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());
        /////움직임을 감지하는 부분
        previous = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        current = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        /////현재영상과 이전 영상을 비교하는 부분
        if(previous!=null)//이전 프레임이 비어있으면
        {
            current=matInput;///인풋을 현재 프레임에 넣어주고
        }else{
            current=matInput;///인풋을 현재 프레임에 넣어주고
            previous = current;//현재프레임을 이전프레임으로 넣어주고
        }
        int move_point=moveDetection(previous.getNativeObjAddr(),current.getNativeObjAddr());//현재프레임과 이전프레임을 비교
        previous = current;/////비교후에 현재프레임을 이전 프레임에 넣어줌
        System.out.println(move_point);
        ////움직이지 않을때 - 손가락을 갖다댔을 때 값이 3 나옴(실험 결과)
        ////손가락을 갖다댔을 때와 평상시인데 움직임이 없는 경우를 구분해야됨
        if(move_point<5)
        {
            System.out.println(sum);//출력해줌
            handler.sendEmptyMessage(no_moving);
        }else{////움직일때
            handler.sendEmptyMessage(is_moving);//출력안해줌
        }
        return matInput;
    }
    ////////움직임 알림/측정중/측정완료 알려줌
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case is_moving:
                {
                    message.setText("측정중에는 움직이지 말아주세요");
                    break;
                }

                case no_moving:
                {
                    message.setText("측정중입니다");
                    break;
                }
            }
        }
    };


}
