package com.hoccer.xo.android.util;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import com.hoccer.xo.android.XoApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * This class is the central source for thumbnail representations of given image attachments.
 * It creates, stores, caches scaled and masked bitmaps of specified images.
 * <p/>
 * it manage
 */
public class ThumbnailManager {
    private static ThumbnailManager mInstance;
    private LruCache mMemoryLruCache;
    private Context mContext;
    private Map mImageViews = Collections.synchronizedMap(new WeakHashMap());
    private Drawable mStubDrawable;

    private ThumbnailManager(Context context) {
        mContext = context;
        init(context);
    }

    public static ThumbnailManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ThumbnailManager(context);
        }
        return mInstance;
    }

    private void init(Context context) {
        final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        // 1/8 of the available mem
        final int cacheSize = 1024 * 1024 * memClass / 8;
        mMemoryLruCache = new LruCache(cacheSize);
        mStubDrawable = new ColorDrawable(Color.LTGRAY);
    }

    /**
     * Retrieves a thumbnail representation of an image at a specified uri and adds it to a given ImageView.
     *
     * @param uri          The uri of the image
     * @param imageView    The ImageView which will display the thumbnail
     * @param maskResource The resource id of a drawable to mask the thumbnail
     */
    public void displayThumbnailForImage(String uri, ImageView imageView, int maskResource) {
        mImageViews.put(imageView, uri);
        Bitmap bitmap = null;
        if (uri != null) {
            bitmap = (Bitmap) mMemoryLruCache.get(uri);
        }
        if (bitmap == null) {
            bitmap = loadThumbnailForImage(uri);
        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setImageDrawable(mStubDrawable);
            if (uri != null) {
                queueThumbnailCreation(uri, imageView, maskResource);
            }
        }
    }

    private Bitmap loadThumbnailForImage(String uri) {
        String thumbnailFilename = uri.substring(uri.lastIndexOf("/") + 1, uri.length());
        File thumbnail = new File(XoApplication.getThumbnailDirectory(), thumbnailFilename);
        Bitmap bitmap = null;
        if (thumbnail.exists()) {
            bitmap = BitmapFactory.decodeFile(thumbnail.getAbsolutePath());
            if (bitmap != null) {
                mMemoryLruCache.put(uri, bitmap);
            }
        }
        return bitmap;
    }

    private void saveToThumbnailDirectory(Bitmap bitmap, String filename) {
        filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
        File destination = new File(XoApplication.getThumbnailDirectory(), filename);

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(destination));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private Bitmap rotateBitmap(Bitmap bitmap, String filePath) {
        int rotation = 0;
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case 6:
                    rotation = 90;
                    break;
                case 3:
                    rotation = 180;
                    break;
                case 8:
                    rotation = -90;
                    break;
                default:
                    rotation = 0;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, Context context) {
        //200dp in item_chat_message.xml -> rl_message_attachment -> height
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float scaledHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, metrics);
        float scaledWidth = bitmap.getWidth() * (scaledHeight / bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, Math.round(scaledWidth), Math.round(scaledHeight), false);
    }

    private Bitmap getNinePatchMask(int id, int x, int y, Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        byte[] chunk = bitmap.getNinePatchChunk();
        NinePatchDrawable drawable = new NinePatchDrawable(context.getResources(), bitmap, chunk, new Rect(), null);
        drawable.setBounds(0, 0, x, y);
        Bitmap result = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.draw(canvas);
        return result;
    }

    private String getRealPathFromURI(Uri contentURI, Context context) {
        String result = "";
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
                cursor.close();
            }
        }
        return result;
    }

    private void queueThumbnailCreation(String uri, ImageView imageView, int maskResource) {
        new LoadBitmapTask().execute(uri, imageView, maskResource);
    }

    private Bitmap createThumbnail(String uri, int maskResource) {
        Bitmap thumbnail = null;
        File imageFile = new File(getRealPathFromURI(Uri.parse(uri), mContext));
        if (imageFile.exists()) {
            thumbnail = renderThumbnail(imageFile, maskResource);
            if (thumbnail != null) {
                saveToThumbnailDirectory(thumbnail, uri);
                return thumbnail;
            }
        }
        return thumbnail;
    }

    private Bitmap renderThumbnail(File file, int maskResource) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 4;
        Bitmap original = BitmapFactory.decodeFile(file.getAbsolutePath(), opt);
        original = rotateBitmap(original, file.getAbsolutePath());
        original = scaleBitmap(original, mContext);
        //Load mask
        Bitmap mask = getNinePatchMask(maskResource, original.getWidth(), original.getHeight(), mContext);
        //Draw everything on canvas
        Bitmap result = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        c.drawBitmap(original, 0, 0, null);
        c.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(null);
        return result;
    }


    private class ImageToLoad {
        public String mUrl;
        public ImageView mImageView;

        public ImageToLoad(String url, ImageView imageView) {
            mUrl = url;
            mImageView = imageView;
        }
    }

    private boolean imageViewReused(ImageToLoad imageToLoad) {
        String tag = (String) mImageViews.get(imageToLoad.mImageView);
        if (tag == null || !tag.equals(imageToLoad.mUrl))
            return true;
        return false;
    }

    class LoadBitmapTask extends AsyncTask<Object, Object, Bitmap> {
        private ImageToLoad mImageToLoad;
        private int mMaskResource;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object[] params) {
            String uri = (String) params[0];
            mImageToLoad = new ImageToLoad(uri, (ImageView) params[1]);
            if (imageViewReused(mImageToLoad)) {
                return null;
            }
            mMaskResource = (Integer) params[2];
            Bitmap thumbnail = createThumbnail(mImageToLoad.mUrl, mMaskResource);
            if (thumbnail == null) {
                return null;
            }
            mMemoryLruCache.put(mImageToLoad.mUrl, thumbnail);
            return thumbnail;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReused(mImageToLoad)) {
                return;
            }
            if (bitmap != null) {
                mImageToLoad.mImageView.setImageBitmap(bitmap);
                mImageToLoad.mImageView.setVisibility(View.VISIBLE);
            } else {
                mImageToLoad.mImageView.setImageDrawable(mStubDrawable);
            }
        }
    }
}
