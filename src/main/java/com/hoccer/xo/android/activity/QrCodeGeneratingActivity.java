package com.hoccer.xo.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.hoccer.talk.client.IXoContactListener;
import com.hoccer.talk.client.model.TalkClientContact;
import com.hoccer.xo.android.XoApplication;
import com.hoccer.xo.release.R;

import java.util.EnumMap;
import java.util.Map;

public class QrCodeGeneratingActivity extends Activity implements IXoContactListener {
    private ImageView targetImageView;
    private String mQrString;
    private int mPairedContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);
        targetImageView = (ImageView) findViewById(R.id.iv_qr_code);
        Intent intent = getIntent();
        if (intent != null) {
            mQrString = intent.getExtras().getString("QR");
        }
        XoApplication.getXoClient().registerContactListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bitmap barcode_bitmap = null;
        try {
            barcode_bitmap = encodeAsBitmap(mQrString, BarcodeFormat.QR_CODE, 400, 400);
            targetImageView.setImageBitmap(barcode_bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private static Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height)
            throws WriterException {
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contents, format, img_width, img_height, hints);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    public void onContactAdded(TalkClientContact contact) {

    }

    @Override
    public void onContactRemoved(TalkClientContact contact) {

    }

    @Override
    public void onClientPresenceChanged(TalkClientContact contact) {
        if (!isFinishing() && mPairedContact == contact.getClientContactId()) {
            final TalkClientContact newContact = contact;
            final Context context = this;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(getResources().getString(R.string.paired_with) + " " + newContact.getName())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent contactList = new Intent(context, ContactsActivity.class);
                                    contactList.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    context.startActivity(contactList);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }

    @Override
    public void onClientRelationshipChanged(TalkClientContact contact) {
        mPairedContact = contact.getClientContactId();
    }

    @Override
    public void onGroupPresenceChanged(TalkClientContact contact) {

    }

    @Override
    public void onGroupMembershipChanged(TalkClientContact contact) {

    }
}
