package com.example.nam.healthforyou;

import android.hardware.Camera;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import android.content.Context;
import android.graphics.Color;
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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by NAM on 2017-07-13.
 */
///////////////Fragment와 카메라 연동하기가 힘들어서 일단 Activity로 작성후 옮길예정

public class Fragment_meas extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2, OnChartValueSelectedListener{

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    Context mContext;

    private LinearLayout meas;

    private static final String TAG = "opencv";
    //private CameraBridgeViewBase mOpenCvCameraView;
    private javaViewCameraControl mOpenCvCameraView;
    ////카메라 영상 받아오는 부분
    private Mat matInput;
    private Mat matResult;
    int sum;//intensity의 합
    int moving;

    ////이전이미지와 현재이미지 비교
    private Mat previous=null;
    private Mat current=null;
    int move_point;

    ////그래프
    private LineChart mChart;//차트
    private Thread thread;//값 넣어주는 부분

    private int valleyX=0;
    private int W_size=20;
    private ArrayList<Integer> peakPoint;
    private ArrayList<Integer> tempforPeak;//피크를 구하기위한 ArrayList
    private ArrayList<Integer> localMinX;//피크를 구하기위한 ArrayList

    int peak=0;
    int X=0;
    int max=0;
    int count=0;//데이터를 세는데 필요

    private YAxis leftAxis;
    private ArrayList<Integer> heart_data; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌

    //ExponentialMovingAverage maf;
    private ArrayList<Integer> heart_maf; ///피크값을 구하기 위해 그래프 말고 Array에도 데이터를 넣어줌
    double alpha;
    Double oldValue;
    double peakInterval;
    private ArrayList<Integer> arraybpm;//순간순간마다의 bpm을 저장해서 평균을 낼 역할
    int bpm;
    int avebpm;
    final static int sampling_rate=30;///카메라 fps 기준 : 30Hz
    final static int minute=60;///1분은 60초

    ////네이티브 메소드
    public native int redDetection(long matAddrInput, long matAddrResult);
    public native int moveDetection(long previous,long current);

    ////제어부분
    //*핸들러 메세지 정의
    static final int detectGo=0;
    static final int is_moving=1;
    static final int no_moving=2;
    static final int setprogress=3;///핸들러에 프로그레스바를 갱신하라는 메시지
    static final int detectDone=4;///검사를 완료 하였다는 메시지
    static final int update_heartrate=5;
    TextView startmessage;
    boolean detectStart;

    //**프로그레스 바 값 부분
    ProgressBar detectComplete;

    //***동작제어 버튼 정의
    Button btn_start;
    LinearLayout btn_detectdone;
    Button btn_restart;
    Button btn_result;

    //****메세지 텍스트뷰 부분
    TextView heart_rate;// 심박수 나타내주는 부분
    TextView follow_message;// 측정 관련 메세지를 보여주는 부분

    //*****Thread
    setTextthread setTextthread; ///텍스트 바꿔주는 쓰레드 클래스
    setHeartratethread setHeartratethread; ///심장박동수 바꿔주는 쓰레드 클래스

    ////필터 부분
    double filtered_Raw;
    private final int N = 32;
    private int n = 0;
    private double[] x = new double[N];
    private final double[] h =
            {
                    -0.00825998710050537990,
                    -0.00094549491400912290,
                    0.00162817839503944160,
                    -0.01104320682553394000,
                    -0.03522777356983981100,
                    -0.05215865024345721400,
                    -0.04496990747950474500,
                    -0.01987572938995437300,
                    -0.00796052088164146510,
                    -0.03759964387008083600,
                    -0.09790882454086007000,
                    -0.13180532798007569000,
                    -0.07790881312870971700,
                    0.06745778393854905100,
                    0.22802929229187494000,
                    0.29801216506635481000,
                    0.22802929229187494000,
                    0.06745778393854905100,
                    -0.07790881312870971700,
                    -0.13180532798007569000,
                    -0.09790882454086007000,
                    -0.03759964387008083600,
                    -0.00796052088164146510,
                    -0.01987572938995437300,
                    -0.04496990747950474500,
                    -0.05215865024345721400,
                    -0.03522777356983981100,
                    -0.01104320682553394000,
                    0.00162817839503944160,
                    -0.00094549491400912290,
                    -0.00825998710050537990,
                    -0.00865680610025201280
            };

