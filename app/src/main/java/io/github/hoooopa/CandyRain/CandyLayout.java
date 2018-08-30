package io.github.hoooopa.CandyRain;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;



public class CandyLayout extends RelativeLayout {

    private Interpolator line = new LinearInterpolator();//线性
    private Interpolator acc = new AccelerateDecelerateInterpolator();//加速
    private Interpolator dec = new DecelerateInterpolator();//减速
    private Interpolator accdec = new AccelerateDecelerateInterpolator();//先加速后减速
    private Interpolator[] interpolators;

    Drawable candy1;
    Drawable candy2;
    Drawable candy3;
    Drawable candy4;
    Drawable candy5;

    Drawable[] drawables ;

    private int dHeight;
    private int dWidth;
    private int mWidth;
    private int mHeight;
    private LayoutParams params;
    private PointF pointF;

    private Random random = new Random();

    public CandyLayout(Context context) {
        super(context);
    }

    public CandyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        interpolators = new Interpolator[]{line,acc,dec,accdec};
        drawables = new Drawable[5];
        candy1 = getResources().getDrawable(R.drawable.candy);
        candy2 = getResources().getDrawable(R.drawable.candy2);
        candy3 = getResources().getDrawable(R.drawable.candy3);
        candy4 = getResources().getDrawable(R.drawable.candy4);
        candy5 = getResources().getDrawable(R.drawable.candy5);
        drawables[0] = candy1;
        drawables[1] = candy2;
        drawables[2] = candy3;
        drawables[3] = candy4;
        drawables[4] = candy5;

//        //得到图片的宽高
        dHeight = candy1.getIntrinsicHeight();
        dWidth = candy1.getIntrinsicWidth();
        //初始化Params
        params = new LayoutParams(dHeight,dWidth);
        params.addRule(CENTER_HORIZONTAL,TRUE);//父容器的水平居中
        params.addRule(ALIGN_PARENT_BOTTOM,TRUE);//父容器的底部


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //测绘 -- 得到本layout的宽高
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    public void addLove(){
        final ImageView imageView = new ImageView(getContext());
        imageView.setImageDrawable(drawables[random.nextInt(5)]);
        imageView.setLayoutParams(params);
        addView(imageView);

        //属性动画控制坐标
        AnimatorSet set = getAnimator(imageView);
        //设置一个监听
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //结束后，将imageview移除
                removeView(imageView);
            }
        });
        set.start(); //开启动画
    }

    private AnimatorSet getAnimator(ImageView imageView) {
        //1.alpha动画
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(imageView,"alpha",0.3f,1f);
        //2.缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView,"scaleX",0.2f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView,"scaleY",0.2f,1f);
        AnimatorSet enter = new AnimatorSet();
        enter.setDuration(500);
        //三个动画同时执行
        enter.playTogether(alpha,scaleX,scaleY);
        enter.setTarget(imageView);

        //3.贝塞尔曲线动画（核心，不断的修改当前ImageView的坐标  PointF(x,y)）
        ValueAnimator bezierValueAnimator = getBezierValueAnimator(imageView);
        AnimatorSet bezierSet = new AnimatorSet();
        //先enter，后beziervalueAnimator。顺序执行
        bezierSet.playSequentially(enter,bezierValueAnimator);
        //加速因子，使用插值器
        bezierSet.setInterpolator(interpolators[random.nextInt(4)]);
//        bezierSet.setDuration(3000);
        bezierSet.setTarget(imageView);
        return bezierSet;
    }

    private ValueAnimator getBezierValueAnimator(final ImageView imageView) {
        //构造一个贝塞尔曲线动画
        PointF pointF2 = getPointF(2);
        PointF pointF1 = getPointF(1);
        PointF pointF0 = new PointF(mWidth/2 - dWidth/2,mHeight - dHeight);
        PointF pointF3 = new PointF(random.nextInt(mWidth),0);
        //估值器
        BezierEvaluetor evaluetor = new BezierEvaluetor(pointF1,pointF2);
        //属性动画:不仅仅可以改变view的属性，还可以改变自定义的属性
        ValueAnimator animator = ValueAnimator.ofObject(evaluetor,pointF0,pointF3);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF pointF = (PointF) animation.getAnimatedValue();
                imageView.setX(pointF.x);
                imageView.setY(pointF.y);
                imageView.setAlpha(1-animation.getAnimatedFraction());//getAnimatedFraction()得到百分比

            }
        });
        animator.setTarget(imageView);
        animator.setDuration(3000);

        return animator;
    }

    public PointF getPointF(int i) {

        PointF pointF = new PointF();
        pointF.x = random.nextInt(mWidth); //  0 ~ Layout宽度
        //为了好看，尽量保证Point2.y>point1.y
        if (i == 2){
            pointF.y = random.nextInt(mHeight/2) ;
        }else {
            pointF.y = random.nextInt(mHeight/2) + mHeight/2; // 0 ~ Layout的高度
        }
        return pointF;
    }
}
