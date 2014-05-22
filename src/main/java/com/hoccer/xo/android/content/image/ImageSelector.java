package com.hoccer.xo.android.content.image;

import android.graphics.drawable.Drawable;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.android.content.IContentSelector;
import com.hoccer.xo.android.content.SelectedContent;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import com.whitelabel.gw.release.R;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class ImageSelector implements IContentSelector {

    Logger LOG = Logger.getLogger(ImageSelector.class);

    private String mName;
    private Drawable mIcon;

    public ImageSelector(Context context) {
        mName = context.getResources().getString(R.string.content_images);
        mIcon = context.getResources().getDrawable(R.drawable.ic_attachment_select_image);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Drawable getContentIcon() {
        return mIcon;
    }

    @Override
    public Intent createSelectionIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        return intent;
    }

    public Intent createCropIntent(Context context, Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP", android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);

        File tmpFile = new File(XoApplication.getAttachmentDirectory(), "tmp_crop");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tmpFile));
        return intent;
    }


    @Override
    public SelectedContent createObjectFromSelectionResult(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        if (selectedContent.toString().startsWith("content://com.google.android.gallery3d")) {
            return createFromPicasa(context, intent);
        } else {
            return createFromFile(context, intent);
        }
    }

    private SelectedContent createFromPicasa(final Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.Images.Media.ORIENTATION};
        Cursor cursor = context.getContentResolver().query(selectedContent, filePathColumn, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
            if (columnIndex != -1) {
                try {
                    String displayName = cursor.getString(columnIndex);
                    final Uri contentUri = selectedContent;
                    Bitmap bmp = getBitmap(context, displayName, contentUri);
                    File imageFile = new File(XoApplication.getAttachmentDirectory(), displayName);

                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(imageFile));

                    int fileWidth = bmp.getWidth();
                    int fileHeight = bmp.getHeight();
                    int orientation = 0;
                    double aspectRatio = (double) bmp.getWidth() / (double) bmp.getHeight();

                    int orientationIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);
                    if (orientationIndex != -1) {
                        orientation = cursor.getInt(orientationIndex);
                        aspectRatio = calculateAspectRatio(fileWidth, fileHeight, orientation);
                    }
                    LOG.debug("Aspect ratio: " + fileWidth + " x " + fileHeight + " @ " + aspectRatio + " / " + orientation + "°");

                    SelectedContent contentObject = new SelectedContent(intent, "file://" + imageFile.getAbsolutePath());
                    contentObject.setFileName(displayName);
                    contentObject.setContentType("image/jpeg");
                    contentObject.setContentMediaType("image");
                    contentObject.setContentLength((int) imageFile.length());
                    contentObject.setContentAspectRatio(aspectRatio);
                    return contentObject;
                } catch (FileNotFoundException e) {
                    LOG.error("Error while creating image from Picasa: ", e);
                }
            }
        }

        return null;
    }

    private Bitmap getBitmap(Context context, String fileName, Uri url) {
        File cacheDir = XoApplication.getAttachmentDirectory();
        File f = new File(cacheDir, fileName);
        try {
            InputStream is = null;
            if (url.toString().startsWith("content://com.google.android.gallery3d")) {
                is = context.getContentResolver().openInputStream(url);
            } else {
                is = new URL(url.toString()).openStream();
            }
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            LOG.error("Error while creating bitmap: ", e);
            return null;
        }
    }

    private SelectedContent createFromFile(Context context, Intent intent) {
        Uri selectedContent = intent.getData();
        String[] filePathColumn = {
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.ORIENTATION
        };

        Cursor cursor = context.getContentResolver().query(selectedContent, filePathColumn, null, null, null);
        cursor.moveToFirst();

        int mimeTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
        int dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
        int sizeIndex = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
        int widthIndex = cursor.getColumnIndex(MediaStore.Images.Media.WIDTH);
        int heightIndex = cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT);
        int fileNameIndex = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
        int orientationIndex = cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION);

        String mimeType = cursor.getString(mimeTypeIndex);
        String filePath = cursor.getString(dataIndex);
        int fileSize = cursor.getInt(sizeIndex);
        int fileWidth = cursor.getInt(widthIndex);
        int fileHeight = cursor.getInt(heightIndex);
        String fileName = cursor.getString(fileNameIndex);
        int orientation = cursor.getInt(orientationIndex);
        double aspectRatio;

        cursor.close();

        if (filePath == null) {
            filePath = selectedContent.toString();
        }

        // Validating file size
        File file = new File(filePath);
        int fileLength = (int) file.length();
        if (fileSize != fileLength) {
            LOG.debug("File size from content database is not actual file size. Reading file size from actual file.");
            fileSize = fileLength;
        }

        // Validating image measurements
        if (fileWidth == 0 || fileHeight == 0) {
            LOG.debug("Could not retrieve image measurements from content database. Will use values extracted from file instead.");

            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedContent);
                fileWidth = bmp.getWidth();
                fileHeight = bmp.getHeight();

            } catch (IOException e) {
                LOG.error("Error while creating image from file: " + filePath, e);

                // safeguard
                fileWidth = 1;
                fileHeight = 1;
            }
        }

        aspectRatio = calculateAspectRatio(fileWidth, fileHeight, orientation);

        LOG.debug("Aspect ratio: " + fileWidth + " x " + fileHeight + " @ " + aspectRatio + " / " + orientation + "°");

        SelectedContent contentObject = new SelectedContent(intent, "file://" + filePath);
        contentObject.setFileName(fileName);
        contentObject.setContentType(mimeType);
        contentObject.setContentMediaType("image");
        contentObject.setContentLength(fileSize);
        contentObject.setContentAspectRatio(aspectRatio);
        return contentObject;
    }

    private double calculateAspectRatio(int fileWidth, int fileHeight, int orientation) {
        double aspectRatio;
        if (orientation == 0 || orientation == 180) {
            aspectRatio = (double) fileWidth / (double) fileHeight;
        } else {
            aspectRatio = (double) fileHeight / (double) fileWidth;
        }
        return aspectRatio;
    }

}
