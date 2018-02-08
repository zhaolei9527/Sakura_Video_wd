package sakura.sakura_video_wd;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import static android.content.ContentValues.TAG;

/**
 * Created by 赵磊 on 2017/10/19.
 */

public class ZoomListenter implements OnTouchListener {
    int screenWidth;
    int screenHeight;
    private int mode = 0;
    float oldDist;
    int width = 0;
    int height = 0;
    View v = null;
    private int dx;
    private int dy;
    Context context;

    public ZoomListenter(int minWidth, int minHeight, Context context) {
        this.width = minWidth;
        this.height = minHeight;
        this.context = context;
        screenWidth = WindowUtil.getScreenWidth(context);
        screenHeight = WindowUtil.getScreenHeight(context);
        if (screenHeight == 1776) {
            screenHeight = WindowUtil.getScreenHeight(context) + 144;
        }

    }

    int startX;
    int startY;
    int lastX;
    int lastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.v = (View) v;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                mode = 1;
                break;
            case MotionEvent.ACTION_UP:
                mode = 0;
                if ((int) event.getRawX() - startX > 20 || (int) event.getRawY() - startY > 20) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode -= 1;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mode += 1;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode >= 2) {
                    float newDist = spacing(event);
                    Log.d("ZoomListenter", "newDist:" + newDist);
                    Log.d("ZoomListenter", "oldDist:" + oldDist);
                    if (newDist > oldDist + 1) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                    if (newDist < oldDist - 1) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                } else {
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    setLayout(v, (lastX - startX) / 10, (lastY - startY) / 10);
                    return true;
                }
                break;
        }
        return false;
    }

    private int lastLeft = 0;
    private int lastTop = 0;
    private int lastRight = 0;
    private int lastBottom = 0;

    public void setLayout(View v, int dx, int dy) {
        lastLeft = v.getLeft();
        lastTop = v.getTop();
        lastRight = v.getRight();
        lastBottom = v.getBottom();

        int left = v.getLeft();
        int top = v.getTop();
        int right = v.getRight();
        int bottom = v.getBottom();

        if (dx > 0) {
            Log.e(TAG, "右");
            left = left + dx;
            right = right + dx;
            if (left >= 0) {
                left = v.getLeft();
                right = right - dx;
            }
        }

        if (dx < 0) {
            Log.e(TAG, "左");
            right = right + dx;
            left = left + dx;
            if (right <= screenWidth) {
                right = screenWidth;
                left = left - dx;
            }
        }

        if (dy > 0) {
            Log.e(TAG, "下");
            top = top + dy;
            bottom = bottom + dy;
            if (top >= 0) {
                top = v.getTop();
                bottom = bottom - dy;
            }
        }
        if (dy < 0) {
            Log.e(TAG, "上");
            bottom = bottom + dy;
            top = top + dy;
            if (bottom <= screenHeight) {
                bottom = screenHeight;
                top = top - dy;
            }
        }
        v.layout(left, top, right, bottom);
    }

    private void zoom(float f) {
        ViewGroup.LayoutParams layoutParams = v.getLayoutParams();

        layoutParams.width = (int) (layoutParams.width * f);
        layoutParams.height = (int) (layoutParams.height * f);

        if (layoutParams.width > 4800) {
            layoutParams.width = 4800;
        }

        if (layoutParams.height > 6800) {
            layoutParams.height = 6800;
        }

        if (layoutParams.width < width) {
            layoutParams.width = width;
        }

        if (layoutParams.height < height) {
            layoutParams.height = height;
        }

        if (layoutParams.height == height && layoutParams.width == width) {
            SPUtil.putAndApply(context, "isbig", "1");
        } else {
            SPUtil.putAndApply(context, "isbig", "0");
        }

        v.setLayoutParams(layoutParams);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

}