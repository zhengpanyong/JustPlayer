package com.example.zu.myapp.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

/**
 * Created by zu on 2016/4/26.
 */
public class BitmapBlur
{
    public static Bitmap blur(Bitmap pic)
    {
        Bitmap outBitmap=Bitmap.createBitmap(pic.getWidth(),pic.getHeight(), Bitmap.Config.ARGB_8888);
        RenderScript renderScript=RenderScript.create(MyApplication.getContext());
        ScriptIntrinsicBlur intrinsicBlur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        Allocation allIn = Allocation.createFromBitmap(renderScript, pic);
        Allocation allOut = Allocation.createFromBitmap(renderScript, outBitmap);
        intrinsicBlur.setRadius(24f);

        intrinsicBlur.setInput(allIn);
        intrinsicBlur.forEach(allOut);

        allOut.copyTo(outBitmap);

        Bitmap outBitmap1=Bitmap.createBitmap(pic.getWidth(), pic.getHeight(), Bitmap.Config.ARGB_8888);
        allIn = Allocation.createFromBitmap(renderScript, outBitmap);
        allOut = Allocation.createFromBitmap(renderScript, outBitmap1);
        intrinsicBlur.setRadius(25f);
        intrinsicBlur.setInput(allIn);
        intrinsicBlur.forEach(allOut);
        allOut.copyTo(outBitmap1);

        outBitmap.recycle();

        renderScript.destroy();


        float light=0;//亮度
        float contrast=0.6f;//对比度
        float saturation=0f;//饱和度
        float alpha=1f;
        Bitmap resultBitmap=Bitmap.createBitmap(outBitmap.getWidth(),outBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        ColorMatrix colorMatrix=new ColorMatrix();


        colorMatrix.set(new float[]{contrast, 0, 0, saturation, light,
                0, contrast, 0, saturation, light,
                0, 0, contrast, saturation, light,
                0, 0, 0, alpha, 0});
        //colorMatrix.setSaturation(0.5f);

        Paint paint=new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        Canvas canvas=new Canvas(resultBitmap);
        canvas.drawBitmap(outBitmap1,0,0,paint);
        outBitmap1.recycle();
        return resultBitmap;
    }
}
