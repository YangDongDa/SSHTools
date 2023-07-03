package ydd.son01.SshTools;
import android.os.Handler;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SessionController {
    private static final String TAG = "SessionController";

    private Session mSession;

    private SessionUserInfo mSessionUserInfo;


    private Thread mThread;

    private ConnectionStatusListener mConnectStatusListener;


    private static SessionController sSessionController;

    private ShellController mShellController;
    private static SftpController mSftpController;



    private SessionController() {

    }

    private SessionController(SessionUserInfo sessionUserInfo) {
        mSessionUserInfo = sessionUserInfo;
        connect();

    }

    public static SessionController getSessionController() {
        if (sSessionController == null) {
            sSessionController = new SessionController();
        }
        return sSessionController;
    }
    public Session getSession() {
        return mSession;
    }
    public void setConnectionStatusListener(ConnectionStatusListener csl) {
        mConnectStatusListener = csl;
    }




    public static boolean exists() {
        return sSessionController != null;
    }


    public static boolean isConnected() {

        //如果sSessionController不为空，返回true
        Log.v(TAG, "session controller exists... " + exists());
        if (exists()) {
            Log.v(TAG, "disconnecting");
            if (getSessionController().getSession().isConnected())
                return true;
        }
        return false;
    }
    public void setUserInfo(SessionUserInfo sessionUserInfo) {
        mSessionUserInfo = sessionUserInfo;
    }

    public SessionUserInfo getSessionUserInfo() {
        return mSessionUserInfo;
    }
    public void connect() {
        if (mSession == null) {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        } else if (!mSession.isConnected()) {
            mThread = new Thread(new SshRunnable());
            mThread.start();
        }
    }

    public void disconnect() throws IOException {

            if (mSession != null){
                if (mShellController != null) {
                    try {
                        mShellController.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "Shell close exception " + e.getMessage());
                    }
                }
                synchronized (mConnectStatusListener){
                    if (mConnectStatusListener != null){
                        mConnectStatusListener.onDisconnected();
                    }
                }
                mSession.disconnect();
                mSession = null;
            }

            if (mThread != null && mThread.isAlive()){
                try {
                    mThread.join();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Thread join exception " + e.getMessage());
                }
            }
            mShellController = null;

    }




    public boolean executeCommand(Handler handler, SshEditText editText, ExecTaskCallbackHandler callback,String command) {
        if (mSession == null || !mSession.isConnected()) {
            return false;
        } else {


            if (mShellController == null) {
                mShellController = new ShellController();

                try {
                    Log.v(TAG, "open shell");
                    mShellController.openShell(getSession(), handler, editText);

                } catch (Exception e) {
                    Log.e(TAG, "Shell open exception " + e.getMessage());
//                    TODO fix general exception catching
                }
            }

            synchronized (mShellController) {
                mShellController.writeToOutput(command);
            }
        }

        return true;
    }



    public class SshRunnable implements Runnable {

        public void run() {
            JSch jsch = new JSch();
            mSession = null;
            try {
                //创建session，连接到服务器
                mSession = jsch.getSession(mSessionUserInfo.getUser(), mSessionUserInfo.getHost(),
                        mSessionUserInfo.getPort()); // port 22
//                mSession = jsch.getSession("root","175.24.199.237",22);
                Log.v(TAG,"username:" + mSessionUserInfo.getUser() + " host:"+ mSessionUserInfo.getHost()+" port:"+mSessionUserInfo.getPort());

//                mSession.setPassword(mSessionUserInfo.getPassword());

                mSession.setPassword("2233");
                //设置不检查host key
                Properties properties = new Properties();
                properties.setProperty("StrictHostKeyChecking", "no");
                mSession.setConfig(properties);
                mSession.connect(30000);//连接到服务器

            } catch (JSchException jex) {
                Log.e(TAG, "JschException: " + jex.getMessage() +
                        ", Fail to get session " + mSessionUserInfo.getUser() +
                        ", " + mSessionUserInfo.getHost());
            } catch (Exception ex) {
                Log.e(TAG, "Exception:" + ex.getMessage());
            }

            Log.d("SessionController", "Session connected? " + mSession.isConnected());
//            alias ls="ls --color=never"
            //开始监控连接状态
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        //keep track of connection status
                        try {
                            Thread.sleep(2000);
                            if (mConnectStatusListener != null) {
                                if (mSession.isConnected()) {
                                    mConnectStatusListener.onConnected();
                                } else mConnectStatusListener.onDisconnected();
                            }
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }).start();
        }
    }

    public SftpController getSftpController(){
        return mSftpController;

    }
    public void uploadFiles(File[] files, SftpProgressMonitor spm) {
        if (mSftpController == null) {
            mSftpController = new SftpController();
            Log.v(TAG, "new sftp controller");

        }
        Log.v(TAG, "upload files");
        mSftpController.new UploadTask(mSession, files, spm).execute();
    }
    public boolean downloadFile(String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException {
        if (mSftpController == null) {
            mSftpController = new SftpController();
        }
        mSftpController.new DownloadTask(mSession, srcPath, out, spm).execute();
        return true;
    }
    public void listRemoteFiles(TaskCallbackHandler taskCallbackHandler, String path) throws JSchException, SftpException {

        if (mSession == null || !mSession.isConnected()) {
            return;
        }

        if (mSftpController == null) {
            mSftpController = new SftpController();

        }
        //list the files.
        mSftpController.lsRemoteFiles(mSession, taskCallbackHandler, path);


    }



}
