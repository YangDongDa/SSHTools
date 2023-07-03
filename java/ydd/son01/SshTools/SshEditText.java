package ydd.son01.SshTools;

import android.content.Context;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

public class SshEditText extends EditText {
    private String mlastInput;

    public long count = 0;

    //设置提示符
    private String mPrompt = "";
    public SshEditText(Context context) {
        super(context);
        setup();
    }

    public SshEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public SshEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setup();
    }

    public SshEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setup();
    }


    //设置输入类型
    public void setup(){
        this.setRawInputType(InputType.TYPE_CLASS_TEXT);//设置输入类型
        this.setImeOptions(EditorInfo.IME_ACTION_GO);//设置输入完成后的动作
        this.setTextSize(12f);//设置字体大小
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        //onSelectionChanged()方法是在选中文本时调用的，这里我们重写这个方法，使得选中文本时，始终是光标在最后
        //将方法中的参数selStart和selEnd都设置为getText().length()，这样就可以保证光标始终在最后
        //super.onSelectionChanged(selStart, selEnd);
        //强制将光标设置在最后
        setSelection(getText().length());//设置光标位置,这里是在最后,也就是光标始终在最后
    }


    //获取当前光标所在行
    public int getCurrentCursorLine() {
        //获取光标所在位置，即用户输入的最后一行，如果没有输入，则返回-1，即没有输入
        //Selection.getSelectionStart()方法是获取光标的起始位置,起始位置是指光标所在的位置
        int selectionStart = Selection.getSelectionStart(this.getText());
        //获取布局，即获取文本的布局，即文本的行数。
        Layout layout = this.getLayout();

        if (!(selectionStart == -1)) {
            //返回光标所在行，layout.getLineForOffset(selectionStart)方法是获取光标所在行
            return layout.getLineForOffset(selectionStart);
        }

        return -1;
    }


    public String getLastInput() {
        //synchronized关键字是Java中的同步关键字，它可以保证在同一时刻，只有一个线程执行某个方法或某个代码块。
        synchronized (this) {
            //返回用户输入的最后一行
            String rez = mlastInput;
            mlastInput = null;
            return rez;
        }
    }

    //添加用户输入的最后一行
    //synchronized关键字是Java中的同步关键字，它可以保证在同一时刻，只有一个线程执行某个方法或某个代码块。
    //如果mlastInput为null,则将s赋值给mlastInput
    public void AddLastInput(String s) {
        synchronized (this) {
            if (mlastInput == null) {
                mlastInput = "";
            }
            mlastInput = s;
        }
    }

    //判断是否是新的一行
    public boolean isNewLine() {

        int i = this.getText().toString().toCharArray().length;
        if(i == 0)
            return true;

        char s = this.getText().toString().toCharArray()[i - 1];
        if (s == '\n' || s == '\r') return true;

        return false;
    }


    public synchronized void setPrompt(String prompt){
        mPrompt = prompt;
    }

    public synchronized String getPrompt(){
        return mPrompt;
    }

    @Override
    //返回一个InputConnection对象，这个对象是用来与输入法进行交互的
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        return super.onCreateInputConnection(outAttrs);
        //这里我们重写onCreateInputConnection()方法，返回一个InputConnection对象，这个对象是用来与输入法进行交互的,这里我们返回null，表示不与输入法进行交互,这样就可以禁止输入法弹出
        return new SshConnectionWrapper(super.onCreateInputConnection(outAttrs),
                true);
    }

//这里我们重写onCreateInputConnection()方法，返回一个InputConnection对象，这个对象是用来与输入法进行交互的,这里我们返回null，表示不与输入法进行交互,这样就可以禁止输入法弹出
   private class SshConnectionWrapper extends InputConnectionWrapper {
        //inputConnectionWrapper是一个包装类，它可以包装一个InputConnection对象，然后对这个InputConnection对象进行一些操作，比如我们可以在这个包装类中对用户输入的内容进行过滤，或者对用户输入的内容进行一些处理，
    // 然后再返回给InputConnection对象，这样就可以对用户输入的内容进行一些处理了，
    // 这里我们重写这个包装类，然后在这个包装类中对用户输入的内容进行过滤，然后再返回给InputConnection对象，这样就可以禁止用户输入了
    //

        public SshConnectionWrapper(InputConnection target, boolean mutable) {
            //调用父类的构造方法
            super(target, mutable);
        }


        //
        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            //sendKeyEvent()方法是在用户按下按键时调用的，这里我们重写这个方法，当用户按下回车键时，我们将用户输入的内容添加到mlastInput中，然后将用户输入的内容清空，这样就可以实现用户输入的内容不显示在屏幕上了
//            ACTION_DOWN表示按下，ACTION_UP表示抬起
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//                    //获取用户输入的内容
//                    String s = getText().toString();
//                    //将用户输入的内容添加到mlastInput中
//                    AddLastInput(s);
//                    //将用户输入的内容清空
//                    setText("");
//                    return true;
//                }
//            }

            //如果用户按下del键，且当前光标不是新的一行，且当前光标不是在最后一行，则不允许用户输入
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL) {

                //如果当前光标不是新的一行
                if(isNewLine()) {
                    return false;

                }
                //如果当前光标不是在最后一行
                else if(getCurrentCursorLine() < getLineCount() - 1){
                    return false;
                }
            }

            //允许用户输入
            return super.sendKeyEvent(event);
        }

        //如果光标是在新的一行，并且是在最后一行，则允许删除环绕文本
        public boolean deleteSurroundingText (int beforeLength, int afterLength){

            if(isNewLine()) {
                return false;

            }

            //getLineCount() - 1表示最后一行
            else if(getCurrentCursorLine() < getLineCount() - 1){
                return false;
            }

            else {
                return super.deleteSurroundingText(beforeLength, afterLength);
            }
        }

    }
}