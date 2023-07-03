package ydd.son01.SshTools;

import android.os.AsyncTask;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

public class SftpController {
    public static final String TAG = "SftpController";

    //当前路径
    private String mCurrentPath = "/";
    private String simplifiedPath = mCurrentPath;
    public SftpController() {

    }
    public SftpController(String path) {

        mCurrentPath = path;
    }

    //重置路径
    public void resetPathToRoot() {

        mCurrentPath = "/";
    }

    public String getPath() {
        return simplifiedPath;
    }
    public void setPath(String path) {

        mCurrentPath = path;
    }


    public class UploadTask extends AsyncTask<Void,Void,Boolean>{
        private Session mSession;
        private SftpProgressMonitor mMonitor;
        private File[] mLocalFiles;
        public UploadTask(Session session, File[] localFiles, SftpProgressMonitor spd) {

            mMonitor = spd;
            mLocalFiles = localFiles;
            mSession = session;
        }
        @Override
        protected void onPreExecute() {
            //nothing
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean success = true;
            try {
                uploadFiles(mSession, mLocalFiles, mMonitor);
            } catch (JSchException e) {
                e.printStackTrace();
                Log.e(TAG, "JSchException " + e.getMessage());
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IOException " + e.getMessage());
                success = false;
            } catch (SftpException e) {
                e.printStackTrace();
                Log.e(TAG, "SftpException " + e.getMessage());
                success = false;
            } finally {
                return success;
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            //TODO
        }

    }
    public void uploadFiles(Session session, File[] localFiles, SftpProgressMonitor spm) throws JSchException, IOException, SftpException {
        if (session == null || !session.isConnected()) {
            session.connect();
        }

        Channel channel = session.openChannel("sftp");
        Log.v(TAG, "uploadFiles: " + channel);
        channel.setInputStream(null);
        try {


            channel.connect();
            ChannelSftp channelSftp = (ChannelSftp) channel;

            for (File file : localFiles) {
                channelSftp.put(file.getPath(), simplifiedPath);
            }

            channelSftp.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "uploadFiles: " + e.getMessage());
        } finally {
            channel.disconnect();
        }
    }

    public void lsRemoteFiles(Session session, TaskCallbackHandler taskCallbackHandler, String path) {
        mCurrentPath = path == null || path == "" ? mCurrentPath : mCurrentPath + path + "/";
        new LsTask(session, taskCallbackHandler).execute();
    }
    private class LsTask extends AsyncTask<Void, Void, Boolean> {


        private Vector<ChannelSftp.LsEntry> mRemoteFiles;


        private TaskCallbackHandler mTaskCallbackHandler;


        private Session mSession;


        public LsTask(Session session, TaskCallbackHandler tch) {

            mSession = session;
            mTaskCallbackHandler = tch;
        }

        @Override
        protected void onPreExecute() {
            if(mTaskCallbackHandler != null)
                mTaskCallbackHandler.OnBegin();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Log.d(TAG, "current path is " + mCurrentPath);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Path normalizedPath = Paths.get(mCurrentPath).normalize();
                simplifiedPath = normalizedPath.toString();
                Log.d(TAG,simplifiedPath);
            }


            boolean success = true;
            Channel channel = null;
            try {
                mRemoteFiles = null;
                if (true) {
                    channel = mSession.openChannel("sftp");
                    channel.setInputStream(null);
                    channel.connect();

                    ChannelSftp channelsftp = (ChannelSftp) channel;
                    String path = mCurrentPath == null ? "/" : mCurrentPath;
                    mRemoteFiles = channelsftp.ls(path);
                    if (mRemoteFiles == null) {
                        Log.d(TAG, "remote file list is null");
                    } else {
                        for (ChannelSftp.LsEntry e : mRemoteFiles) {
                            //Log.v(TAG," file "+ e.getFilename());
                        }
                    }
                }
            } catch (Exception e) {
                Log.v(TAG, "sftprunnable exptn " + e.getCause());
                success = false;
                return success;
            }
            if (channel != null) {
                channel.disconnect();
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                if (mTaskCallbackHandler != null) {
                    mTaskCallbackHandler.onTaskFinished(mRemoteFiles);
                }
            } else {
                if (mTaskCallbackHandler != null) {
                    mTaskCallbackHandler.onFail();
                }
            }
        }
    }

    public void downloadFile(Session session, String srcPath, String out, SftpProgressMonitor spm) throws JSchException, SftpException {
        if (session == null || !session.isConnected()) {
            session.connect();
        }

        Channel channel = session.openChannel("sftp");
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.connect();
        sftpChannel.get(srcPath, out, spm, ChannelSftp.OVERWRITE);
        sftpChannel.disconnect();
    }
    public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

        Session mSession;
        String mSrcPath;
        String mOut;
        SftpProgressMonitor mSpm;


        public DownloadTask(Session session, String srcPath, String out, SftpProgressMonitor spm) {
            mSession = session;
            mSrcPath = srcPath;
            mOut = out;
            mSpm = spm;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Boolean result = false;
            try {
                Log.v(TAG, " path " + mSrcPath + ", out path " + mOut);
                downloadFile(mSession, mCurrentPath + mSrcPath, mOut, mSpm);
                result = true;

            } catch (Exception e) {
                Log.e(TAG, "EXCEPTION " + e.getMessage());

            } finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success == null) return;
            if (success) {
                mSpm.end();
            }
        }
    }



}
