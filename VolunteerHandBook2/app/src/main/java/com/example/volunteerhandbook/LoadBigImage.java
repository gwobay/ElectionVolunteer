package com.example.volunteerhandbook;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class LoadBigImage {
	static Activity mActivity=null;
	static InputStream mCurrentInput=null;
	static FileDescriptor mFd=null;
	static Rect mOutPadding=null;
	static boolean useFd=true;
	static int instanceCount=0;
	static ArrayList<FinishLoadingListener> listeners=new ArrayList<FinishLoadingListener>();
	static HashMap<String, Bitmap> cache=new HashMap<String, Bitmap>();
	Uri mUri;
	public interface FinishLoadingListener
	{
		public void onImageLoaded(ImageView parent, Bitmap bmp);
	}
	
	public void addFinishLoadingListener(FinishLoadingListener aL)
	{
		listeners.add(aL);
	}
	private LoadBigImage() {
		// TODO Auto-generated constructor stub
		mUri=null;
	}

	public static Bitmap getCached(Uri _uri)
	{
		return cache.get(_uri.toString());
	}
	public static LoadBigImage getInstance(Activity v)
	{
		Log.d(LoadBigImage.class.getSimpleName(), "Got Called "+(instanceCount+1));
		if (instanceCount > 0) return null;
		instanceCount++;
		if (v!=null)
			mActivity=v;
		return new LoadBigImage();		
	}
	
	public void useFileDescriptor(boolean T_F)
	{
		useFd=T_F;
	}
	
	public static FileDescriptor getFd()
	{
		return mFd;
	}
	
	@Override
	protected void finalize()
	{
		instanceCount--;
		if (instanceCount < 0) instanceCount=0;
		Log.d(LoadBigImage.class.getSimpleName(), "!Got Called "+instanceCount);		
		try {
				if (mCurrentInput != null) 
				{
						mCurrentInput.close();
				} 
		super.finalize();
		} catch (Throwable e){}
	}
	
	public void loadBitMap(ImageView v, int resId)
	{
		BitmapWorkerTask task = new BitmapWorkerTask(v);
		Integer[] params=new Integer[]{resId, v.getWidth(), v.getHeight()};
		task.execute(params);
		try {
			Bitmap bm=task.get(1000, TimeUnit.MILLISECONDS);
			if (bm!=null) {
				if (listeners.size() > 0)
				{
					listeners.get(0).onImageLoaded(v, bm);
				}
				//v.setImageBitmap(bm);
                //v.postInvalidate();              
			}
		} catch (InterruptedException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch ( ExecutionException  e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch ( TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		 finalize();
	}

	public boolean loadBitMap(ImageView v, Uri imgUri, int reqWidth, int reqHeight)
	{
		Bitmap bmp=cache.get(imgUri.toString());
		if (bmp != null)
		{
			if (listeners.size() > 0)
			{
				listeners.get(0).onImageLoaded(v, bmp);
			}
			else 
			{
				v.setImageBitmap(bmp);
				v.invalidate();
			}
			finalize();
			return true;
		}
		cache.clear();
		try {
			mUri=imgUri;
			mOutPadding=new Rect(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
		mCurrentInput=mActivity.getContentResolver().openInputStream(imgUri);
		if (mCurrentInput != null)
		mFd=((FileInputStream)mCurrentInput).getFD();
		} catch (IOException e){
			Toast.makeText(mActivity, "file not found "+imgUri.toString(), Toast.LENGTH_SHORT).show();
			return false;
		}
		BitmapWorkerTask task = new BitmapWorkerTask(v);
		
		Integer[] params=new Integer[]{useFd?0:-1, reqWidth, reqHeight};
		task.execute(params);
		try {
			Bitmap bm=task.get(100000, TimeUnit.MILLISECONDS);
			if (bm!=null) {
				cache.put(mUri.toString(), bm);
				if (listeners.size() > 0)
				{
					listeners.get(0).onImageLoaded(v, bm);
				}
				//v.setImageBitmap(bm);
                //v.postInvalidate();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//task.cancel(true);
			//finalize();
			//return false;
		}
		if (task != null) task.cancel(true);
		finalize();
		return true;
	}
	
	public static BitmapFactory.Options getImageParams(Resources inRs, int imgRid)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		Resources res=inRs;
		if (res==null ) res=mActivity.getResources();
		BitmapFactory.decodeResource(res, imgRid, options);
		return options;
	}
	
	public static BitmapFactory.Options getUriImageParams(InputStream in)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		return options;
	}
	
	public static BitmapFactory.Options getFdImageParams(FileDescriptor fD)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fD, null, options);
		return options;
	}

	
	static final int NO_SCALE=1;
	public static int getScaleFactor(BitmapFactory.Options options, int reqWidth, int reqHeight) 
	{
		if (options.outHeight < reqHeight && options.outWidth < reqWidth) return NO_SCALE;		
		int scaleFactor=NO_SCALE;
		final int halfHeight = options.outHeight / 2;
        final int halfWidth = options.outWidth / 2;
		while (halfHeight > scaleFactor*reqHeight && halfWidth > scaleFactor*reqWidth)
			scaleFactor *= 2;
		return scaleFactor;
	}
	
	public static Bitmap scaleDownBitMap(Resources res, int resId,
	        int reqWidth, int reqHeight)
	{
		final BitmapFactory.Options options=getImageParams(res, resId);
		options.inSampleSize = getScaleFactor(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap scaleDownBitMap(FileDescriptor fd,  int reqWidth, int reqHeight)
	{
		final BitmapFactory.Options options=getFdImageParams(fd);
		options.inSampleSize = getScaleFactor(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    Bitmap bmp=BitmapFactory.decodeFileDescriptor(fd, null, options);//mOutPadding, options);
	    return bmp;
	} 
	
	public static Bitmap scaleDownBitMap(InputStream in,  int reqWidth, int reqHeight)
	{
		final BitmapFactory.Options options=getUriImageParams(in);
		options.inSampleSize = getScaleFactor(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    Bitmap bmp=BitmapFactory.decodeStream(in, null, options);//mOutPadding, options);
	    return bmp;
	} 
	
	private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
	    private final WeakReference<ImageView> imageViewReference;
	    private int data = 0;

	    public BitmapWorkerTask() {
	    	imageViewReference=null;
	    }
	    
	    
	    public BitmapWorkerTask(ImageView imageView) {
	        // Use a WeakReference to ensure the ImageView can be garbage collected
	        imageViewReference = new WeakReference<ImageView>(imageView);
	    }

	    // Decode image in background.
	    @Override
	    protected Bitmap doInBackground(Integer... params) {
	    	//ParcelFileDescriptor
	        data = params[0];
	        int reqWidth=params[1];
	        int reqHeight=params[2];
	        if (data < 0)
	        return scaleDownBitMap(mCurrentInput,  reqWidth, reqHeight);
	        if (data == 0)
	        	return scaleDownBitMap(LoadBigImage.getFd(),  reqWidth, reqHeight);
	        return scaleDownBitMap(mActivity.getResources(), data, reqWidth, reqHeight);
	    }

	    // Once complete, see if ImageView is still around and set bitmap.
	    @Override
	    protected void onPostExecute(Bitmap bitmap) {
	    	
	        if (imageViewReference != null && bitmap != null) {
	        	if (mUri != null){
	        		cache.clear();	        	
	        		cache.put(mUri.toString(), bitmap);
	        	}
	        	instanceCount--;
	            final ImageView imageView = imageViewReference.get();
	            if (imageView != null) {
	                imageView.setImageBitmap(bitmap);
	                imageView.postInvalidate();
	            }  
	            
	        }
	    }
	}
	
}
