package com.sentinella.james.wvs_android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.sentinella.james.ClientCallback;
import com.sentinella.james.ClientSocket;

import java.lang.reflect.Field;

/**
 * Created by grecaun on 8/26/17.
 */

public class PlayView extends View implements View.OnTouchListener {
    private static final String TAG = "PlayView";
    int mDeviceWidth;
    int mDeviceHeight;
    int mCardHeight;
    int mCardWidth;
    int mCardOffset;
    int mCardWidthOffsetLeft;
    int mCardWidthOffsetRight;
    int mAvatarWidth;
    int mAvatarHeight;
    int mIconWidth;
    int mIconHeight;
    int mNameTextSize = 35;
    int mOtherTextSize = 20;
    int mButtonSize;
    int mButtonTextSize;
    int mButtonXOffset;
    int mButtonYOffset;
    int mTableCardsXOffset;
    int mTableCardsYOffset;
    int mTableCardsPadding;
    boolean mIsVertical;
    Paint mNameTextPaint;
    Paint mOtherTextPaint;

    static final Bitmap cards[]    = new Bitmap[52];
    static       Rect   cardRects[] = new Rect[20];

    public PlayView(Context context) {
        super(context);
        Log.d(TAG, "Constructor");
        init(context);
    }

    public PlayView(Context context, AttributeSet attributeSet) {
        super (context, attributeSet);
        Log.d(TAG, "Constructor");
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init");
        mNameTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNameTextPaint.setColor(Color.BLACK);
        mNameTextPaint.setTextSize(mNameTextSize);
        mOtherTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOtherTextPaint.setColor(Color.BLACK);
        mOtherTextPaint.setTextSize(mOtherTextSize);
        this.setOnTouchListener(this);
    }

    public static void preInit(Resources res, Context context) {
        String number;
        for (int i = 0; i < 52; i += 4) {
            switch (i / 4) {
                case 0:
                    number = "03";
                    break;
                case 1:
                    number = "04";
                    break;
                case 2:
                    number = "05";
                    break;
                case 3:
                    number = "06";
                    break;
                case 4:
                    number = "07";
                    break;
                case 5:
                    number = "08";
                    break;
                case 6:
                    number = "09";
                    break;
                case 7:
                    number = "10";
                    break;
                case 8:
                    number = "ja";
                    break;
                case 9:
                    number = "qu";
                    break;
                case 10:
                    number = "ki";
                    break;
                case 11:
                    number = "ac";
                    break;
                case 12:
                    number = "02";
                    break;
                default:
                    number = "";
            }
            String c1 = "clubs_" + number;
            String c2 = "diamonds_" + number;
            String c3 = "hearts_" + number;
            String c4 = "spades_" + number;
            Log.d(TAG, String.format("Trying to get bitmap for cards '%s' '%s' '%s' '%s'", c1, c2, c3, c4));
            cards[i] = BitmapFactory.decodeResource(res, res.getIdentifier(c1, "drawable", context.getPackageName()));//clubs
            cards[i + 1] = BitmapFactory.decodeResource(res, res.getIdentifier(c2, "drawable", context.getPackageName()));//diamonds
            cards[i + 2] = BitmapFactory.decodeResource(res, res.getIdentifier(c3, "drawable", context.getPackageName()));//hearts
            cards[i + 3] = BitmapFactory.decodeResource(res, res.getIdentifier(c4, "drawable", context.getPackageName()));//spades
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldW, int oldH) {
        Log.d(TAG, "onSizeChanged");
        mDeviceWidth = w;
        mDeviceHeight = h;
        mIsVertical = mDeviceWidth < mDeviceHeight;
        mNameTextSize = mIsVertical ? mDeviceHeight / 18 : mDeviceWidth / 18;
        mNameTextPaint.setTextSize(mNameTextSize);
        mOtherTextSize = mNameTextSize / 2;
        mOtherTextPaint.setTextSize(mOtherTextSize);
        Log.d(TAG, String.format("Width: %d - Height: %d - %s", mDeviceWidth, mDeviceHeight, mIsVertical ? "Vertical" : "Horizontal"));
        mCardWidth = mIsVertical ? mDeviceWidth / 4 : mDeviceHeight / 4;
        mCardHeight = mCardWidth * 144 / 100;
        mCardOffset = mIsVertical ? mDeviceHeight / 10 : mDeviceWidth / 20;
        if (mIsVertical) {
            for (int i=0;i<10;i++) {
                cardRects[i] = new Rect(0,i*mCardOffset,mCardWidth,i*mCardOffset+mCardHeight);
            }
        } else {
            for (int i=0;i<20;i++) {
                cardRects[i] = new Rect(i*mCardOffset,0,i*mCardOffset+mCardWidth,mCardHeight);
            }
        }
        // calculate sizes
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw");
        if (mIsVertical) {
            for (int i=0; i<10; i++) {
                canvas.drawBitmap(cards[0],null,cardRects[i],null);
            }
        } else {
            for (int i=0; i<20; i++) {
                canvas.drawBitmap(cards[0],null,cardRects[i],null);
            }
        }
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        Log.d(TAG, "onLayout");
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float xCoord,yCoord;
        xCoord = motionEvent.getX();
        yCoord = motionEvent.getY();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.d(TAG, String.format("SOMEONE DONE TOUCHED ME! They done it at %f, %f",xCoord,yCoord));
                break;
            default:
                Log.d(TAG, "Something happened but I'm ignoring it.");
        }
        return true;
    }
}