    public double filter(double x_in)
    {
        double y = 0.0;

        //Store the current input, overwriting the oldest input
        x[n] = x_in;

        // Multiply the filter coefficients by the previous inputs and sum
        for (int i=0; i<N; i++)
        {
            y += h[i] * x[((N - i) + n) % N];
        }

        // Increment the input buffer index to the next location
        n = (n + 1) % N;

        return y;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
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

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        meas = (LinearLayout) inflater.inflate(R.layout.frag_meas,container,false);

        super.onCreate(savedInstanceState);
        Log.d("생명주기","CreateView");
        mOpenCvCameraView =(javaViewCameraControl)meas.findViewById(R.id.activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setAlpha(0);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(0); // front-camera(1),  back-camera(0)
        mOpenCvCameraView.setMaxFrameSize(200, 200);
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        //////////////////////측정순서 제어
        //1) 측정 시작 초기 단계
        btn_start = (Button)meas.findViewById(R.id.start_measure);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(detectGo);

                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();

                detectStart=true;/////시작하는 부분

                ///쓰레드 부분
                setTextthread = new setTextthread();
                setTextthread.start();////text를 테스트 하는 부분
                setHeartratethread = new setHeartratethread();
                setHeartratethread.start();///BPM을 정해주는 부분

                feedMultiple();//그래프에 데이터를 넣는 부분
            }
        });

        //////****측정 완료후 제어
        btn_detectdone = (LinearLayout)meas.findViewById(R.id.btn_detectdone); ////버튼을 담고 있는 레이아웃
        btn_detectdone.setVisibility(View.GONE);

        //2) 측정 재시작
        btn_restart = (Button)meas.findViewById(R.id.btn_redetect);
        btn_restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //기존에 있던 데이터 clear
                heart_data.clear(); // 심장에 대한 데이터 clear
                heart_maf.clear(); // 심장에 valley를 구하기 위한 데이터 clear
                peakPoint.clear(); // peakPoint 초기화
                localMinX.clear(); // 윈도우를 정하기 위한 데이터 clear
                arraybpm.clear(); // 박동에 대한 데이터 clear
                W_size=20;
                heart_rate.setText("--");
                X=0;//X값도 초기화 시켜줘야됨
                mOpenCvCameraView.enableView();
                mOpenCvCameraView.turnFlashOn();
                handler.sendEmptyMessage(detectGo);

                detectStart=true;/////시작하는 부분

                ///쓰레드 부분
                setTextthread = new setTextthread();
                setTextthread.start();////text를 정해주는 부분
                setHeartratethread = new setHeartratethread();
                setHeartratethread.start();///BPM을 정해주는 부분

                feedMultiple();//그래프에 데이터를 넣는 부분
            }
        });

        //3) 측정 기록
        btn_result = (Button)meas.findViewById(R.id.btn_result);
        btn_result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///네트워크 부분 AsyncTask 로 기록 후 측정내역으로 넘어감
            }
        });


        //*프로그레스 바
        detectComplete = (ProgressBar)meas.findViewById(R.id.progressDetecting);

        //**측정이 시작되면 보여지는 텍스트뷰
        heart_rate = (TextView)meas.findViewById(R.id.heart_rate); // 심장박동수를 보여주는 텍스트 뷰
        follow_message = (TextView)meas.findViewById(R.id.message); // 동작하는 부분을 보여주는 텍스트 뷰 ex) 측정중입니다. 측정시 움직이지 마세요
        startmessage = (TextView)meas.findViewById(R.id.startmessage);// 초기에 있는 메세지

        //CHART setting
        mChart = (LineChart)meas.findViewById(R.id.heartGraph);

        //LineChart chart = new LineChart(mContext);
        mChart.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        //data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setInverted(true);
        mChart.setAutoScaleMinMaxEnabled(true);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        heart_data = new ArrayList<>();
        tempforPeak = new ArrayList<>();
        peakPoint = new ArrayList<>();
        localMinX = new ArrayList<>();
        ////ExponentialMovingAverage 알파값을 생성자에 넣어줘야됨
        alpha = 0.03;
        heart_maf = new ArrayList<>();
        arraybpm = new ArrayList<>();
        return meas;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,getActivity(), mLoaderCallback);
        } else {
            Log.d(TAG, "onResum :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        //다른 화면 갔다가 다시 돌아왔을때를 생각!
        //초기환경 설정 해줘야됨
        //카메라,시작버튼
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("생명주기","Stop");
        mOpenCvCameraView.disableView();//카메라를 꺼줌
        //쓰레드 제어부분
        detectStart=false;
        if(setTextthread!=null)//null check
        {
            if(!setTextthread.isInterrupted())//isInterrupted check
            {
                setTextthread.interrupt();//텍스를 바꿔주는 쓰레드
            }
        }
        if(thread!=null)//null check
        {
            if(!thread.isInterrupted())//isInterrupted check
            {
                thread.interrupt();///데이터를 처리하는 쓰레드
            }
        }
        if(setHeartratethread!=null)//null check
        {
            if(!setHeartratethread.isInterrupted())//isInterrupted check
            {
                setHeartratethread.interrupt();//심박수를 갱신하는 쓰레드
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("생명주기","Destroy");
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mOpenCvCameraView.setFrameRate(30,30);
        matInput = inputFrame.rgba();
        /////빨간색을 받아오는 부분
        if ( matResult != null ) matResult.release();
        matResult = new Mat(matInput.rows(), matInput.cols(), matInput.type());
        sum=redDetection(matInput.getNativeObjAddr(), matResult.getNativeObjAddr());

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
        move_point=moveDetection(previous.getNativeObjAddr(),current.getNativeObjAddr());//현재프레임과 이전프레임을 비교

        previous = current;/////비교후에 현재프레임을 이전 프레임에 넣어줌
        ////움직이지 않을때 - 손가락을 갖다댔을 때 값이 3 나옴(실험 결과)
        ////손가락을 갖다댔을 때와 평상시인데 움직임이 없는 경우를 구분해야됨

        return matInput;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "심장박동 데이터");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawCircles(false);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setLineWidth(2f);
        //set.setHighLightColor(Color.rgb(244, 117, 117));
        //set.setValueTextColor(Color.WHITE);
        set.setDrawValues(false);
        return set;
    }

    private void addEntry() {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(),(float)Math.abs(filtered_Raw/100)), 0);///그래프에 데이터를 넣는 부분
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();
            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(150);

            //mChart.setVisibleYRangeMinimum(1, YAxis.AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    //Exponential Moving Average - alpha =0.03
    public double average(double value) {
        if (oldValue == null) {
            oldValue = value;
            return value;
        }
        double newValue = oldValue + alpha * (value - oldValue);
        oldValue = newValue;
        return newValue;
    }

    private void feedMultiple() {

        if (thread != null)
            thread.interrupt();

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                if(moving==2)//움직이지 않을때
                {
                    filtered_Raw=filter(sum);//1~5Hz BandPass Filter
                    //System.out.println((int)Math.abs(filtered_Raw/1000));//**************심장박동 출력보기**************
                    heart_data.add((int)Math.abs(filtered_Raw/100)); ///peak 값을 찾기 위해 ArrayList에 넣어줌
                    heart_maf.add((int)average(Math.abs(filtered_Raw/100)));
                    //System.out.println((int)average(Math.abs(filtered_Raw/100)));
                    ////초기에 데이터가 많이 튄다. 그러므로 50전까지는 데이터를 버린다.
                    ////2초정도
                    ////교정을 어떻게 할까? 고민해볼것
                    if(heart_data.size()<50)
                    {
                        handler.sendEmptyMessage(detectGo);
                        ////윈도우 크기를 정해주는 부분
                        if(heart_maf.size()>2)////
                        {
                            if(heart_maf.get(X-2)>=heart_maf.get(X-1)&&heart_maf.get(X-1)<=heart_maf.get(X))////실시간으로 valley 값을 구해줌
                            {
                                valleyX = X-1;
                                localMinX.add(valleyX);
                            }

                            if(localMinX.size()>2)
                            {
                                W_size=localMinX.get(localMinX.size()-1)-localMinX.get(localMinX.size()-2);//가장 최근의 윈도우의값을 구해줌
                            }

                            if(W_size<10||W_size>60)
                            {
                                W_size = 20;
                            }

                            System.out.println("윈도우 크기: "+W_size);
                        }
                    }

                    if(heart_data.size()>50)////W_size를 구할 시간을 줘야됨
                    {
                        detectStart=true;/////시작하는 부분
                        //데이터의 크기가 윈도우 크기 만큼 찼을 때까지 0일 수는 없으니
                        //W_size로 나머지를 구해주면 배수일 때 옮겨주게 됨
                        //데이터는 0~W_size,W_size~2*W_size 만큼 갖고오게됨
                        //X는 들어오는 데이터임

                        if(heart_data.size()%W_size==0)/////지금 데이터의 갯수가 이전 윈도우가 정해졌을때보다 윈도우만큼 켜져있다면
                        //W_size가 바뀌면서 갑자기 나눠질수도 있음-2017.07.20 예를들면 윈도우 사이즈가 50이다가 51로 바뀌었을때 heart_data.size가 51이면 데이터가 충분하지 않은데 바로 나눠짐
                        {
                            for(int i=heart_data.size()-W_size;i<heart_data.size()-1;i++)
                            {
                                tempforPeak.add(heart_data.get(i));/////i부터
                            }

                            max=Collections.max(tempforPeak);
                            peak=tempforPeak.indexOf(max);///피크를 구하기 위해 필요함
                            System.out.println("피크를 구하기 위한 array:"+tempforPeak);
                            //System.out.println("최대값 : "+(peak+heart_data.size()-W_size));
                            //System.out.println("여기부터 : "+(heart_data.size()-W_size)+"저기까지 : "+(heart_data.size()-1));
                            //System.out.println("heart_data:"+heart_data);
                            //System.out.println(heart_data);
                            peakPoint.add(peak+heart_data.size()-W_size);//////array안에서 최고값이므로 평소 데이터에서 X값을 구해야됨

                            System.out.println("피크들의 X값:"+peakPoint);////피크값들의 X값
                        }
                        tempforPeak.clear();////피크를 위해 저장했던 ArrayList 초기화

                        ////윈도우 크기를 정해주는 부분
                        if(heart_maf.size()>2)////
                        {
                            if(heart_maf.get(X-2)>=heart_maf.get(X-1)&&heart_maf.get(X-1)<=heart_maf.get(X))////실시간으로 valley 값을 구해줌
                            {
                                valleyX = X-1;
                                localMinX.add(valleyX);
                            }

                            if(localMinX.size()>2)
                            {
                                W_size=localMinX.get(localMinX.size()-1)-localMinX.get(localMinX.size()-2);//가장 최근의 윈도우의값을 구해줌
                            }

                            if(W_size<10||W_size>60)
                            {
                                W_size = 20;
                            }
                        }

                        if(peakPoint.size()>2)/////피크의 간격을 구하는 부분
                        {
                            peakInterval=0;//피크 간격 초기화
                            for(int i=1;i<peakPoint.size();i++)
                            {
                                peakInterval+=peakPoint.get(i)-peakPoint.get(i-1);
                            }
                            peakInterval=peakInterval/(peakPoint.size()-1);
                            //그때그때마다 구해주는 방향
                            bpm = (int)((1/peakInterval)*sampling_rate*minute);//bpm을 구하는 식은 (1/PPI)*60초*샘플링 레이트 = BPM;
                            int sumbpm=0;
                            arraybpm.add(bpm);
                            for(int i=0;i<arraybpm.size();i++) //평균 심박수에 대한 데이터 구하기
                            {
                                sumbpm+=arraybpm.get(i);
                            }
                            avebpm = sumbpm/arraybpm.size();
                            //System.out.println((float)peakInterval);
                        }
                    }

                    X++;
                    addEntry();
                }
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {//try-catch를 통해 쓰레드 인터럽트
                        while(heart_data.size()<500)///데이터가 15000개가 쌓일때까지 - 기준 생각해보기
                        {
                        // Don't generate garbage runnables inside the loop.
                            getActivity().runOnUiThread(runnable);
                            Thread.sleep(33);/////데이터 넣는 속도-카메라 프레임과 동기화
                        }
                        handler.sendEmptyMessage(detectDone);
                    }
                    catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

            }
        });
        thread.start();
    }

    public class setTextthread extends Thread
    {
        @Override
        public void run() {
            try {
                while(true)
                {
                    if(detectStart)
                    {
                        if(move_point<500)
                        {
                            handler.sendEmptyMessage(no_moving);
                        }else{////움직일때
                            handler.sendEmptyMessage(is_moving);//그래프에 데이터를 넣지 않음
                        }
                        sleep(1000);///1초에 한번씩 체크
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public class setHeartratethread extends Thread
    {
        @Override
        public void run() {
            try {
                while(true)
                {
                    if(detectStart)
                    {
                        handler.sendEmptyMessage(update_heartrate);//심장박동수를 갱신해주는 메시지
                        handler.sendEmptyMessage(setprogress);
                        sleep(500);///3초에 한번씩 체크
                    }
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    ////////움직임 알림/측정중/측정완료 알려줌
     Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case detectGo:{
                    btn_start.setVisibility(View.GONE);///시작 버튼을 누르면 사라짐
                    btn_detectdone.setVisibility(View.GONE);
                    heart_rate.setVisibility(View.VISIBLE);
                    startmessage.setVisibility(View.GONE);
                    follow_message.setVisibility(View.VISIBLE);
                    follow_message.setText("측정을 준비중입니다");
                    break;
                }

                case is_moving: {
                    follow_message.setText("측정중에는 움직이지 말아주세요");
                    moving = is_moving;
                    break;
                }

                case no_moving: {
                    follow_message.setText("측정중입니다");
                    moving = no_moving;
                    if(detectComplete.getProgress()>50)//50%가 넘어가면
                    {
                        follow_message.setText("거의 측정이 완료되었습니다");
                    }
                    break;
                }

                case setprogress: {
                    detectComplete.setProgress((heart_data.size() /5));
                    break;
                }

                case detectDone:
                {
                    detectStart=false;//쓰레드를 정지시킴
                    btn_detectdone.setVisibility(View.VISIBLE);//측정 완료 시에 보여주는 레이아웃
                    btn_detectdone.bringToFront(); ////FrameLayout에서 가장 앞쪽에 위치하도록 해줌
                    follow_message.setText("측정 완료");//측정 지시 메시지 안보이게

                    mOpenCvCameraView.turnFlashOff();//플래시를 꺼줌
                    mOpenCvCameraView.disableView();//카메라를 꺼줌
                    break;
                }

                case update_heartrate:
                {
                    if(avebpm!=0)
                    {
                        heart_rate.setText(avebpm+"BPM");
                    }else{
                        heart_rate.setText("--");
                    }
                }
            }
        }
    };

    public void chart_Init()
    {
        mChart = (LineChart)meas.findViewById(R.id.heartGraph);

        //LineChart chart = new LineChart(mContext);
        mChart.setOnChartValueSelectedListener(this);
        // enable description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);
        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        //data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(false);

        leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        mChart.setAutoScaleMinMaxEnabled(true);
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        heart_data = new ArrayList<>();

        tempforPeak = new ArrayList<>();
        peakPoint = new ArrayList<>();

        localMinX = new ArrayList<>();
        ////ExponentialMovingAverage 알파값을 생성자에 넣어줘야됨
        alpha = 0.03;
        heart_maf = new ArrayList<>();
        arraybpm = new ArrayList<>();
    }
}
