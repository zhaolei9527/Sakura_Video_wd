package sakura.sakura_video_wd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * sakura.sakura_video_wd
 *
 * @author 赵磊
 * @date 2017/11/14
 * 功能描述：
 */
public class CollectorListAdapter extends BaseAdapter {

    private List<String> listItems;//数据集合
    private LayoutInflater layoutinflater;//视图容器，用来导入布局

    static class ViewHolder {
        private TextView tv_name;
        private TextView tv_id;
    }

    /*
     * 实例化Adapter
     */
    public CollectorListAdapter(Context context, List<String> dataSet) {
        this.listItems = dataSet;
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public Object getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View view;
        if (convertView == null) {
            holder = new ViewHolder();
            //获取listitem布局文件
            view = layoutinflater.inflate(R.layout.collectorlist_listitem, null);
            //获取控件对象
            holder.tv_name = (TextView) view.findViewById(R.id.tv_name);
            holder.tv_id = (TextView) view.findViewById(R.id.tv_id);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        return convertView;
    }

}