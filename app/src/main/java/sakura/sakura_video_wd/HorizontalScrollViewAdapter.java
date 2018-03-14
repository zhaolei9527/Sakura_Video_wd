package sakura.sakura_video_wd;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * sakura.sakura_video_wd
 *
 * @author 赵磊
 * @date 2018/2/8
 * 功能描述：
 */
public class HorizontalScrollViewAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mDatas;

    public HorizontalScrollViewAdapter(Context context, List<String> mDatas) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        this.mDatas = mDatas;
    }

    public int getCount() {
        return mDatas.size();
    }

    public List<String> getData() {
        return mDatas;
    }

    public Object getItem(int position) {
        return mDatas.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(
                    R.layout.activity_index_gallery_item, parent, false);
            viewHolder.mImg = (ImageView) convertView
                    .findViewById(R.id.id_index_gallery_item_image);
            viewHolder.mText = (TextView) convertView
                    .findViewById(R.id.id_index_gallery_item_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        try {
            viewHolder.mImg.setImageURI(Uri.parse("file://" + mDatas.get(position)));
            viewHolder.mText.setText("some info ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    private class ViewHolder {
        ImageView mImg;
        TextView mText;
    }

}
