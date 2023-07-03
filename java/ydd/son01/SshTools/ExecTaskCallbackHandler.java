package ydd.son01.SshTools;

public interface ExecTaskCallbackHandler
{
    void onFail();
    void onComplete(String completeString);
}
