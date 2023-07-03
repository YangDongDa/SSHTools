package ydd.son01.SshTools;


import android.os.Handler;
import android.util.Log;
import android.widget.EditText;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellController {
    public static final String TAG = "ShellController";
    private BufferedReader mBufferedReader;
    private DataOutputStream mDataOutputStream;
    private Channel mChannel;
    private String mSshText = null;
    private int count = 0;
    private boolean ifClear = false;


    public ShellController() {
        //nothing
    }

    public DataOutputStream getDataOutputStream() {
        return mDataOutputStream;
    }//取得資料輸出串流


    public synchronized void disconnect() throws IOException {
        try {
            Log.v(TAG, "close shell channel");
            //disconnect channel
            if (mChannel != null)//如果通道不是空的
                mChannel.disconnect();//關閉通道

            Log.v(TAG, "close streams");
            //close streams
            mDataOutputStream.flush();
            mDataOutputStream.close();
            mBufferedReader.close();

        }catch(Throwable t){
            Log.e(TAG, "Exception: "+t.getMessage());
        }
    }


    public void writeToOutput(String command) {
        if (mDataOutputStream != null) {
            try {
                Log.v(TAG, "write to output: " + command);
                if (command.equalsIgnoreCase("clear")) ifClear = true;
                else ifClear = false;
                mDataOutputStream.writeBytes(command + "\r\n");
                //在这里加入了\r\n，这样就可以在命令行中输入回车了
                count = 0;
                mDataOutputStream.flush();//刷新，不然不会立即执行命令
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void openShell(Session session, Handler handler, EditText editText) throws JSchException, IOException {
        if (session == null) throw new NullPointerException("Session cannot be null!");
        if (!session.isConnected()) throw new IllegalStateException("Session must be connected.");
        final Handler myHandler = handler;
        final EditText myEditText = editText;
        mChannel = session.openChannel("shell");
        mChannel.connect();

        mBufferedReader = new BufferedReader(new InputStreamReader(mChannel.getInputStream()));
        mDataOutputStream = new DataOutputStream(mChannel.getOutputStream());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;

                    while (true) {
                        while ((line = mBufferedReader.readLine()) != null) {
                            count++;
                            if (count == 1) continue;
                            final String result = line;
                            if (mSshText == null) mSshText = result;
                            myHandler.post(new Runnable() {
                                public void run() {
                                    synchronized (myEditText) {
                                        ((SshEditText)myEditText).setPrompt(result);//设置提示符为当前行，最终它将是最后一行。
//                                        AnsiConsole.systemInstall();
//                                        Ansi ansi = Ansi.ansi().eraseScreen();
//                                        myEditText.setText(myEditText.getText().toString() + "\r\n" + result/*+ "\r\n"+fetchPrompt(result)*/);
                                        handleAnsiEscapeCodes(result,(SshEditText) myEditText);
//                                        Log.d(TAG, "LINE : " + result);
//                                        Log.d(TAG, "LINE : " + ansi.render(result));

                                    }
                                }
                            });
                        }

                    }
                } catch (Exception e) {
                    Log.e(TAG, " Exception " + e.getMessage() + "." + e.getCause() + "," + e.getClass().toString());
                }
            }
        }).start();
    }
    private void handleAnsiEscapeCodes(String line,SshEditText editText){
        //转义字符处理,去掉转义字符
        String[] parts = line.split("\\033\\[[0-9;]+m");
        int lastIndex = parts.length - 1;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i]);
            if (i < lastIndex) {
                sb.append(" ");
            }
        }
        String text = sb.toString();
        // Define color codes
        final String RESET_COLOR = "\\u001B\\[0m";
        final String BLACK_COLOR = "\\u001B\\[30m";
        final String RED_COLOR = "\\u001B\\[31m";
        final String GREEN_COLOR = "\\u001B\\[32m";
        final String YELLOW_COLOR = "\\u001B\\[33m";
        final String BLUE_COLOR = "\\u001B\\[34m ";
        final String MAGENTA_COLOR = "\\u001B\\[35m";
        final String CYAN_COLOR = "\\u001B\\[36m";
        final String WHITE_COLOR = "\\u001B\\[37m";

        // Define corresponding Android color span codes
        final String BLACK_SPAN = "<font color='#000000'>";
        final String RED_SPAN = "<font color='#FF0000'>";
        final String GREEN_SPAN = "<font color='#008000'>";
        final String YELLOW_SPAN = "<font color='#FFA500'>";
        final String BLUE_SPAN = "<font color='#0000FF'>";
        final String MAGENTA_SPAN = "<font color='#FF00FF'>";
        final String CYAN_SPAN = "<font color='#00FFFF'>";
        final String WHITE_SPAN = "<font color='#FFFFFF'>";
        text = text.replaceAll(RESET_COLOR, "</font>")
                .replaceAll(BLACK_COLOR, BLACK_SPAN)
                .replaceAll(RED_COLOR, RED_SPAN)
                .replaceAll(GREEN_COLOR, GREEN_SPAN)
                .replaceAll(YELLOW_COLOR, YELLOW_SPAN)
                .replaceAll(BLUE_COLOR, BLUE_SPAN)
                .replaceAll(MAGENTA_COLOR, MAGENTA_SPAN)
                .replaceAll(CYAN_COLOR, CYAN_SPAN)
                .replaceAll(WHITE_COLOR, WHITE_SPAN);
        if (!ifClear) {
            editText.setText(editText.getText().toString() + "\r\n" + text/*+ "\r\n"+fetchPrompt(result)*/);
        }else if (ifClear){
            String pattern = "\033\\[H\033\\[J";
            String[] groups = text.split(pattern);

            String ll = groups[1];
            char[] chars = ll.toCharArray();

            int i = 0;
            StringBuilder stringBuilder = new StringBuilder();
            while (chars[i] == 0){
                i++;
            }
            for (int j = i; j < chars.length;j++){
                stringBuilder.append(chars[j]);
            }
            editText.setText(stringBuilder.toString());

        }
        Log.d(TAG, "LINE : " + text);

    }
}
