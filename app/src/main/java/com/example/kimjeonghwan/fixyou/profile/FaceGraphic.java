/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.kimjeonghwan.fixyou.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.profile.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

import java.util.Objects;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;
    private Bitmap bitmap;
    private Bitmap op;

    private int maskPosition;       // 사용자가 선택한 마스크의 위치
    private int[] maskDrawable;     // 마스크 드로워블의 id 값을 저장하고 있는 int 배열
    private Context mContext;

    FaceGraphic(GraphicOverlay overlay, int maskPosition, Context mContext) {
        super(overlay);

        this.maskPosition = maskPosition;
        this.mContext = mContext;
        // 마스크 Drawable 의 ID 값을 배열로 저장한 변수
        maskDrawable = new int[]{R.drawable.captin, R.drawable.starwars, R.drawable.op, R.drawable.iron, R.drawable.cat, R.drawable.dog, R.drawable.crown};
        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // 현재 얼굴의 중심점 (x,y)를 찾는다.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);
        // 좌우상하 위치를 찾는다.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        //canvas.drawRect(left, top, right, bottom, mBoxPaint);
        //canvas.drawBitmap(op, left, top, new Paint());

        if(maskPosition != 0) {  // 사용자가 선택한 마스크가 "None" 이 아니라면 마스크를 그린다.
            Drawable drawable = ContextCompat.getDrawable(mContext, maskDrawable[maskPosition - 1]);  // 선택된 마스크 드로워블을 가져온다.

            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            op = bitmap;

            // 좌우상하 위치값을 int 형으로 변환한다.
            int l = (int) left;
            int t = (int) top;
            int r = (int) right;
            int b = (int) bottom;

            // 마스크의 위치를 조정할 필요가 있는 경우 조정한다.
            switch (maskPosition) {
                case 1:     // 캡틴아메리카
                    Objects.requireNonNull(drawable).setBounds(l,t,r,b);
                    drawable.draw(canvas);
                    break;
                case 2:     // 스타워즈
                    Objects.requireNonNull(drawable).setBounds(l,t,r,b);
                    drawable.draw(canvas);
                    break;
                case 3:     // 옵티머스
                    op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                            (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
                    canvas.drawBitmap(op, left, top, new Paint());
                    break;
                case 4:     // 아이언맨
                    op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                            (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
                    canvas.drawBitmap(op, left, top, new Paint());
                    break;
                case 5:     // 고양이
                    op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                            (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
                    canvas.drawBitmap(op, left, top, new Paint());
                    break;
                case 6:     // 강아지
                    op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                            (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
                    canvas.drawBitmap(op, left, top, new Paint());
                    break;
                case 7:     // 꽃왕관
                    op = Bitmap.createScaledBitmap(bitmap, (int) scaleX(face.getWidth()),
                            (int) scaleY(((bitmap.getHeight() * face.getWidth()) / bitmap.getWidth())), false);
                    canvas.drawBitmap(op, left, top, new Paint());
                    break;
            }
        }
    }

    private float getNoseAndMouthDistance(PointF nose, PointF mouth) {
        return (float) Math.hypot(mouth.x - nose.x, mouth.y - nose.y);
    }
}
