package sakura.sakura_video_wd;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;
import java.util.List;

/**
 * sakura.sakura_video_wd
 *
 * @author 赵磊
 * @date 2017/11/14
 * 功能描述：
 */
public class UserListActivity extends Activity {
    private SwipeMenuListView listView;
    private List<String> data;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userlist_layout);
        listView = (SwipeMenuListView) findViewById(R.id.listView);
        String userMessage="aaa*123456#bbb*784536#ccc*784536#ddd*784536#eee*784536";
        initData();
    }

    //初始化ListView数据，在OnCreate方法中调用
    private void initData() {
        data = new ArrayList<>(20);
        final CollectorListAdapter adapter = new CollectorListAdapter(UserListActivity.this, data);
        listView.setAdapter(adapter);
        // step 1. create a MenuCreator
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(UserListActivity.this);
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9, 0xCE)));
                // set item width
                openItem.setWidth(dp2px(90));
                // set item title
                openItem.setTitle("修改");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(UserListActivity.this);
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.mipmap.icon);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        // set creator
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // open
                        open(position);
                        break;
                    case 1:
                        // delete
                        delete(position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Toast.makeText(UserListActivity.this, data.get(position).toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * 打开
     *
     * @param item
     */
    private void open(int item) {
        Toast.makeText(this, data.get(item).toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 删除
     *
     * @param item
     */
    private void delete(int item) {
        Toast.makeText(this, data.get(item).toString(), Toast.LENGTH_SHORT).show();


    }


}
