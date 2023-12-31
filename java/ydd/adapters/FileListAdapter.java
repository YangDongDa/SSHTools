package ydd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import ydd.yddson02.myapplication.R;

public class FileListAdapter extends BaseAdapter {


    private ArrayList<File> mFiles;
    private Context mContext;
    private LayoutInflater mInflater;

    //
    // Constructor
    //
    public FileListAdapter(Context context, ArrayList<File> files) {
        mContext = context;
        mFiles = files;

        //获取LayoutInflater对象,这样就可以使用inflate()方法来动态创建布局
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mFiles.size();
    }

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.listview_item, null);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.imageview_item);
            TextView textView = (TextView) convertView.findViewById(R.id.textview_item);
            holder.textView = textView;
            holder.imageView = imageView;
            // change text color for directories
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.textView.setText(mFiles.get(position).getName());
        if (mFiles.get(position).isDirectory()) {
            holder.color = 0xff009999;
            holder.imageView.setImageResource(R.drawable.baseline_drive_file_move_24);
        }
        else{
            holder.color = 0xffff8888;
            holder.imageView.setImageResource(R.drawable.baseline_insert_drive_file_24);
        }

        holder.textView.setTextColor(holder.color);

        return convertView;
    }




    private class ViewHolder {
        ImageView imageView;
        TextView textView;
        int color;
    }

}
