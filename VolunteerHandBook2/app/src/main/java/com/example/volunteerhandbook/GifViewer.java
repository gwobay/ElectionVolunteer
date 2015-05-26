package com.example.volunteerhandbook;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.view.View;

public class GifViewer extends View {

        private Movie mMovie;
        private long mMovieStart;

        //Set to false to use decodeByteArray
        private static final boolean DECODE_STREAM = true;

        private static byte[] streamToBytes(InputStream is) {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int len;
            try {
                while ((len = is.read(buffer)) >= 0) {
                    os.write(buffer, 0, len);
                }
            } catch (java.io.IOException e) {
            }
            return os.toByteArray();
        }

        public GifViewer(Context context, int r_gif ) {
            super(context);
            setFocusable(true);

            java.io.InputStream is;
 
            is = context.getResources().openRawResource(r_gif);//R.drawable.ninja_turtle);

            if (DECODE_STREAM) {
                mMovie = Movie.decodeStream(is);
            } else {
                byte[] array = streamToBytes(is);
                mMovie = Movie.decodeByteArray(array, 0, array.length);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFCCCCCC);

            Paint p = new Paint();
            p.setAntiAlias(true);

            long now = android.os.SystemClock.uptimeMillis();
            if (mMovieStart == 0) {   // first time
                mMovieStart = now;
            }
            if (mMovie != null) {
                int dur = mMovie.duration();
                if (dur == 0) {
                    dur = 1000;
                }
                int relTime = (int)((now - mMovieStart) % dur);
                mMovie.setTime(relTime);
                mMovie.draw(canvas, (getWidth() - mMovie.width())/2, 0);
                            
               /*
                *  mMovie.draw(canvas, getWidth()-/4, 0);
					getHeight() - mMovie.height());
                         */
                invalidate();
            }
        }
}
