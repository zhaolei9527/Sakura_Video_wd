package sakura.sakura_video_wd;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by 赵磊 on 2017/5/11.
 */

public class WindowUtil {

    private WindowUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 设置当前界面为全屏模式
     */
    public static void setFullScreen(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * 如果当前为全屏，那么取消全屏模式，回到正常的模式
     */
    public static void cancelFullScreen(Activity activity) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 判断当前手机是否是全屏
     *
     * @return 如果是true，那么当前就是全屏
     */
    public static boolean isFullScreen(Activity activity) {
        int flag = activity.getWindow().getAttributes().flags;
        if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN)
                == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断当前屏幕是否是横屏
     *
     * @param activity 当前的activity
     * @return 如果true就是竖屏
     */
    public static boolean isVerticalScreen(Activity activity) {
        int flag = activity.getResources().getConfiguration().orientation;
        if (flag == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取顶部状态栏高度
     *
     * @return 顶部状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        java.lang.reflect.Field field = null;
        int x = 0;
        int statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
            return statusBarHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }

    /**
     * 推荐的获取屏幕长宽的方式,但需要API13
     *
     * @return 装载了屏幕长宽的数组，int[0] = width,int[1] = height
     */
    @SuppressLint("NewApi")
    public static int[] getWindow_WH(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return new int[]{size.x, size.y};
    }

    /**
     * 获取屏幕长宽的方式(仅在低版本中使用)
     *
     * @return 装载了屏幕长宽的数组，int[0] = width,int[1] = height
     */
    @Deprecated
    public static int[] getWindow_wh(Activity activity) {
        int w = activity.getWindowManager().getDefaultDisplay().getWidth();//获得手机屏幕的宽度
        int h = activity.getWindowManager().getDefaultDisplay().getHeight();//获得手机屏幕的高度
        return new int[]{w, h};

    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 显示正在加载动画
     *
     * @param context
     */
    public static Dialog showLoadingDialog(final Activity context) {
        //创建Dialog并传递style文件
        final Dialog dialog = new Dialog(context, R.style.dialog);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        // 设置它的ContentView
        dialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
        dialog.setContentView(R.layout.dialog_loading_layout);
        try {
            dialog.show();//显示dialog
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dialog;
    }

}