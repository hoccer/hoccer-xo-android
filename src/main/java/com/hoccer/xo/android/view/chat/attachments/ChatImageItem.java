package com.hoccer.xo.android.view.chat.attachments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.hoccer.talk.client.model.TalkClientMessage;
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.view.chat.ChatMessageItem;
import com.hoccer.xo.release.R;
import java.io.IOException;


public class ChatImageItem extends ChatMessageItem {

    /**
     * Caches the loaded attachment image
     */

    public ChatImageItem(Context context, TalkClientMessage message) {
        super(context, message);
    }

    public ChatItemType getType() {
        return ChatItemType.ChatItemWithImage;
    }

    @Override
    protected void configureViewForMessage(View view) {
        super.configureViewForMessage(view);
        configureAttachmentViewForMessage(view);
    }

    @Override
    protected void displayAttachment(IContentObject contentObject, boolean isIncoming) {
        super.displayAttachment(contentObject, isIncoming);

        // add view lazily
        if (mContentWrapper.getChildCount() == 0) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            RelativeLayout imageLayout = (RelativeLayout) inflater.inflate(R.layout.content_image, null);
            mContentWrapper.addView(imageLayout);
        }
        ImageView imageView = (ClickableImageView) mContentWrapper.findViewById(R.id.civ_image_view);
        RelativeLayout root = (RelativeLayout) mContentWrapper.findViewById(R.id.rl_root);
        imageView.setVisibility(View.INVISIBLE);
        loadImage(root, imageView, contentObject.getContentDataUrl(), mContext, isIncoming);
    }

    private void loadImage(RelativeLayout root, ImageView view, String contentUrl, Context context, boolean isIncoming) {
        String path = getRealPathFromURI(Uri.parse(contentUrl), context);
        UpdateImageView task = new UpdateImageView(mContext, path, view, root, isIncoming);
        task.execute(null);
    }

    private class UpdateImageView extends AsyncTask<Object, Object, Bitmap> {
        private Context mContext;
        private String mPath;
        private ImageView mView;
        private boolean mIsIncoming;
        private RelativeLayout mRoot;

        public UpdateImageView(Context context, String path, ImageView view, RelativeLayout root, boolean isIncoming) {
            mContext = context;
            mPath = path;
            mView = view;
            mIsIncoming = isIncoming;
            mRoot = root;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(Object[] objects) {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 4;
            Bitmap original = BitmapFactory.decodeFile(mPath, opt);
            original = rotateBitmap(original, mPath);
            original = scaleBitmap(original, mContext);
            //Load mask
            int maskResource = R.drawable.bubble_green;
            if (mIsIncoming) {
                maskResource = R.drawable.bubble_grey;
            }
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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mView.setImageBitmap(bitmap);
            mView.setVisibility(View.VISIBLE);
            if (mIsIncoming) {
                mRoot.setGravity(Gravity.LEFT);
            } else {
                mRoot.setGravity(Gravity.RIGHT);
            }
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
        float scaledWidth = bitmap.getWidth() * (scaledHeight/bitmap.getHeight());
        return Bitmap.createScaledBitmap(bitmap, Math.round(scaledWidth), Math.round(scaledHeight), false);
    }

    private Bitmap getNinePatchMask(int id,int x, int y, Context context){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), id);
        byte[] chunk = bitmap.getNinePatchChunk();
        NinePatchDrawable drawable = new NinePatchDrawable(context.getResources(), bitmap, chunk, new Rect(), null);
        drawable.setBounds(0, 0,x, y);
        Bitmap result = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.draw(canvas);
        return result;
    }

    private String getRealPathFromURI(Uri contentURI, Context context) {
        String result = ""  ;
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
}
