
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WifiControlActivity extends AppCompatActivity {
    EditText wifiIp_et, wifiPort_et;
    Button stop_motorByWifi_btn,stop_fanByWifi_btn, wifiConnect_btn, wifiDisconnect_btn,wifiBack_bt;
    private int PORT;
    private String HOST;
    private Socket mSocket;
    private PrintStream out;

    //    private Handler handler = new Handler() {
//        // 在这里进行UI操作，将结果显示到界面上
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
////                case CONNECTED_RESPONSE:
////                    bt_connect.setTextColor(Color.parseColor("#216F02"));
////                    bt_connect.setText("已连接");
////                    bt_connect.setClickable(false);
////                    disconnect.setTextColor(Color.BLACK);
////                    disconnect.setClickable(true);
////                    break;
////                case RESPONSE_TIMEOUT:
////                    Toast.makeText(getApplicationContext(), "连接失败！", Toast.LENGTH_SHORT).show();
////                case RECEIVER_RESPONSE:
////                case SEND_RESPONSE:
//                default:
//                    break;
//            }
//        }
//    };
    final static int CONNECTED_RESPONSE = 1000;//连接成功
    final static int RESPONSE_TIMEOUT = 1001;//未连接
    final static int SEND_RESPONSE = 1002;//发送
    final static int RECEIVER_RESPONSE = 1003;//接收

    private class MHandler extends Handler {
        private WeakReference<Activity> mActivity;

        MHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                case CONNECTED_RESPONSE:
                    wifiConnect_btn.setTextColor(Color.parseColor("#216F02"));
                    wifiConnect_btn.setText("已连接");
                    wifiConnect_btn.setClickable(false);
                    wifiDisconnect_btn.setTextColor(Color.BLACK);
                    wifiDisconnect_btn.setClickable(true);
                    break;
                case RESPONSE_TIMEOUT:
                    Toast.makeText(getApplicationContext(), "连接失败！", Toast.LENGTH_SHORT).show();
                case RECEIVER_RESPONSE:
                case SEND_RESPONSE:
                default:
                    break;
                }

            }
        }
    }

    private final Handler mHandler = new WifiControlActivity.MHandler(this);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_control);
        wifiPort_et = findViewById(R.id.wifiPort);
        wifiIp_et = findViewById(R.id.wifiIp);
        stop_motorByWifi_btn = findViewById(R.id.stop_motorByWifi);
        stop_fanByWifi_btn = findViewById(R.id.stop_fanByWifi);
        wifiConnect_btn = findViewById(R.id.wifiConnect);
        wifiDisconnect_btn = findViewById(R.id.wifiDisconnect);
        wifiBack_bt = findViewById(R.id.wifiBack_bt);

        wifiConnect_btn.setOnClickListener(new OnClick());
        wifiDisconnect_btn.setOnClickListener(new OnClick());
        stop_motorByWifi_btn.setOnClickListener(new OnClick());
        stop_fanByWifi_btn.setOnClickListener(new OnClick());
        wifiBack_bt.setOnClickListener(new OnClick());
    }
    class OnClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.wifiConnect:
                    connect();
                    break;
                case R.id.wifiDisconnect:
                    diconnect();
                    break;
                case R.id.stop_motorByWifi:
                    TCPSend("0");
                        break;
                case R.id.stop_fanByWifi:
                    TCPSend("1");
                        break;
                case R.id.wifiBack_bt:
                    Intent intent = new Intent(WifiControlActivity.this,MainActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    }
//    private void connect() {
//        // 开启线程来发起网络请求
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HOST = (InetAddress) wifiName_et.getText();
//                PORT = Integer.valueOf(wifiPw_et.getText().toString());
//                //文本框都不为空
//                if( !TextUtils.isEmpty(wifiName_et.getText().toString())  && !TextUtils.isEmpty(wifiPw_et.getText().toString())) {
//
//                    try {
//                        socket = new Socket();
//                        socket.connect(new InetSocketAddress(HOST, PORT), 4000);
//                        if (socket != null) {
//                            Message message = new Message();
//                            message.what = CONNECTED_RESPONSE;
//                            mHandler.sendMessage(message);
//                        }
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                        Message message = new Message();
//                        message.what = RESPONSE_TIMEOUT;
//                        mHandler.sendMessage(message);
//                    }
//                }else{
//                    showToast("请输入WiFi名称及密码");
//                }
//            }
//        }).start();
//    }
    //连接
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {

                //判断输入是否为空
                if (!TextUtils.isEmpty(wifiIp_et.getText())  && !TextUtils.isEmpty(wifiPort_et.getText())){
                    //获取编辑框值
                    HOST = wifiIp_et.getText().toString();
                    PORT = Integer.valueOf(wifiPort_et.getText().toString());
                    try{
                        mSocket = new Socket(HOST,PORT);
//                        out = new PrintStream(mSocket.getOutputStream());
                        //如果不为空发送连接成功
                        if (mSocket != null) {
                            System.out.println("socket连接成功");
                            Message message = new Message();
                            message.what = CONNECTED_RESPONSE;
                            mHandler.sendMessage(message);
                        }
                    }catch (IOException ex) {
                        System.out.println("连接失败");
                        ex.printStackTrace();
                        Message message = new Message();
                        message.what = RESPONSE_TIMEOUT;
                        mHandler.sendMessage(message);
                    }
                }else{
                    showToast("请输入WiFi名称及密码");
                }
            }
        }).start();
    }
    //断开连接
    private void diconnect(){
        if(mSocket != null) {
            out.close();
            try {
                mSocket.close();
                System.out.println("已断开连接");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            wifiDisconnect_btn.setTextColor(Color.parseColor("#216F02"));
            wifiDisconnect_btn.setText("已断开连接");
            wifiDisconnect_btn.setClickable(false);
            wifiConnect_btn.setTextColor(Color.BLACK);
            wifiConnect_btn.setClickable(true);
        }
    }
    //发送数据
    private void TCPSend(final String data) {
        if(mSocket == null)
        {
            showToast("请先连接！");
        }else {
            // 开启线程来发起网络请求
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PrintWriter writer = new PrintWriter(mSocket.getOutputStream());
                        writer.println(data);
                        writer.flush();
                        System.out.println("TCPSend发送数据成功");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        System.out.println("TCPSend发送数据失败");
                        System.out.println("发送数据成功");
                        Message message = new Message();
                        message.what = SEND_RESPONSE;
                        // 将服务器返回的结果存放到Message中
                        message.obj = "操作失败！";
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        }
    }

    void showToast(String msg){//toast显示
        Looper.prepare();
        Toast.makeText(WifiControlActivity.this, msg, Toast.LENGTH_SHORT).show();
        Looper.loop();
    }
}
