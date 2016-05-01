package com.example.zu.myapp.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.example.zu.myapp.R;
import com.example.zu.myapp.util.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by zu on 2016/4/18.
 */
/*
* TODO:
* setOnLocationChangeListener(OnLocationChangeListener)
* setTime(int time)(单位：ms)
* 手势等操作
* 要在左边画三角箭头以及在视图上画一条准线
* */
public class LrcView extends View implements View.OnTouchListener{

    //不在播放状态的文字颜色
    private int baseTextColor=Color.GRAY;
    //在播放状态的文字颜色
    private int isPlayingTextColor=Color.WHITE;
    private int textSize=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,16,getResources().getDisplayMetrics());


    //每一句歌词之间的空隙
    private int textPadding=textSize;
    //同一句歌词不同行之间的空隙，针对那些一句歌词太长需要两行显示的情况
    private int changeLinePadding=6;
    //整个歌词的上边界，是基准值
    private int topEdge;
//    在touch事件中的一些值
    private float oldX=0,oldY=0,newX=0,newY=0,dx=0,dy=0;
/*
* 时间标签和距离的队列，距离指的是每一句歌词的分割线距离topEdge的距离
* */
    private ArrayList<Integer> timeStamps=new ArrayList<>();
    private ArrayList<Integer> dis=new ArrayList<>();
    private TreeMap<Integer,String> lrc=new TreeMap<>();
    private Rect mRect;
    private Paint mPaint;
//    歌词部分的宽度
    private int paintWidth=0;
    private int padding=30;
    private Rect triRect;
//    现在正在播放歌词的序号
    private int nowLocation=0;
    private int oldLocation=0;
//    监听器队列
    private ArrayList<OnLocationChangeListener> listeners=new ArrayList<>();
