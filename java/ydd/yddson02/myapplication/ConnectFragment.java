package ydd.yddson02.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ydd.son01.SshTools.ConnectionStatusListener;
import ydd.son01.SshTools.SessionController;
import ydd.son01.SshTools.SessionUserInfo;

public class ConnectFragment extends DialogFragment implements View.OnClickListener{

    private static final String TAG = "SshConnectFragmentDialog";

    private EditText mUserEdit;
    private EditText mHostEdit;
    private EditText mPasswordEdit;
    private EditText mPortNumEdit;
    private Button mButton;
    private Context mContext;

    //用户信息
    private SessionUserInfo mSUI;
    private ConnectionStatusListener mListener;


    public void setContext(Context context){
        mContext = context;
    }
    //连接状态监听器

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @SuppressLint("MissingInflatedId")//忽略inflate的警告，inflate的布局文件中没有id
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_connect, container, false);
        mUserEdit = (EditText) v.findViewById(R.id.username);
        mHostEdit = (EditText) v.findViewById(R.id.host);
        mPasswordEdit = (EditText) v.findViewById(R.id.password);
        mPortNumEdit = (EditText) v.findViewById(R.id.port);
        mButton = (Button) v.findViewById(R.id.connectBtn);
        mButton.setOnClickListener(this);
        return v;
    }

        @Override
        public void onClick(View v) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.button_click_animation);
            v.startAnimation(animation);

            if(v.getId() == R.id.connectBtn){
                if(isEditTextEmpty(mUserEdit) || isEditTextEmpty(mHostEdit) || isEditTextEmpty(mPasswordEdit) || isEditTextEmpty(mPortNumEdit)){
                    return;
                }
                Log.e(TAG,"connect click");
                mSUI = new SessionUserInfo(mUserEdit.getText().toString(), mHostEdit.getText().toString(), mPasswordEdit.getText().toString(), Integer.parseInt(mPortNumEdit.getText().toString()));
//                mSUI = new SessionUserInfo("root","175.24.199.237","2233",22);
                SessionController.getSessionController().setUserInfo(mSUI);
                SessionController.getSessionController().connect();
            }

            if (mListener != null)
                SessionController.getSessionController().setConnectionStatusListener(mListener);


        }
    public void setListener(ConnectionStatusListener listener){
        mListener = listener;
    }

    public static ConnectFragment newInstance() {
        ConnectFragment fragment = new ConnectFragment();

        return fragment;
    }


    //检查EditText是否为空
    public static boolean isEditTextEmpty(EditText editText) {
        if (editText.getText() == null || editText.getText().toString().equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }




}
