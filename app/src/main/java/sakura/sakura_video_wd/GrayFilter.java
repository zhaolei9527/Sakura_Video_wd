package sakura.sakura_video_wd;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

/**
 * sakura.sakura_video_wd
 *
 * @author 赵磊
 * @date 2017/12/20
 * 功能描述：
 */
public class GrayFilter {
    // 黑白效果函数
    public static Bitmap changeToGray(Bitmap bitmap) {
        int width, height;
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        Bitmap grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(grayBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true); // 设置抗锯齿

        //一，数组矩阵的方法
        /*float[] array = {1, 0, 0, 0, 100,
                         0, 1, 0, 0, 100,
0, 0, 1, 0, 0,
0, 0, 0, 1, 0};
ColorMatrix colorMatrix = new ColorMatrix(array);
*/

        //二，把饱和度设置为0 就可以得到灰色（黑白)的图片
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return grayBitmap;
    }
}
