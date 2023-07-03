package ydd.yddson02.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import ydd.adapters.FileListAdapter;
import ydd.adapters.RemoteFileListAdapter;
import ydd.son01.SshTools.SessionController;
import ydd.son01.SshTools.TaskCallbackHandler;

public class FileTransfer extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private static final String TAG = "FileListActivity";
    private String[] mUserInfo;
    private GridView mLocalGridView,mRemoteGridView;
    private File mRootFile;
    private ArrayList<File> mFilenames = new ArrayList<File>();
    private File downloadDir;
    private FileListAdapter mFileListAdapter;
    private RemoteFileListAdapter mRemoteFileListAdapter;
    private RemoteClickListener mRemoteClickListener;
    private Button mUpButton;
    private TextView mStateView,mRemoteView;
    private SessionController mSessionController;
    private boolean mIsProcessing = false;
    private ProgressBar mProgressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filelistactivity);


        //获取用户信息
        mUserInfo = getIntent().getExtras().getStringArray("UserInfo");
        Log.v(TAG,"name" + mUserInfo[0] + "\thost" + mUserInfo[1]);

        mLocalGridView = (GridView) findViewById(R.id.listview);
        mRemoteGridView = (GridView) findViewById(R.id.remotelistview);


//        mRootFile = Context.getExternalFilesDir();//
        // 获取应用程序的下载目录
        downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        //获取本地文件列表
        mRootFile = getExternalFilesDir(null);
        Log.v(TAG,"mRootFile" + mRootFile.toString());



        for (File file : mRootFile.listFiles()) {
            mFilenames.add(file);
            Log.v(TAG,"file" + file.toString());
        }


        Log.v(TAG,"mFilenames" + mFilenames.size());


        //设置本地文件列表
        mFileListAdapter = new FileListAdapter(this, mFilenames);
        mLocalGridView.setAdapter(mFileListAdapter);
        mLocalGridView.setOnItemClickListener(this);





        //buttons
        mUpButton = (Button) findViewById(R.id.upbutton);
//        mConnectButton = (Button) findViewById(R.id.connectbutton);
        mUpButton.setOnClickListener(this);
