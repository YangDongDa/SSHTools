package ydd.yddson02.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Pattern;

import ydd.son01.SshTools.ConnectionStatusListener;
import ydd.son01.SshTools.ExecTaskCallbackHandler;
import ydd.son01.SshTools.SessionController;
import ydd.son01.SshTools.SessionUserInfo;
import ydd.son01.SshTools.SshEditText;
//import ydd.son01.SshUtils.SshSftpFragmentDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, View.OnTouchListener {
    private static String TAG = "MainActivity";
    private Handler mTvHandler;//更新TextView的Handler
    private Handler mHandler;//更新TextView的Handler
    private String mLastLine;//最后一行
    private TextView mConnectStatus;

    private long count = 0;

    private SshEditText mCommandEdit;
    private Button mButton, mEndSessionBtn, mSftpButton;

    private SessionUserInfo mSUI;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }//允许在主线程中进行网络操作
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button) findViewById(R.id.enterbutton);
        mEndSessionBtn = (Button) findViewById(R.id.endsessionbutton);
        mSftpButton = (Button) findViewById(R.id.sftpbutton);
        mConnectStatus = (TextView) findViewById(R.id.connectstatus);
        mCommandEdit = (SshEditText) findViewById(R.id.command);
        mButton.setOnClickListener(this);
        mEndSessionBtn.setOnClickListener(this);
        mSftpButton.setOnClickListener(this);
        mConnectStatus.setText("未连接");


        //Handler
        mHandler = new Handler();
        mTvHandler = new Handler();




        //text change listener, for getting the current input changes,
        //当输入框内容改变时，获取最后一行
        setMCommandEdit();
        mCommandEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String[] sr = editable.toString().split("\r\n");//分割换行符
                String s = sr[sr.length - 1];//获取最后一行
                mLastLine = s;//保存最后一行

            }
        });


        mCommandEdit.setOnEditorActionListener(
                new TextView.OnEditorActionListener(){
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        //Log.d(TAG, "editor action " + event);
                        if (ConnectFragment.isEditTextEmpty(mCommandEdit)) {
                            return false;
                        }

                        // 运行命令
                        else {
                            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) {
                                return false;
                            }
                            String command = getLastLine();

                            // 获取最后一行
                            ExecTaskCallbackHandler t = new ExecTaskCallbackHandler() {
                                @Override
                                public void onFail() {
                                    makeToast(R.string.taskfail);
                                }

                                @Override
                                public void onComplete(String completeString) {
                                }
                            };
                            mCommandEdit.AddLastInput(command);

                            // 执行命令
                            SessionController.getSessionController().executeCommand(mHandler, mCommandEdit, t, command);
                            return false;
                        }
                    }
                }
        );

    }


    //设置输入框字体大小
    private void setMCommandEdit(){
        float textSizeInPixels = getResources().getDisplayMetrics().scaledDensity * 16; // 将16sp转换为像素
        mCommandEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);

    }


    private void makeToast(int text) {
        Toast.makeText(this, getResources().getString(text), Toast.LENGTH_SHORT).show();
    }//显示提示信息



    //获取最后一行
    private String getLastLine() {
        int index = mCommandEdit.getText().toString().lastIndexOf("\n");//获取最后一个换行符的位置
        if (index == -1) {//如果没有换行符，返回全部内容
            return mCommandEdit.getText().toString().trim();//去掉前后空格，第一行就是最后一行
        }
        if(mLastLine == null){//如果没有内容，返回空
            Toast.makeText(this, "no text to process", Toast.LENGTH_LONG);
            return "";
        }
        String[] lines = mLastLine.split(Pattern.quote(mCommandEdit.getPrompt()));//分割提示符,
        String lastLine = mLastLine.replace(mCommandEdit.getPrompt().trim(), "");
        Log.d(TAG, "command is " + lastLine + ", prompt is  " + mCommandEdit.getPrompt());
        return lastLine.trim();
    }



    //显示对话框



    //点击事件
    @Override
    public void onClick(View v) {
        Log.e(TAG,"点击");
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.button_click_animation);
        v.startAnimation(animation);

        if (v == mButton){
            Log.e(TAG,"点击mButton");
            showDialog();
        } else if (v == this.mEndSessionBtn) {
            try {
                if (SessionController.isConnected()) {
                    SessionController.getSessionController().disconnect();
                }
            } catch (Throwable t) { //catch everything!
                Log.e(TAG, "Disconnect exception " + t.getMessage());
            }

        }else if(v == this.mSftpButton) {
            if (SessionController.isConnected()) {
                startSftpActivity();
            }else {
                Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("提示")
                        .setMessage("请先连接服务器")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.e(TAG,"点击确定");
                            }
                        }).create();
                dialog.show();
            }

        }

    }



    //
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public boolean onLongClick(View v) {
        Log.e(TAG,"长点击");
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.e(TAG,"触摸");
        return false;
    }



    //启动文件传输
    private void startSftpActivity() {
        Intent intent = new Intent(this, FileTransfer.class );
        String[] info = {
                SessionController.getSessionController().getSessionUserInfo().getUser(),
                SessionController.getSessionController().getSessionUserInfo().getHost(),
                SessionController.getSessionController().getSessionUserInfo().getPassword()
        };

        intent.putExtra("UserInfo", info);
        Log.v(TAG, "start sftp activity");

        startActivity(intent);
    }




    //显示对话框,连接服务器
    void showDialog() {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");

        //回退栈，
        ft.addToBackStack(null);

        // Create and show the dialog.
        ConnectFragment newFragment = ConnectFragment.newInstance();
        newFragment.setContext(this);
        newFragment.setListener(new ConnectionStatusListener() {
            @Override
            public void onDisconnected() {

                mTvHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatus.setText("未连接");
                    }
                });
            }

            @Override
            public void onConnected() {

                mTvHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mConnectStatus.setText("已连接");
                    }
                });
            }
        });

        newFragment.show(ft, "dialog");
    }



}