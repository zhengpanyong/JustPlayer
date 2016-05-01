package com.example.zu.myapp.layout;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;


import com.example.zu.myapp.util.LogUtil;


/**
 * Created by zu on 2016/2/12.
 */

/*
* 慢速滑动的时候还有问题，明天需要解决
* 还有菜单显示的时候，在右侧布局的点击事件还没有完成
* 还有右侧布局也应该滑动
* */
public class SlideLayout extends FrameLayout
{
    /*
    * 被判定为滑动的滑动速度
    * */
    public int SNAP_VELOCITY=500;

    /*
    * 手指落在屏幕上时的X和Y坐标
    * */
    private float xDown;
    private float yDown;

    /*
    * 手指抬起时的坐标
    * */
    private float xUp;
    private float yUp;

    /*
    * 手指上次滑动时的坐标
    * */
    private float xLastMove;
    private float yLastMove;
    /*
    * 手指本次滑动时的坐标
    * */
    private float xMove;
    private float yMove;

    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /*
    * 是否正在滑动，主要是判断本次手势是否是想要滑动，如果这次手势是想要滑动，就为true，如果不是则为false。
    * */
    private boolean isSliding=false;




    /*
    * 左侧和右侧View
    * */
    private View leftView;
    private View shadow;
    private View rightView;
    /*
    * 屏幕宽度
    * */
    private int screenWidth;

    /*
    * 两个布局的参数
    * */
    private MarginLayoutParams leftViewParams;

    private MarginLayoutParams rightViewParams;

    /*
    * 左侧是否可见
    * */
    private boolean isLeftVisiable=false;


    /*
    * 滑动方向
    * */
    private enum DIRECTION
    {
        RIGHT,LEFT
    }


    /*
    *VelocityTracker对象
    * */
    private VelocityTracker mVelocityTracker;

    /*
    * 每次检测滑动时手指移动的距离
    * */
    private float dx;
    private float dy;

    /*
    * 一次完整的动作从终点到起点的距离
    * */
    private float xDistance;
    private float yDistance;

    /*
    * 阴影和左侧布局宽度的比例
    * */
    private float shadowRatio;

    private int shadowAlpha=200;

    /*
    * 右侧布局与左侧布局的宽度比值
    * */
    private float rightToLeftRatio;


    public int getShadowAlpha() {
        return shadowAlpha;
    }

    public void setShadowAlpha(int shadowAlpha) {

        this.shadowAlpha = shadowAlpha;
    }

    public float getRightToLeftRatio() {
        return rightToLeftRatio;
    }

