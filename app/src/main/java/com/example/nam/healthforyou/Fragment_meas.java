package com.example.nam.healthforyou;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NAM on 2017-07-13.
 */

public class Fragment_meas extends Fragment implements CvCameraViewListener2 {
    private static final String TAG = "opencv";

    private Mat matInput;
    private Mat matResult;
    RelativeLayout meas;
    private javaViewCameraControl mOpenCvCameraView;
    List<Mat> color= new ArrayList<>(3);
    private Camera camera;
    private boolean isFlashOn;
    private boolean hasFlash;
    Camera.Parameters params;
    boolean keep_running;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        meas = (RelativeLayout) inflater.inflate(R.layout.frag_meas,container,false);
        mOpenCvCameraView = (javaViewCameraControl)meas.findViewById(R.id.activity_surface_view);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        Button start_measure = (Button)meas.findViewById(R.id.start_measure);
        start_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOpenCvCameraView.turnFlashOn();
                //keep_running=true;
                //measuring.start();
            }
        });
        return meas;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {//Fragment에서 getActivity르 통해서 context를 얻을수 있다.
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        matResult= inputFrame.rgba();
        return matResult;
    }

    Thread measuring = new Thread(){
        @Override
        public void run() {
            while(keep_running)
            {
                Core.split(matResult,color);
                System.out.println(color.get(0));
            }
        }
    };
}
