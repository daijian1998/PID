package com.example.dsp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.xw.repo.BubbleSeekBar;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.BluetoothCallback;
import me.aflak.bluetooth.interfaces.DeviceCallback;
import me.aflak.bluetooth.interfaces.DiscoveryCallback;
import me.aflak.bluetooth.reader.SocketReader;


public class Index extends AppCompatActivity {
    Bluetooth bluetooth;
    LineReader reader;
    ImageView img;
    LineDataSet lineDataSet;
    LineData data;
    ObjectAnimator animator;
    LineChart lineChart;//线性图控件
    Switch aSwitch;
    BubbleSeekBar seekBar;
    List<Entry> entries = new ArrayList<>();//图表数据集合
    int i = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        initialize_Object();//初始化控件
        bluetooth.getBluetoothAdapter();
        bluetooth.setReader(Split.class);
        bluetooth.setDeviceCallback(new DeviceCallback() {
            @Override
            public void onDeviceConnected(BluetoothDevice device) {
               // bluetooth.send("hello");
//                bluetooth.setReader(DelimiterReader.class);
                Log.d("daijian", "已经连接");
            }

            @Override
            public void onDeviceDisconnected(BluetoothDevice device, String message) {

            }

            @Override
            public void onMessage(byte[] message) {
                Log.d("byte", "消息来了" + i);
                byte[] temp=new byte[1024];

                temp=message;
                //i++;
                for(int i=0;i<message.length;i++){
                    Log.d("byte",message[i]+"");
                }
                byte[] buffer=new byte[4];
                if(message.length>=4){

                    for (int i=0;i<4;i++){
                        buffer[i]=message[i];
                    }
                    buffer= reverseArray(buffer);
                    String daijian=byteArrayToHexString(buffer);

                    Log.d("str",daijian);
                    ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
                    DataInputStream dis=new DataInputStream(new ByteArrayInputStream(buffer));
                    try {
                        float d=dis.readFloat();
                       //temp=d;
                        Log.d("msg","温度"+d+"");
                        if(Math.abs(d*1000)<1){
                        }else {
                            add(d);
                        }

                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //float f=getFloat(buffer,0);


               //Log.d("msg","温度"+f+"");
                for(int i=0;i<buffer.length;i++){
                    Log.d("msg",buffer[i]+"");
                }
                //int res=Byte2IntLowHigh(message);
                //float tem=Float.intBitsToFloat(res);
               // add(tem);

            }

            @Override
            public void onError(int errorCode) {
                Log.d("daijian", "错误");
            }

            @Override
            public void onConnectError(BluetoothDevice device, String message) {

            }
        });
        // init_search();
        initialize_rote();//初始化对象旋转动画
        //region 滑动条
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {//listPairedDevices
//                    seekBar.setVisibility(View.VISIBLE);
//                    if(animator.isStarted()){
//                        animator.resume();
//                    }
//                    else{
//                        animator.start();
//                    }
                   // bluetooth.send("23.4");
                } else {
                    //bluetooth.send("23.4");
//                    animator.pause();
//                    seekBar.setVisibility(View.INVISIBLE);
                }
            }
        });
        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                Log.d("test", progress + "");
                animator.pause();
                animator.setDuration((101 - progress) * 5000);
                animator.resume();
                Log.d("test", "函数结束");
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                Log.d("daijian", progressFloat + "");
                float test=32.0f;
                byte[] b=getBytes(progressFloat);
                byte[] msg=reverseArray(b);
                bluetooth.send(b);
                for(int i=0;i<b.length;i++)
                {
                    Log.d("send",b[i]+"");
                }                //bluetooth.send(msg);

                //img.clearAnimation();
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                Log.d("test", progress + "over");
            }
        });
        init_line();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
               // add();
            }
        }, 0, 1000);
    }
    //endregion

    @Override
    protected void onStart() {
        super.onStart();
        bluetooth.onStart();
        if (bluetooth.isEnabled()) {
            // doStuffWhenBluetoothOn() ...
            bluetooth.connectToName("Wang");
        } else {
            bluetooth.enable();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        bluetooth.onStop();
    }

    private BluetoothCallback bluetoothCallback = new BluetoothCallback() {
        @Override
        public void onBluetoothTurningOn() {
        }

        @Override
        public void onBluetoothTurningOff() {
        }

        @Override
        public void onBluetoothOff() {
        }

        @Override
        public void onBluetoothOn() {
            // doStuffWhenBluetoothOn() ...
            bluetooth.connectToName("Wang");
        }

        @Override
        public void onUserDeniedActivation() {
            // handle activation denial...
        }
    };
    //region 图表
    //初始化旋转函数
    void initialize_rote() {
        animator = ObjectAnimator.ofFloat(img, "rotation", 0.0f, 36000.0f);
        animator.setDuration(50000);
        animator.setRepeatMode(ObjectAnimator.RESTART);
        animator.setRepeatCount(Animation.INFINITE);

        animator.setInterpolator(new LinearInterpolator());
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Log.d("test", "动画开始了");
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.addPauseListener(new Animator.AnimatorPauseListener() {
            @Override
            public void onAnimationPause(Animator animation) {
                Log.d("test", "动画暂停了" + animator.getCurrentPlayTime());
            }

            @Override
            public void onAnimationResume(Animator animation) {

            }
        });
        animator.start();
    }

    //控件实例化函数
    void initialize_Object() {
        bluetooth = new Bluetooth(this);
        bluetooth.setBluetoothCallback(bluetoothCallback);
        img = findViewById(R.id.fan);
        lineChart = findViewById(R.id.lineChart);
        aSwitch = findViewById(R.id.switch1);
        seekBar = findViewById(R.id.seekbar);
    }

    //初始化图表配置
    public void init_line() {
        lineChart.setDrawBorders(true);//显示边界
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);//设置X轴在图表底部
        lineChart.getXAxis().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);
        YAxis yAxisleft;
        yAxisleft = lineChart.getAxisLeft();
        yAxisleft.setAxisMaximum(70);//设置Y轴最大值
        yAxisleft.setAxisMinimum(-10);
        yAxisleft.setGranularity(5f);//设置间隔
        yAxisleft.setTextColor(Color.WHITE);
        //去除高亮点
        lineChart.setHighlightPerTapEnabled(false);
        lineChart.setHighlightPerDragEnabled(false);
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(15);
        //随机添加是个数据
