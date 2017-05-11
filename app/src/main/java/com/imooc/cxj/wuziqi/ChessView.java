package com.imooc.cxj.wuziqi;

/**
 * Created by cxj on 2017/4/22.
 */
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.util.AttributeSet;
import java.util.ArrayList;

import com.imooc.cxj.wuziqi.R;
import com.imooc.cxj.wuziqi.IsChessWin;

public class ChessView extends View {
    private int mPanelWith; //棋盘的宽度（棋盘使方形的）
    private float mLineHeight; //棋盘每一个空格的高度

    private int MAX_LINE = 10;//棋盘的行数

    private Paint mPaint = new Paint(); //创建画笔

    private Bitmap wPieces; //白棋  （慕课网：mWhitePiece)  Bitmap是位图文件
    private Bitmap bPieces; //黑棋   (mBladckPiece)

    private ArrayList<Point> wPoints = new ArrayList<>();//白棋坐标的集合，ArrayList为动态数组
    private ArrayList<Point> bPoints = new ArrayList<>();//黑棋坐标的集合

    private float radioPoeces = 1.0f * 3 / 4 ;  //棋子与棋格的大小比例   (慕课网 radioPieceOfLineHeight)

    private boolean mIsWitch = true;//判断是否白子画在棋盘上，当前轮到白棋
    private boolean isGameOver = false; //判断是否游戏结束

    private Context mContext;
    private  IsChessWin isChessWin;  //一个用来处理胜利与否的逻辑
    private String TAG = "CHESSVIEW";
    private DragEvent event;

    public ChessView(Context context) {
        super(context);
        mContext = context;
        init();
    }
    public  ChessView(Context context,AttributeSet attrs)
    {
        super(context,attrs);
        mContext = context;
        setBackgroundColor(0xFF0000);
        init();
    }
    public ChessView(Context context,AttributeSet attrs,int defStyleAttr)
    {
        super(context,attrs,defStyleAttr);
        mContext = context;
        init();
    }

    /**
     * 再来一局方法
     */
    public void myreStart() {
        wPoints.clear();
        bPoints.clear();
        isGameOver = false;
        Log.i(TAG, "myreStart: " + wPoints.size() + ":::" + bPoints.size());
        invalidate();
    }

    /**
     * 初始化
     */
    public void init(){
        //设置画笔的颜色
        mPaint.setColor(Color.BLACK);
        //设置抗锯齿功能（图像边缘相对清晰一点，锯齿痕迹不那么明显）
        mPaint.setAntiAlias(true);
        //设置防抖动功能（使图像更加柔和一点）
        mPaint.setDither(true);
        //设置画笔的风格为空心
        mPaint.setStyle(Paint.Style.STROKE);
        //获取棋子的资源文件
        wPieces = BitmapFactory.decodeResource(getResources(),R.drawable.stone_w1);
        bPieces = BitmapFactory.decodeResource(getResources(),R.drawable.stone_b1);
    }
    //自定义view的测量问题
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        /**
         * 获取view的宽度和mode
         * mode分为：
         * EXACTLY：EXACTLY是精确尺寸，当我们将控件的layout_width或layout_height指定为具体数值时如andorid:layout_width="50dip"，或者为FILL_PARENT是，都是控件大小已经确定的情况，都是精确尺寸。
         * AT_MOST：最大尺寸，当控件的layout_width或layout_height指定为WRAP_CONTENT时，控件大小一般随着控件的子空间或内容进行变化，此时控件尺寸只要不超过父控件允许的最大尺寸即可。因此，此时的mode是AT_MOST，size给出了父控件允许的最大尺寸。
         * UNSPECIFIED：未指定尺寸，这种情况不多，一般都是父控件是AdapterView，通过measure方法传入的模式
         */

        //宽度的Size,辅助类
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //由于棋盘是正方形，所以要从长和宽取最短的
        int width = Math.min(widthSize,heightSize);