//        mConnectButton.setOnClickListener(this);

        //设置状态栏
        mStateView = (TextView) findViewById(R.id.statetextview);


        //获取进行连接的SessionController
        mSessionController = SessionController.getSessionController();
        mSessionController.connect();

        mProgressBar = (ProgressBar) findViewById(R.id.mprogress_bar);
        //设置远程文件列表点击
        mRemoteView = (TextView) findViewById(R.id.centertext);
        mRemoteClickListener = new RemoteClickListener();
        mRemoteGridView.setOnItemClickListener(mRemoteClickListener);




        //更新状态栏
        if (mSessionController.getSession().isConnected()){
            mStateView.setText("已连接");

            //显示远程文件列表
            showRemoteFiles();
        }else {
            mStateView.setText("未连接");
        }



    }


    private void setAdapter(ArrayList<File> files){
        mFileListAdapter = new FileListAdapter(this,files);
    }

    //本地文件列表点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mFilenames.get(position).isDirectory()){//判断当前点击的位置是否是文件目录

            //若当前点击的文件夹是文件目录

            //将根目录设置在当前点击文件夹
            mRootFile = mFilenames.get(position);
            Log.d(TAG,"ROOT FILE IS" + mRootFile.toString());

            mFilenames.clear();

            //若是空目录则文件列表mFilenames为空
            if (mRootFile.listFiles() == null){
                return;
            }

            //若有内容，将根目录下的文件列表传给mFilenames
            for (File file : mRootFile.listFiles()){
                mFilenames.add(file);
            }

            //将新的文件列表传入适配器,同时在前端显示出来
            setAdapter(mFilenames);
            mLocalGridView.setAdapter(mFileListAdapter);
            mFileListAdapter.notifyDataSetChanged();

        } else {
            //若是一个文件就传输他

            SftpProgressDialog progressDialog = new SftpProgressDialog(this,0);
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

            File[] arr = {mFilenames.get(position)};
            for (File file : arr){
                Log.v(TAG,"file" + file.toString());
            }
            mSessionController.uploadFiles(arr, progressDialog);

        }
    }



    //返回本地文件上级目录
    @Override
    public void onClick(View v) {
        if (v == mUpButton){
            boolean hasParent = mRootFile.getParentFile() == null ? false : true;
            if (hasParent){
                mRootFile = mRootFile.getParentFile();

                mFilenames.clear();
                for (File file : mRootFile.listFiles()){
                    mFilenames.add(file);
                }
            }
            setAdapter(mFilenames);
            mLocalGridView.setAdapter(mFileListAdapter);
            mFileListAdapter.notifyDataSetChanged();

        }
    }



    //显示远程文件列表
    private void showRemoteFiles(){

        try {
            mSessionController.listRemoteFiles(new TaskCallbackHandler() {

                @Override
                public void OnBegin() {
                    mProgressBar.setVisibility(View.VISIBLE);

                }

                @Override
                public void onFail() {
                    Log.e(TAG,"Fail listing remote files");
                    mProgressBar.setVisibility(View.GONE);

                }

                @Override
                public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                    String remotePath = mSessionController.getSftpController().getPath();
                    Log.v(TAG,remotePath);
                    mRemoteView.setText(remotePath);
                    mRemoteFileListAdapter = new RemoteFileListAdapter(FileTransfer.this,lsEntries);
                    mRemoteGridView.setAdapter(mRemoteFileListAdapter);
                    mRemoteFileListAdapter.notifyDataSetChanged();
                    mProgressBar.setVisibility(View.GONE);
                }
            },"");
        }catch (JSchException j){
            Log.e(TAG, "ShowRemoteFiles exception " + j.getMessage());
            mProgressBar.setVisibility(View.GONE);

        } catch (SftpException s){
            Log.e(TAG, "ShowRemoteFiles exception " + s.getMessage());
            mProgressBar.setVisibility(View.GONE);

        }

    }


    //远程文件列表点击事件
    private class RemoteClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mIsProcessing){
                return;
            }
            if (mRemoteFileListAdapter == null){
                return;
            }

            if (mRemoteFileListAdapter.getRemoteFiles().get(position).getAttrs().isDir() || mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename().trim() == ".."){

                //若点击的是文件夹或者是上级目录

                try {
                    mIsProcessing = true;
                    mSessionController.listRemoteFiles(new TaskCallbackHandler() {
                        @Override
                        public void OnBegin() {
                            mProgressBar.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFail() {
                            mIsProcessing = false;
                            mProgressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries) {
                            String remotePath = mSessionController.getSftpController().getPath();
                            Log.v(TAG,remotePath);
                            mRemoteView.setText(remotePath);
                            mRemoteFileListAdapter = new RemoteFileListAdapter(FileTransfer.this,lsEntries);
                            mRemoteGridView.setAdapter(mRemoteFileListAdapter);
                            mRemoteFileListAdapter.notifyDataSetChanged();
                            mIsProcessing = false;
                            mProgressBar.setVisibility(View.GONE);

                        }
                    },mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename());

                }catch (JSchException j){
                    Log.e(TAG, "ShowRemoteFiles exception " + j.getMessage());
                    mProgressBar.setVisibility(View.GONE);

                } catch (SftpException s){
                    Log.e(TAG, "ShowRemoteFiles exception " + s.getMessage());
                    mProgressBar.setVisibility(View.GONE);

                }

            }else {
                //下载一个文件


                SftpProgressDialog progressDialog = new SftpProgressDialog(FileTransfer.this, 0);
                progressDialog.setIndeterminate(false);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();

                try {
                    String name = mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename();
                    String out = mRootFile.getAbsolutePath() + "/" + name;

                    mSessionController.downloadFile(mRemoteFileListAdapter.getRemoteFiles().get(position).getFilename(), out, progressDialog);
                } catch (JSchException je) {
                    Log.d(TAG, "JschException " + je.getMessage());
                } catch (SftpException se) {
                    Log.d(TAG, "SftpException " + se.getMessage());
                }

            }
        }
    }


    //SftpProgressDialog类用于显示SFTP文件传输的进度对话框
    private class SftpProgressDialog extends ProgressDialog implements SftpProgressMonitor{

        private long mSize = 0;

        private long mCount = 0;
        public SftpProgressDialog(Context context, int theme) {
            super(context, theme);
            // TODO Auto-generated constructor stub
        }


        //            初始化进度监视器，传递操作类型（op）、源路径（src）、目标路径（dest）和文件的总大小（max）参数。
        @Override
        public void init(int op, String src, String dest, long max) {

            mSize = max;
        }

//        count方法更新当前已传输的字节数，并根据已传输的字节数与总大小的比例设置ProgressDialog的进度。
        @Override
        public boolean count(long count) {


            mCount += count;
            this.setProgress((int) ((float) (mCount) / (float) (mSize) * (float) getMax()));
            return true;
        }

        @Override
        public void end() {
            this.setProgress(this.getMax());
            this.dismiss();
        }
    }

}