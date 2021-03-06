package id.ghodel.colorpickerlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AlertDialog;


public class ColorPickerDialog {
    private static final String TAG = ColorPickerDialog.class.getName();

    private AlertDialog mAlertDialog;
    private final boolean mIsSupportAlpha;
    private final OnColorPickerListener mListener;
    private final ViewGroup mViewContainer;
    private final ColorPlateView mViewPlate;
    private final View mViewHue;
    private final ImageView mViewAlphaBottom;
    private final View mViewAlphaOverlay;
    private final ImageView mPalteCursor;
    private final ImageView mHueCursor;
    private final ImageView mAlphaCursor;
    private final View mViewOldColor;
    private final View mViewNewColor;
    private final float[] mCurrentHSV = new float[3];
    private int mAlpha;


    public ColorPickerDialog(final Context context, int defauleColor, OnColorPickerListener listener){
        this(context,defauleColor,false,listener);
    }

    public ColorPickerDialog(final Context context,int defauleColor,boolean isSupportAlpha ,OnColorPickerListener listener){
        this.mIsSupportAlpha = isSupportAlpha;
        this.mListener = listener;

        if (!isSupportAlpha){
            defauleColor = defauleColor | 0xff000000;
        }

        Color.colorToHSV(defauleColor,mCurrentHSV);
        mAlpha = Color.alpha(defauleColor);

        final View view = LayoutInflater.from(context).inflate(R.layout.color_picker_dialog,null);
        mViewHue = view.findViewById(R.id.img_hue);
        mViewPlate = (ColorPlateView) view.findViewById(R.id.color_plate);
        mHueCursor = (ImageView) view.findViewById(R.id.hue_cursor);
        mViewOldColor = view.findViewById(R.id.view_old_color);
        mViewNewColor = view.findViewById(R.id.view_new_color);
        mPalteCursor = (ImageView) view.findViewById(R.id.plate_cursor);
        mViewContainer = (ViewGroup) view.findViewById(R.id.container);
        mViewAlphaOverlay = view.findViewById(R.id.view_overlay);
        mAlphaCursor = (ImageView) view.findViewById(R.id.alpha_Cursor);
        mViewAlphaBottom = (ImageView) view.findViewById(R.id.img_alpha_bottom);

        {
            mViewAlphaBottom.setVisibility(mIsSupportAlpha ? View.VISIBLE : View.GONE);
            mViewAlphaOverlay.setVisibility(mIsSupportAlpha ? View.VISIBLE : View.GONE);
            mAlphaCursor.setVisibility(mIsSupportAlpha ? View.VISIBLE : View.GONE);
        }

        mViewPlate.setHue(getColorHue());
        mViewOldColor.setBackgroundColor(defauleColor);
        mViewNewColor.setBackgroundColor(defauleColor);

        initOnTouchListener();
        initAlerDialog(context,view);
        initGlobalLayoutListener(view);
    }


