package ydd.son01.SshTools;

import com.jcraft.jsch.UserInfo;

public class SessionUserInfo implements UserInfo {
    private final String mPassword;
    private final String mUser;
    private final String mHost;
    private final int mPort;

    public SessionUserInfo(String user, String host, String password, int port) {
        mUser = user;
        mHost = host;
        mPassword = password;
        mPort = port;
    }

    public String getPassphrase() {
        // TODO
        return null;
    }

    public String getUser() {
        return mUser;
    }

    public String getHost() {
        return mHost;
    }

    public int getPort() {
        return mPort;
    }

    public String getPassword() {
        return mPassword;
    }

    @Override
    public boolean promptPassword(String message) {
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        return false;
    }

    @Override
    public void showMessage(String message) {

    }

}
