package com.zhengyuan.emqrcodepro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.common.zxing.CaptureActivity;
import com.zhengyuan.baselib.constants.EMProApplicationDelegate;
import com.zhengyuan.baselib.listener.NetworkCallbacks;
import com.zhengyuan.baselib.utils.Utils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

public class MainActivity extends Activity implements View.OnClickListener {

    private String mLoginID = EMProApplicationDelegate.userInfo.getUserId();

    private ImageButton mBackBtn;
    //private ImageButton menu;
    //private TextView mTextView;

    public ImageView mScanImageView;
    public EditText QRInfoEditText;
    public Button submitQRInfoButton;

    public static final int REQUST_SACN_CODE0 = 0;
    private String mScanInfo;
    /*private Handler handlerSubmitQRInfo;*/

    private handlerSubmitQRInfo2 mhandlerSubmitQRInfo2 = new handlerSubmitQRInfo2(MainActivity.this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
        /*handlerSubmitQRInfo = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                if (msg.obj.equals("true")) {
                    Toast.makeText(MainActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    QRInfoEditText.setText("");
                    QRInfoEditText.setVisibility(View.GONE);
                } else {
                    Toast.makeText(MainActivity.this, "系统原因，提交失败", Toast.LENGTH_SHORT).show();
                }
            }
        };*/
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sacnImageView:
                //扫码
                sweep(view);
                break;
            case R.id.title_back_btn:
                finish();
                break;
            case R.id.submitQRInfoButton:
                if (QRInfoEditText.getText() != null && QRInfoEditText.getText().length() > 0) {
                    submitQRInfo(mScanInfo, mLoginID);
                } else {
                    Toast.makeText(MainActivity.this, "请先扫码获取信息", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void initView() {

        mBackBtn = (ImageButton) findViewById(R.id.title_back_btn);
        ImageButton menu = (ImageButton) findViewById(R.id.main_menu_bn);
        menu.setVisibility(View.GONE);
        TextView mTextView = (TextView) findViewById(R.id.title_tv);
        mTextView.setText("扫码" + "-" + mLoginID);

        mScanImageView = (ImageView) findViewById(R.id.sacnImageView);
        QRInfoEditText = (EditText) findViewById(R.id.QRInfoEditText);
        submitQRInfoButton = (Button) findViewById(R.id.submitQRInfoButton);

    }

    private void initEvent() {
        mScanImageView.setOnClickListener(this);
        mBackBtn.setOnClickListener(this);
        submitQRInfoButton.setOnClickListener(this);
    }

    public void sweep(View view) {
        Intent intent = new Intent();
        intent.setClass(this, CaptureActivity.class);
        intent.putExtra("autoEnlarged", true);
        startActivityForResult(intent, REQUST_SACN_CODE0);
    }

    //显示照片
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            //扫码结果返回
            mScanInfo = data.getStringExtra("result");
            mScanInfo = recode(mScanInfo);
            if (mScanInfo != null && mScanInfo != "") {
                //提交到数据库
                QRInfoEditText.setVisibility(View.VISIBLE);
                QRInfoEditText.setText(mScanInfo);
            } else {
                Utils.showToast("未扫描");
            }
        }
    }

    //解决乱码问题
    public String recode(String str) {
        String formart = "";

        try {
            boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
                    .canEncode(str);
            if (ISO) {
                formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
                Log.i("1234      ISO8859-1", formart);
            } else {
                formart = str;
                Log.i("1234      stringExtra", str);
            }
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formart;
    }

    //提交数据
    private void submitQRInfo(String QRInfo, String loginID) {
        //获取并提交数据
        DataObtainer.INSTANCE.submitQRInfo(QRInfo, loginID,
                new NetworkCallbacks.SimpleDataCallback() {
                    @Override
                    public void onFinish(boolean b, String s, Object o) {
                        if (o.equals("")) {
                            Utils.showToast("没有找到！");
                            return;
                        }
                        /*Message m = handlerSubmitQRInfo.obtainMessage();
                        m.obj = (String) o;
                        handlerSubmitQRInfo.sendMessage(m);*/

                        Message m = mhandlerSubmitQRInfo2.obtainMessage();
                        m.obj = (String) o;
                        mhandlerSubmitQRInfo2.sendMessage(m);

                    }
                }
        );
    }

    /*
    * 提示用户信息
    * */
    public void myToast(String info, int LENGTH) {
        Toast.makeText(MainActivity.this, info, LENGTH).show();
    }

    /*
    * 静态内部类和弱引用，解决内存泄漏的问题
    * */
    public static class handlerSubmitQRInfo2 extends Handler {
        //弱引用<引用外部类>
        WeakReference<MainActivity> mActivity;

        handlerSubmitQRInfo2(MainActivity activity) {
            //构造创建弱引用
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            //通过弱引用获取外部类.
            MainActivity activity = mActivity.get();
            //进行非空再操作
            if (activity != null) {
                super.handleMessage(msg);
                if (msg.obj.equals("true")) {
                    activity.myToast("提交成功", Toast.LENGTH_SHORT);
                    activity.QRInfoEditText.setText("");
                    activity.QRInfoEditText.setVisibility(View.GONE);
                } else {
                    activity.myToast("系统原因，提交失败", Toast.LENGTH_SHORT);
                }
            }
        }
    }

}