    public void setRightToLeftRatio(float rightToLeftRatio) {
        this.rightToLeftRatio = rightToLeftRatio;
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        screenWidth = wm.getDefaultDisplay().getWidth();
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();




    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(changed)
        {
            leftView=getChildAt(2);
            leftViewParams=(MarginLayoutParams)leftView.getLayoutParams();
            leftViewParams.leftMargin= -leftViewParams.width;
            leftView.setLayoutParams(leftViewParams);

            shadow=getChildAt(1);

            shadow.getBackground().setAlpha(0);


            rightView=getChildAt(0);
            rightViewParams=(MarginLayoutParams)rightView.getLayoutParams();
            rightViewParams.width=screenWidth;
            rightViewParams.leftMargin=0;
            rightView.setLayoutParams(rightViewParams);

//            rightToLeftRatio=(float)rightViewParams.width/leftViewParams.width;
            rightToLeftRatio=(float)0.5;
            shadowRatio=(float)shadowAlpha/leftViewParams.width;
            setClickable(true);
        }


    }


/*
* 处理滑动菜单上的触摸事件。
* 1、如果左侧布局显示了并且action down是在左侧布局之外，就认为这个操作是想让左侧布局隐藏，则拦截掉此次事件；
* 2、如果action move操作中move的x位移大于y位移，那么认为这是想要滑出或隐藏左侧菜单，拦截掉此次事件；
* 其他时候，都将事件分发给子view去执行
* */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {


        /*
        * 如果左侧布局可见的，那就要将左侧布局设置成clickable的，这样左侧布局显示时才会拦截掉在左侧布局的空白处的操作，否则该操作
        * 会分发到该位置下的右侧布局中的控件
        * */

        if(isLeftVisiable)
        {
            leftView.setClickable(true);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDown = event.getRawX();
                yDown = event.getRawY();
                xLastMove = xDown;
                yLastMove = yDown;
                isSliding = false;

                if(xDown>leftViewParams.width && isLeftVisiable==true)
                {
                    return true;
                }


//                LogUtil.v("SlideLayout","onInterceptTouchEvent:MotionEvent.ACTION_DOWN");

                break;

            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                yMove = event.getRawY();

                dx = xMove - xDown;
                dy = yMove - yDown;

//                LogUtil.v("SlideLayout", dx + "  " + dy);

                if(Math.abs(dx)/2>Math.abs(dy))
                {
                    return true;
                }

//                LogUtil.v("SlideLayout", "onInterceptTouchEvent:MotionEvent.ACTION_MOVE");
                break;

            case MotionEvent.ACTION_UP:
//                LogUtil.v("SlideLayout", "onInterceptTouchEvent:MotionEvent.ACTION_UP");
                break;
            default:
                break;

        }
        return super.onInterceptTouchEvent(event);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {

        createVelocityTracker(event);
        switch (event.getAction())
        {
            case MotionEvent.ACTION_MOVE:
                xMove = event.getRawX();
                yMove = event.getRawY();

                dx = xMove - xLastMove;
                dy = yMove - yLastMove;


                xLastMove = xMove;
                yLastMove = yMove;
                scroll();
//                LogUtil.v("SlideLayout", dx + "  " + dy);
//                LogUtil.v("SlideLayout", "onTouchEvent:MotionEvent.ACTION_MOVE");
                return true;


            case MotionEvent.ACTION_UP:
                xUp=event.getRawX();
                yUp=event.getRawY();
                xDistance=xUp-xDown;
                yDistance=yUp-yDown;
//                LogUtil.v("Params","xDistance"+xDistance);
//                LogUtil.v("Params","yDistance"+yDistance);
                if(xDistance<touchSlop && isLeftVisiable==true && xDown>leftViewParams.width)
                {
                    scrollToLeft();
                    return true;
                }
                scrollTo(getScrollDirection());

//                LogUtil.v("SlideLayout","onTouchEvent:"+mVelocityTracker.toString());
                recycleVelocityTracker();
//                LogUtil.v("SlideLayout", "onTouchEvent:MotionEvent.ACTION_UP");
                break;

        }
        return super.onTouchEvent(event);
    }


    /*
    * 用来控制跟随用户手的滑动
    * */
    private void scroll()
    {
        /*
        * 在第一次进入这个模块的时候，isSliding=false，但只要用户是有左右滑动的意愿，就可以进入滑动，
        * 在随后的move进入这个模块时，即使用户向上下滑动得比较多，但这次的动作已经触发滑动了，并且isSliding=true，
        * 所以只要判断这个动作开始是想要滑动，那么后面不管他怎么滑，都会进入这个模块。
        * 如果滑动已经达到了边界，也不会使isSliding为false，这样是为了屏蔽事件，不去触发其他view的事件。
        * 只有在Action_UP事件发生后才会使isSliding=false。
        * */
        isSliding=true;
        leftViewParams=(MarginLayoutParams)leftView.getLayoutParams();

        rightViewParams=(MarginLayoutParams)rightView.getLayoutParams();

        if(dx>0)
        {
            if(leftViewParams.leftMargin>=0)
            {
                leftViewParams.leftMargin=0;
                isLeftVisiable=true;
                return;
            }
            else
            {
                if(Math.abs(leftViewParams.leftMargin)<dx)
                {
                    leftViewParams.leftMargin=0;
                    isLeftVisiable=true;

                }
                else
                {
                    leftViewParams.leftMargin+=dx;
                }

                leftView.setLayoutParams(leftViewParams);

            }
        }
        if(dx<0)
        {
            if (leftViewParams.leftMargin<=(-leftViewParams.width))
            {

                isLeftVisiable=false;
                return;
            }
            else
            {
                if(leftViewParams.width-Math.abs(leftViewParams.leftMargin)<Math.abs(dx))
                {
                    leftViewParams.leftMargin=-leftViewParams.width;
                    isLeftVisiable=false;
                }
                else
                {
                    leftViewParams.leftMargin+=dx;
                }


                leftView.setLayoutParams(leftViewParams);



            }
        }

        shadow.getBackground().setAlpha((int) (shadowAlpha - Math.abs(leftViewParams.leftMargin) * shadowRatio));
        rightViewParams.leftMargin=(int)((leftViewParams.leftMargin+leftViewParams.width)*rightToLeftRatio);
        rightView.setLayoutParams(rightViewParams);

    }

    public void scrollToLeft()
    {

        leftViewParams=(MarginLayoutParams)leftView.getLayoutParams();

        ValueAnimator animator=ValueAnimator.ofInt(leftViewParams.leftMargin, -leftViewParams.width);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftViewParams.leftMargin = (int) animation.getAnimatedValue();
                leftView.setLayoutParams(leftViewParams);
                shadow.getBackground().setAlpha((int) (shadowAlpha - Math.abs(leftViewParams.leftMargin) * shadowRatio));
                rightViewParams.leftMargin=(int)((leftViewParams.leftMargin+leftViewParams.width)*rightToLeftRatio);
                rightView.setLayoutParams(rightViewParams);
            }
        });

        animator.start();
        isLeftVisiable=false;


    }

    public void scrollToRight()
    {


        leftViewParams=(MarginLayoutParams)leftView.getLayoutParams();

        ValueAnimator animator=ValueAnimator.ofInt(leftViewParams.leftMargin, 0);
        animator.setDuration(200);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftViewParams.leftMargin = (int) animation.getAnimatedValue();
                leftView.setLayoutParams(leftViewParams);
                shadow.getBackground().setAlpha((int) (shadowAlpha - Math.abs(leftViewParams.leftMargin) * shadowRatio));
                rightViewParams.leftMargin=(int)((leftViewParams.leftMargin+leftViewParams.width)*rightToLeftRatio);
                rightView.setLayoutParams(rightViewParams);
            }
        });

        animator.start();
        isLeftVisiable=true;

    }




    /*
    * 手指松开后得到应该滚动的方向
    * */
    private DIRECTION getScrollDirection()
    {
        leftViewParams=(MarginLayoutParams)leftView.getLayoutParams();
        float speed=getScrollVelocity();

        if(Math.abs(speed)>SNAP_VELOCITY)
        {
            if(speed<0)
            {
                return DIRECTION.LEFT;
            }
            else
            {
                return DIRECTION.RIGHT;
            }
        }
        else if(Math.abs(leftViewParams.leftMargin)>=leftViewParams.width/2)
        {
            return DIRECTION.LEFT;
        }

        return DIRECTION.RIGHT;
    }


    private void scrollTo(DIRECTION direction)
    {
        if(direction== DIRECTION.LEFT)
        {
            scrollToLeft();
        }
        else
        {
            scrollToRight();
        }
    }

    /*
    * 建立VelocityTracker
    * */
    private void createVelocityTracker(MotionEvent event)
    {
        if(mVelocityTracker==null)
        {
            mVelocityTracker=VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

    }

    /*
    * 回收VelocityTracker
    * */
    private void recycleVelocityTracker()
    {
        if(mVelocityTracker!=null)
        {
            mVelocityTracker.recycle();
        }
        mVelocityTracker=null;
    }

    /*
    * 获得手指X方向滑动的速度，为绝对值
    * */
    private float getScrollVelocity()
    {
        mVelocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) mVelocityTracker.getXVelocity();
//        LogUtil.v("SlideLayout", "getScrollVelocity:" + velocity);
        return velocity;
    }




}


