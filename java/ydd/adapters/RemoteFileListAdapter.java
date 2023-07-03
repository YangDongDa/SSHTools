package ydd.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcraft.jsch.ChannelSftp;

import java.util.Vector;

import ydd.yddson02.myapplication.R;

public class RemoteFileListAdapter extends BaseAdapter {
    private Vector<ChannelSftp.LsEntry> mFiles;
    private Context mContext;
    private LayoutInflater mInflater;

    //
    // Constructor
    //
    public RemoteFileListAdapter(Context context, Vector<ChannelSftp.LsEntry> files) {
        mContext = context;
        mFiles = files;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return mFiles.size();
    }
//    重写了getCount()方法，返回远程文件列表的大小，即文件数量。

    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

//    适配器实现了getView()方法，用于创建每个列表项的视图，并填充相应的数据。
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

        holder.textView.setText(mFiles.get(position).getFilename());
        if (mFiles.get(position).getAttrs().isDir()) {
            holder.color = 0xff0099ff;
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


    public Vector<ChannelSftp.LsEntry> getRemoteFiles(){
        return mFiles;
    }

}
