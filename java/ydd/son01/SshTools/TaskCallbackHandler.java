package ydd.son01.SshTools;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

public interface TaskCallbackHandler {
    public void OnBegin();
    public void onFail();
    public void onTaskFinished(Vector<ChannelSftp.LsEntry> lsEntries);
}