//    监听器
    public interface OnLocationChangeListener
    {
        void onLocationChange(int time);
    }


    public void setOnLocationChangeListener(OnLocationChangeListener listener)
    {
        if(listeners==null)
        {
            listeners=new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeOnLocationChangeListener(OnLocationChangeListener listener)
    {
        if(listeners!=null)
        {
            listeners.remove(listener);
        }
    }

    private void notifyAll(int time)
    {
        if(listeners!=null)
        {
            for(int i=0;i<listeners.size();i++)
            {
                listeners.get(i).onLocationChange(time);
            }
        }
    }

    public LrcView(Context context)
    {
        this(context, null);
    }

    public LrcView(Context context,AttributeSet attrs)
    {
        this(context, attrs, 0);
        //this(context,attrs,0);
    }

    public LrcView(Context context,AttributeSet attrs,int defStyle)
    {
        super(context, attrs, defStyle);
        TypedArray array=context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomView1,defStyle,0);
        for(int i=0;i<array.getIndexCount();i++)
        {
            int attr=array.getIndex(i);
            switch (attr)
            {
                case R.styleable.CustomView1_baseTextColor:
                    baseTextColor=array.getColor(attr, Color.GRAY);
                    break;
                case R.styleable.CustomView1_textSize:
                    textSize=array.getDimensionPixelSize(attr,(int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_SP, 60, getResources().getDisplayMetrics()));
                    break;
                case R.styleable.CustomView1_isPlayingTextColor:
                    isPlayingTextColor=array.getColor(attr,Color.WHITE);
            }
        }
        array.recycle();






    }

    /*
    * 在measure中确定出定位三角形的大小和位置，以及歌词部分的宽度
    * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        paintWidth=getWidth()-2*padding;
        triRect=new Rect(0,getHeight()/2-10,20,getHeight()/2+10);

    }

    /*
    * 只画出在屏幕范围内的歌词，这样可以减少开销
    * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        textPadding=textSize;
        /*
        * 每一句歌词的基准线为该句歌词的下边界，因此画当前歌词时，需要得到上一句歌词的基准线的dis，如果当前就是第一句歌词
        * 那么以topEdge为上一句歌词的基准
        * */
        if(dis==null)
        {
            return;
        }
        for(int i=0;i<dis.size();i++)
        {
            int x,y;
            y=topEdge+dis.get(i);
            //只绘制在屏幕范围内的歌词
            if(y>=0&&(topEdge+dis.get((i==0?0:i-1)))<getHeight())
            {
                int textLocation;
                if(i==0)
                {
                    textLocation=topEdge;
                }
                else
                {
                    textLocation=topEdge+dis.get(i-1);
                }

                /*
                * 在每句歌词开始就要加入行间距
                * */
                textLocation+=textPadding;
                /*
                * 获取一句歌词，并且计算当前歌词长度是否大于歌词绘制的宽度，如果是，就要分行显示
                * */
                String temp=lrc.get(timeStamps.get(i));
                Rect textBounds=new Rect();
                mPaint=new Paint();

                mPaint.setTextSize(textSize);

                mPaint.getTextBounds(temp,0,temp.length(),textBounds);
                if(textBounds.width()>=paintWidth)
                {
                    //计算单个字符的宽度
                    int oneWidth=textBounds.width()/temp.length();
                    //last：未绘制的字符个数
                    int last=temp.length();
                    //paintedCount：已绘制的字符个数
                    int paintedCount=0;
                    while(last*oneWidth>=paintWidth)
                    {
                        //绘制基准线要加一行字符的高度，因为文字绘制是以左下角为基准
                        textLocation+=textBounds.height();
                        //nowPaint：准备要绘制的字符个数。获得该行可以绘制的最大字符个数
                        int nowPaint=paintWidth/oneWidth;
                        //未绘制的=全部-已绘制的-正要绘制的
                        last=temp.length()-nowPaint-paintedCount;

                        mPaint=new Paint();
                        mPaint.setTextSize(textSize);
                        mPaint.setAntiAlias(true);
                        //如果当前歌词是正在播放的，绘制为白色，否则为灰色
                        if(i==nowLocation)
                        {
                            mPaint.setColor(isPlayingTextColor);
                        }
                        else
                        {
                            mPaint.setColor(baseTextColor);
                        }
                        canvas.drawText(temp,paintedCount,paintedCount+nowPaint,(paintWidth-nowPaint*oneWidth)/2+padding,
                                textLocation,mPaint);
                        paintedCount+=nowPaint;
                        textLocation+=changeLinePadding;
                    }
                    /*
                    * 最后跳出循环后，还有last个未绘制，需要绘制一下
                    * */
                    textLocation+=textBounds.height();
                    mPaint=new Paint();
                    mPaint.setTextSize(textSize);
                    mPaint.setAntiAlias(true);
                    if(i==nowLocation)
                    {
                        mPaint.setColor(isPlayingTextColor);
                    }
                    else
                    {
                        mPaint.setColor(baseTextColor);
                    }
                    canvas.drawText(temp,paintedCount,temp.length(),(paintWidth-last*oneWidth)/2+padding,
                            textLocation,mPaint);
                }
                else
                {
                    /*
                    * 如果一行足够容纳，那么直接绘制即可
                    * */
                    textLocation+=textBounds.height();
                    mPaint=new Paint();
                    mPaint.setTextSize(textSize);
                    mPaint.setAntiAlias(true);
                    if(i==nowLocation)
                    {
                        mPaint.setColor(isPlayingTextColor);
                    }
                    else
                    {
                        mPaint.setColor(baseTextColor);
                    }
                    canvas.drawText(temp,0,temp.length(),(paintWidth-textBounds.width())/2+padding,
                            textLocation,mPaint);
                }
            }
        }

    }

    /*
    * 根据传递进来的lrc歌词，初始化dis序列和timeStamps序列，这里并没有包括进歌名和艺术家等信息，只提取歌词内容
    * */
    public void setData(TreeMap<Integer,String> lrc)
    {
        this.lrc=lrc;
        int distance=0;
        textPadding=textSize;

        topEdge=getHeight()/2;

        timeStamps=new ArrayList<>();
        dis=new ArrayList<>();
        Set<Integer> keys=lrc.keySet();
        Iterator<Integer> it=keys.iterator();
        /*
        * 这里距离的计算和绘制过程一样
        * */
        while(it.hasNext())
        {

            int time=it.next();
            if(time>=0)
            {
                //开始就要加上行间距
                distance += textPadding;

                String temp=lrc.get(time);
                Rect textBounds=new Rect();
                mPaint=new Paint();

                mPaint.setTextSize(textSize);

                mPaint.getTextBounds(temp,0,temp.length(),textBounds);
                if(textBounds.width()>=paintWidth)
                {
                    int oneWidth=textBounds.width()/temp.length();
                    int last=temp.length();
                    while(last*oneWidth>=paintWidth)
                    {
                        distance+=textBounds.height()+changeLinePadding;
                        last=temp.length()-(paintWidth/oneWidth);
                    }
                    distance+=textBounds.height();

                }
                else
                {
                    distance+=textBounds.height();
                }


                //结尾也要加上
                distance+=textPadding;
                dis.add(distance);
                timeStamps.add(time);


            }
        }
        postInvalidate();

    }

    public void setTime(int time)
    {
        //nowLocation=0;
        if(timeStamps==null)
        {
            return;
        }
        for(int i=0;i<timeStamps.size();i++)
        {
            if(time<timeStamps.get(i))
            {
                nowLocation= i==0 ? 0:i-1;
                break;
            }
        }
        if(nowLocation!=oldLocation)
        {
            //TODO:如果不同，就需要对歌词进行滚动
            int tempNew=getHeight()/2-dis.get(nowLocation);
            int tempOld=topEdge;
            //topEdge=(getHeight()/2-dis.get(nowLocation));
            oldLocation=nowLocation;
            smoothScroll(tempOld,tempNew);
            //postInvalidate();

        }

    }




    public void init()
    {
        topEdge=getHeight()/2;
        this.setClickable(true);
        this.setOnTouchListener(this);
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                newX=event.getRawX();
                newY=event.getRawY();
                return false;
            case MotionEvent.ACTION_MOVE:
                if(dis.size()>1)
                {
                    LogUtil.v("LrcView","lrc on action move");
                    oldX=newX;
                    oldY=newY;
                    newX=event.getRawX();
                    newY=event.getRawY();
                    topEdge+=newY-oldY;
                    if(dis.size()>1)
                    {
                        if(topEdge<=(getHeight()/2-dis.get(dis.size()-1)))
                        {
                            topEdge=getHeight()/2-dis.get(dis.size()-1);
                        }
                        if(topEdge>=getHeight()/2)
                        {
                            topEdge=getHeight()/2;
                        }
                        postInvalidate();
                    }
                    return true;
                }
                return false;




            case MotionEvent.ACTION_UP:
                /*oldX=newX;
                oldY=newY;
                newX=event.getRawX();
                newY=event.getRawY();
                if(triRect.contains((int)oldX,(int)oldY)&&triRect.contains((int)newX,(int)newY))
                {
                    nowLocation=0;
                    for(int i=0;i<dis.size();i++)
                    {
                        if((getHeight()/2-topEdge)<=dis.get(i))
                        {
                            nowLocation=i;
                            break;
                        }
                    }
                    notifyAll(timeStamps.get(nowLocation));
                    postInvalidate();
                }*/
                return false;
            default:
                return false;
        }
    }

    private void smoothScroll(int oldValue,int newValue)
    {
        ValueAnimator animator=new ValueAnimator().ofInt(oldValue,newValue);
        animator.setDuration(500);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                topEdge = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.start();

    }
}