        //如果上方有一个使UNSPECTFIED,相对应的有一个尺寸是0，如果有一个是0，纳米width就是0显示不出来
        if(widthMode == MeasureSpec.UNSPECIFIED){
            width = heightSize;
        }
        else if (heightMode == MeasureSpec.UNSPECIFIED){
            width = widthSize;
        }
        //设置实际的长和宽上去
        setMeasuredDimension(width,width);
    }

    /**
     * 当view的尺寸改变时，会回掉这个方法,跟尺寸挂钩的
     * @param w
     * @param h
     * @param oldw
     * @param  oldh
     */
    @Override
    protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);

        mPanelWith = w;

        mLineHeight = mPanelWith *1.0f/MAX_LINE;

        int piecesWidth = (int) (mLineHeight * radioPoeces);  // 棋子目标宽度
        //按照以前存在的位图按照一定的比例构建一个新的位图，修改棋子的尺寸
        wPieces = Bitmap.createScaledBitmap(wPieces,piecesWidth,piecesWidth,true);    //慕课设置为false

        bPieces = Bitmap.createScaledBitmap(bPieces,piecesWidth,piecesWidth,true);
    }

    /**
     * 获取坐标的集合
     *
     * @param event
     * @return
     */
    @Override
    //用户点击的时候坐标在哪
    public boolean onTouchEvent(MotionEvent event){
        isGameOver = isChessWin.isGameOverMethod(wPoints, bPoints);
        if(isGameOver){
            showDialog();
            return false;
        }
        int action = event.getAction();
        if(action == MotionEvent.ACTION_UP){
            int x = (int) event.getX();
            int y = (int) event.getY();
            Point point = getSimulatePoint(x,y);
            //如果黑棋的集合或者白棋的集合包含这个坐标，那么返回false
            //contains和eequals比较的不是内存空间的地址，而是x，y值是否一致
            if (wPoints.contains(point) || bPoints.contains(point)){
                return false;
            }
            if(mIsWitch){
                wPoints.add(point);
            }else{
                bPoints.add(point);
            }

            //刷新View
            invalidate();
            mIsWitch = !mIsWitch;
            return true;
        }
        return true;
    }
    /**
     * 根据真实的坐标模拟出绝对值坐标
     *
     * @param  x
     * @param  y
     * @return
     */
    public Point getSimulatePoint(int x,int y){
        return new Point ((int)(x / mLineHeight),(int) (y / mLineHeight));
    }
    /**
     * 显示白棋或者黑棋获胜的提示的对话框
     */
    public void showDialog(){
        String successText = isChessWin.isWhiteWinFlag() ? "白棋获胜！" : "黑棋获胜！";
        new AlertDialog.Builder(mContext)
                .setMessage("恭喜"+ successText+ "，是否再来一局？")
                .setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        myreStart();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawBoard(canvas);
        drawPieces(canvas);
        //下面的实例用来判断是否胜利
        isChessWin = new IsChessWin(mContext);
        isGameOver = isChessWin.isGameOverMethod(wPoints,bPoints);
        //判断是否结束游戏
        if(isGameOver){
            showDialog();
        }
    }

    /**
     * 画棋盘的线
     *
     * @param canvas
     */

    public void drawBoard(Canvas canvas){
        int w = mPanelWith;  //成员变量，棋盘宽度
        float lineHeight = mLineHeight;
        //画十条线
        for(int i = 0;i <MAX_LINE;i++){
            //设置起点的横坐标为半个棋盘空格的宽度
            int startX = (int) (lineHeight / 2);
            //设置终点x横坐标为宽度减去半个lineHeight（棋盘空格宽度）
            int endX = (int) (w - lineHeight /2);
            int y = (int) ((0.5 + i)*lineHeight);
            //画横线
            canvas.drawLine(startX,y,endX,y,mPaint);
            //画纵线，坐标反过来
            canvas.drawLine(y,startX,y,endX,mPaint);
        }
    }

    /**
     * 画棋子
     */

    public void drawPieces(Canvas canvas){
        Log.i(TAG,"drawPieces:"+wPoints.size()+":::"+bPoints.size());
        for (int i = 0; i < wPoints.size(); i++) {
            Point point = wPoints.get(i);
            //drawBitmap是将图片的右下角为坐标
            canvas.drawBitmap(wPieces, ((point.x + (1 - radioPoeces) / 2) * mLineHeight), (point.y + (1 - radioPoeces) / 2) * mLineHeight, null);
        }
        for (int i = 0; i < bPoints.size(); i++) {
            Point point = bPoints.get(i);
            canvas.drawBitmap(bPieces, ((point.x + (1 - radioPoeces) / 2) * mLineHeight), (point.y + (1 - radioPoeces) / 2) * mLineHeight, null);
        }
    }

    private static final String INSTANCE = "instance";

    private static final String INSTANCE_GAMEOVER = "instance_gameover";

    private static final String INSTANCE_WHITEARRAY = "instance_whitearray";

    private static final String INSTANCE_BLACKARRAY = "instance_blackarray";

    /**
     * 当view因为某种原因（比如系统回收）销毁时，保存状态
     *
     * @return
     */

    @Override
    protected Parcelable onSaveInstanceState(){
        Bundle bundle = new Bundle();

        //保存系统默认状态
        bundle.putParcelable(INSTANCE,super.onSaveInstanceState());

        //保存是否游戏结束的值
        bundle.putBoolean(INSTANCE_GAMEOVER,isGameOver);

        //保存白棋的子数
        bundle.putParcelableArrayList(INSTANCE_WHITEARRAY,wPoints);

        //保留黑棋的子数
        bundle.putParcelableArrayList(INSTANCE_BLACKARRAY,bPoints);
        return bundle;
    }

    /**
     * 取出保存的值
     *
     * @parme state
     */

    @Override
    protected void onRestoreInstanceState(Parcelable state){
        if (state instanceof Bundle){
            Bundle bundle = (Bundle) state;
            isGameOver = bundle.getBoolean(INSTANCE_GAMEOVER);
            wPoints = bundle.getParcelableArrayList(INSTANCE_WHITEARRAY);
            bPoints = bundle.getParcelableArrayList(INSTANCE_BLACKARRAY);
            super.onRestoreInstanceState(bundle.getParcelable(INSTANCE));
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