    private void initOnTouchListener() {
        //????????????????????????
        mViewHue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP){
                    float y = event.getY();
                    if (y < 0.f) y = 0.f;
                    if (y > mViewHue.getMeasuredHeight()) y = mViewHue.getMeasuredHeight() - 0.001f;
                    float colorHue = 360.f - 360.f / mViewHue.getMeasuredHeight() * y;
                    if (colorHue == 360.f ) colorHue = 0.f;
                    setColorHue(colorHue);
                    mViewPlate.setHue(colorHue);

                    moveHueCursor();
                    mViewNewColor.setBackgroundColor(getColor());
                    if (mListener!=null){
                        mListener.onColorChange(ColorPickerDialog.this,getColor());
                    }
                    updateAlphaView();
                    return true;
                }
                return false;
            }
        });


        if (mIsSupportAlpha) mViewAlphaBottom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP){
                    float y = event.getY();
                    if (y < 0.f) y = 0.f;
                    if (y > mViewHue.getMeasuredHeight()) y = mViewHue.getMeasuredHeight() - 0.001f;
                    final  int alpha = Math.round(255.f - (255.f / mViewAlphaBottom.getMeasuredHeight() * y));
                    ColorPickerDialog.this.setAlpha(alpha);
                    moveAlphaCursor();
                    int color = ColorPickerDialog.this.getColor();
                    int alphaColor = alpha << 24 | color & 0x00ffffff;
                    mViewNewColor.setBackgroundColor(alphaColor);
                    if (mListener!=null){
                        mListener.onColorChange(ColorPickerDialog.this,getColor());
                    }
                    return true;
                }
                return false;
            }
        });

        //???????????????????????????
        mViewPlate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_UP){
                    float x = event.getX();
                    float y = event.getY();
                    if (x < 0.f) x = 0.f;
                    if (x > mViewPlate.getMeasuredWidth()) x = mViewPlate.getMeasuredWidth();
                    if (y < 0.f) y = 0.f;
                    if (y > mViewPlate.getMeasuredHeight()) y = mViewPlate.getMeasuredHeight();

                    setColorSat(1.f / mViewPlate.getMeasuredWidth() * x);//????????????
                    setColorVal(1.f - (1.f / mViewPlate.getMeasuredHeight() * y));//????????????
                    movePlateCursor();
                    mViewNewColor.setBackgroundColor(getColor());
                    if (mListener!=null){
                        mListener.onColorChange(ColorPickerDialog.this,getColor());
                    }
                    return true;
                }
                return false;
            }
        });
    }


    private void initAlerDialog(Context context,View view) {
        mAlertDialog = new AlertDialog.Builder(context).create();
        mAlertDialog.setTitle(context.getResources().getString(R.string.dialog_title));
        mAlertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getResources().getString(android.R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ColorPickerDialog.this.mListener != null){
                    ColorPickerDialog.this.mListener.onColorConfirm(ColorPickerDialog.this,getColor());
                }
            }
        });
        mAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, context.getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ColorPickerDialog.this.mListener != null){
                    ColorPickerDialog.this.mListener.onColorCancel(ColorPickerDialog.this);
                }
            }
        });
        mAlertDialog.setView(view,0,0,0,0);
    }

    private void initGlobalLayoutListener(final View view) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)//api 16
            @Override
            public void onGlobalLayout() {
                moveHueCursor();
                movePlateCursor();
                if (ColorPickerDialog.this.mIsSupportAlpha) {
                    moveAlphaCursor();
                    updateAlphaView();
                }
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
    }



    private void moveHueCursor() {//ConstraintLayout$LayoutParams
        float y = mViewHue.getMeasuredHeight() - (getColorHue() * mViewHue.getMeasuredHeight() / 360.f);
        if (y == mViewHue.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mHueCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (mViewHue.getLeft() - Math.floor(mHueCursor.getMeasuredWidth() / 3) - mViewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (mViewHue.getTop() + y - Math.floor(mHueCursor.getMeasuredHeight() / 2) - mViewContainer.getPaddingTop());
        mHueCursor.setLayoutParams(layoutParams);
    }

    private void moveAlphaCursor(){
        final float y = mViewAlphaBottom.getMeasuredHeight() - (this.getAlpha() * mViewAlphaBottom.getMeasuredHeight() / 255.f);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mAlphaCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (mViewAlphaBottom.getLeft() - Math.floor(mAlphaCursor.getMeasuredWidth() /3) -  mViewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (mViewAlphaBottom.getTop() + y - Math.floor(mAlphaCursor.getMeasuredHeight() / 2) - mViewContainer.getPaddingTop());
        mAlphaCursor.setLayoutParams(layoutParams);
    }

    private void movePlateCursor(){
        final float x = getColorSat() * mViewPlate.getMeasuredWidth();
        final float y = (1.f - getColorVal()) * mViewPlate.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPalteCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (mViewPlate.getLeft() + x - Math.floor(mPalteCursor.getMeasuredWidth() / 2) - mViewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (mViewPlate.getTop() + y - Math.floor(mPalteCursor.getMeasuredHeight() / 2) - mViewContainer.getPaddingTop());
        mPalteCursor.setLayoutParams(layoutParams);

    }

    private void setColorHue(float color){
        mCurrentHSV[0] = color;
    }

    private float getColorHue(){
        return mCurrentHSV[0];
    }

    private void setColorSat(float color) {
        this.mCurrentHSV[1] = color;
    }

    private float getColorSat(){
        return this.mCurrentHSV[1];
    }

    private void setColorVal(float color){
        this.mCurrentHSV[2] = color;
    }

    private float getColorVal(){
        return mCurrentHSV[2];
    }


    private int getColor(){
        final  int argb = Color.HSVToColor(mCurrentHSV);
        return mAlpha << 24 | (argb & 0x00ffffff);
    }


    private void updateAlphaView(){
        final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.HSVToColor(mCurrentHSV),0x0});
        mViewAlphaOverlay.setBackgroundDrawable(gd);
    }

    public void setButtonTextColor(int color){
        if (mAlertDialog != null) {
            Button btnPositive = mAlertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            Button btnNegative = mAlertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            btnPositive.setTextColor(color);
            btnNegative.setTextColor(color);
        }
    }

    private void setAlpha(int alpha){
        this.mAlpha = alpha;
    }

    private int getAlpha(){
        return mAlpha;
    }

    public ColorPickerDialog show(){
        mAlertDialog.show();
        return ColorPickerDialog.this;
    }

    public AlertDialog getDialog(){
        return mAlertDialog;
    }

}
