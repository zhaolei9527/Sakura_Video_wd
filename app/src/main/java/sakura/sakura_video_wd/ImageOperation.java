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
 * @date 2017/12/12
 * 功能描述：
 */
public class ImageOperation {

    //传入需要修改的Bitmap和色彩三元素
    public static Bitmap imageoperation(Bitmap mbitmap, float hue, float saturation, float lum) {
        try {
            //传入的Bitmap默认不可修改，需啊哟创建新的Bitmap
            Bitmap mbitmap_fu = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), Bitmap.Config.ARGB_8888);
            //创建画布，在新的bitmap上绘制
            Canvas canvas = new Canvas(mbitmap_fu);
            //设置画笔抗锯齿，后面在Bitmap上绘制需要使用到画笔
            Paint mpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            ColorMatrix huematrix = new ColorMatrix();
            huematrix.setRotate(0, hue);
            huematrix.setRotate(1, hue);
            huematrix.setRotate(2, hue);
            ColorMatrix saturationmatrix = new ColorMatrix();
            saturationmatrix.setSaturation(saturation);
            ColorMatrix lummatrix = new ColorMatrix();
            //参数：rscale gscale bscale 透明度
            lummatrix.setScale(lum, lum, lum, 1);
            ColorMatrix imagematrix = new ColorMatrix();
            imagematrix.postConcat(huematrix);
            imagematrix.postConcat(saturationmatrix);
            imagematrix.postConcat(lummatrix);
            //通过画笔的setColorFilter进行设置
            mpaint.setColorFilter(new ColorMatrixColorFilter(imagematrix));
            canvas.drawBitmap(mbitmap, 0, 0, mpaint);
            return mbitmap_fu;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mbitmap;
    }

}