//        for (int i = 0; i < 11; i++) {
        //           entries.add(new Entry(i, new Random().nextInt(50-45+1)+45));
        //     }
        lineDataSet = new LineDataSet(entries, "温度表");

        lineDataSet.setValueTextColor(Color.WHITE);//设置数据文本颜色
        lineDataSet.setValueTextSize(0);//设置数据文本大小
        lineDataSet.setDrawCircles(false);//禁止显示圆点
        data = new LineData(lineDataSet);
        lineChart.setBorderColor(Color.WHITE);
        lineChart.setDrawGridBackground(false);
        Description description = new Description();
        description.setText("");
        description.setTextSize(18);
        description.setTextColor(Color.BLUE);
        lineChart.setDescription(description);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        Drawable drawable = getResources().getDrawable(R.drawable.fade_blue);
        lineChart.setData(data);
        setChartFillDrawable(drawable);

    }

    //为图表设置填充背景
    public void setChartFillDrawable(Drawable drawable) {
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            //避免在 initLineDataSet()方法中 设置了 lineDataSet.setDrawFilled(false); 而无法实现效果
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            lineChart.invalidate();
        }
    }
//endregion
    public void add(float temp) {
        lineDataSet.addEntry(new Entry(lineDataSet.getEntryCount(),temp));
        data = new LineData(lineDataSet);
        lineChart.setData(data);
        Log.d("test", i + "");
        i++;
        XAxis x = lineChart.getXAxis();

        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

    }

    public class LineReader extends SocketReader {
        public BufferedReader reader;

        public LineReader(InputStream inputStream) {
            super(inputStream);
            reader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public byte[] read() throws IOException {
            Log.d("daijian", "test");
            return reader.readLine().getBytes();
        }
    }

    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }
    //转换byte
    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        bytes[2] = (byte) ((data & 0xff0000) >> 16);
        bytes[3] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }
    //翻转byte数组
    public static byte[] reverseArray(byte[] array){
        byte[] newArray = new byte[array.length];
        for(int i=0; i<newArray.length; i++){
            newArray[i] = array[array.length - i - 1];
        }
        return newArray;
    }

    //ByteToFloat
    public static float getFloat(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }
    public static final String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) {
            int v = b & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.getDefault());
    }
    public class DelimiterReader extends SocketReader {
        private PushbackInputStream reader;
        private byte delimiter;

        public DelimiterReader(InputStream inputStream) {
            super(inputStream);
            reader = new PushbackInputStream(inputStream);
            delimiter = 0x0d;
        }

        @Override
        public byte[] read() throws IOException {
            List<Byte> byteList = new ArrayList<>();
            byte[] tmp = new byte[1];

            while(true) {
                int n = reader.read();
                reader.unread(n);

                int count = reader.read(tmp);
                if(count > 0) {
                    if(tmp[0] == delimiter){
                        byte[] returnBytes = new byte[byteList.size()];
                        for(int i=0 ; i<byteList.size() ; i++){
                            returnBytes[i] = byteList.get(i);
                        }
                        return returnBytes;
                    } else {
                        byteList.add(tmp[0]);
                    }
                }
            }
        }
    }



    }