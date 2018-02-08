package sakura.sakura_video_wd;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * sakura.sakura_video_wd
 *
 * @author 赵磊
 * @date 2017/11/19
 * 功能描述：
 */
public class SettingActivity extends Activity implements View.OnClickListener {
    private SpinerPopWindow<Integer> mSpinerPopWindow;
    private List<Integer> list1;
    private List<Integer> list2;
    private List<Integer> list3;


    private LinearLayout ll_miaozhen;
    private LinearLayout ll_miaohuifang;
    private TextView tv_miaozhen;
    private TextView tv_miaohuifang;

    private int zhen = 26;

    private int huifang = 60;
    private Button btn_check;
    private LinearLayout ll_yanshi;
    private TextView tv_yanshi;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        initView();
    }

    private void initView() {
        ll_miaozhen = (LinearLayout) findViewById(R.id.ll_miaozhen);
        ll_miaohuifang = (LinearLayout) findViewById(R.id.ll_miaohuifang);
        ll_yanshi = (LinearLayout) findViewById(R.id.ll_yanshi);

        list1 = new ArrayList<Integer>();
        list2 = new ArrayList<Integer>();
        list3 = new ArrayList<Integer>();

        ll_miaozhen.setOnClickListener(this);
        ll_miaohuifang.setOnClickListener(this);
        ll_yanshi.setOnClickListener(this);

        tv_miaozhen = (TextView) findViewById(R.id.tv_miaozhen);
        tv_miaohuifang = (TextView) findViewById(R.id.tv_miaohuifang);
        tv_yanshi = (TextView) findViewById(R.id.tv_yanshi);


        int time = (int) SPUtil.get(SettingActivity.this, "TIME", 2000);
        tv_miaozhen.setText("设置每秒帧:当前" + time + "帧每秒");

        int max = (int) SPUtil.get(SettingActivity.this, "MAX", 30);
        tv_miaohuifang.setText("设置最大回放秒：当前" + max + "秒");

        int yanshi = (int) SPUtil.get(SettingActivity.this, "YANSHI", 30);
        tv_yanshi.setText("设置暂停延时截图：当前" + yanshi + "秒");

        list3.add(5);
        list3.add(10);
        list3.add(15);
        list3.add(20);

        for (int i = 1; i < zhen; i++) {
            list1.add(i);
        }

        for (int i = 10; i < huifang; i++) {
            list2.add(i);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_miaozhen:
                mSpinerPopWindow = new SpinerPopWindow<Integer>(this, list1, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSpinerPopWindow.dismiss();
                        tv_miaozhen.setText("设置每秒帧:当前" + list1.get(position) + "帧每秒");
                        SPUtil.putAndApply(SettingActivity.this, "TIME", list1.get(position));
                    }
                });
                mSpinerPopWindow.setWidth(ll_miaozhen.getWidth());
                mSpinerPopWindow.showAsDropDown(ll_miaozhen);
                break;
            case R.id.ll_miaohuifang:
                mSpinerPopWindow = new SpinerPopWindow<Integer>(this, list2, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSpinerPopWindow.dismiss();
                        tv_miaohuifang.setText("设置最大回放秒：当前" + list2.get(position) + "秒");
                        SPUtil.putAndApply(SettingActivity.this, "MAX", list2.get(position));

                    }
                });
                mSpinerPopWindow.setWidth(ll_miaohuifang.getWidth());
                mSpinerPopWindow.showAsDropDown(ll_miaohuifang);
                break;
            case R.id.ll_yanshi:
                mSpinerPopWindow = new SpinerPopWindow<Integer>(this, list3, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSpinerPopWindow.dismiss();
                        tv_yanshi.setText("设置暂停延时截图：当前" + list3.get(position) + "秒");
                        SPUtil.putAndApply(SettingActivity.this, "YANSHI", list3.get(position));
                    }
                });
                mSpinerPopWindow.setWidth(ll_yanshi.getWidth());
                mSpinerPopWindow.showAsDropDown(ll_yanshi);
                break;
            default:
                break;
        }
    }
}
