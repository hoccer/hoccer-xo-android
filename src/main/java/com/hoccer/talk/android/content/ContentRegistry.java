package com.hoccer.talk.android.content;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import com.hoccer.talk.android.R;
import com.hoccer.talk.android.content.audio.AudioViewer;
import com.hoccer.talk.android.content.audio.MusicSelector;
import com.hoccer.talk.android.content.image.GallerySelector;
import com.hoccer.talk.android.content.image.ImageViewer;
import com.hoccer.talk.android.content.vcard.ContactSelector;
import com.hoccer.talk.android.content.vcard.ContactViewer;
import com.hoccer.talk.android.util.IntentHelper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentRegistry {

    private static final Logger LOG = Logger.getLogger(ContentRegistry.class);

    private static ContentRegistry INSTANCE = null;

    public static synchronized ContentRegistry get(Context applicationContext) {
        if(INSTANCE == null) {
            INSTANCE = new ContentRegistry(applicationContext);
        }
        return INSTANCE;
    }

    Context mContext;

    IContentSelector mAvatarSelector;

    List<IContentSelector> mAttachmentSelectors = new ArrayList<IContentSelector>();
    List<IContentViewer> mAttachmentViewers = new ArrayList<IContentViewer>();

    public ContentRegistry(Context context) {
        mContext = context;
        initialize();
    }

    private void initialize() {
        mAvatarSelector = new GallerySelector();

        initializeSelector(new GallerySelector());
        initializeSelector(new MusicSelector());
        initializeSelector(new ContactSelector());

        mAttachmentViewers.add(new ImageViewer());
        mAttachmentViewers.add(new AudioViewer());
        mAttachmentViewers.add(new ContactViewer());
    }

    private void initializeSelector(IContentSelector selector) {
        Intent intent = selector.createSelectionIntent(mContext);
        if(IntentHelper.isIntentResolvable(intent, mContext)) {
            LOG.info("content selector "  + selector.getName() + " / "+ selector.getClass().getSimpleName() + " activated");
            mAttachmentSelectors.add(selector);
        } else {
            LOG.warn("content selector "  + selector.getName() + " / "+ selector.getClass().getSimpleName() + " not supported");
        }
    }

    public IContentViewer selectViewerForContent(ContentObject contentObject) {
        for(IContentViewer viewer: mAttachmentViewers) {
            if(viewer.canViewObject(contentObject)) {
                return viewer;
            }
        }
        return null;
    }

    public View createViewForContent(Activity activity, ContentObject contentObject, ContentView view) {
        IContentViewer viewer = selectViewerForContent(contentObject);
        if(viewer != null) {
            return viewer.getViewForObject(activity, contentObject, view);
        }
        return null;
    }

    public ContentSelection selectAvatar(Activity activity, int requestCode) {
        ContentSelection cs = new ContentSelection(activity, mAvatarSelector);
        Intent intent = mAvatarSelector.createSelectionIntent(activity);
        activity.startActivityForResult(intent, requestCode);
        return cs;
    }

    public ContentObject createSelectedAvatar(ContentSelection selection, Intent intent) {
        return selection.getSelector().createObjectFromSelectionResult(selection.getActivity(), intent);
    }

    private static final String KEY_ICON = "icon";
    private static final String KEY_NAME = "name";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_SELECTOR = "selector";

    public ContentSelection selectAttachment(final Activity activity, final int requestCode) {

        final List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        for(IContentSelector selector: mAttachmentSelectors) {
            Intent selectionIntent = selector.createSelectionIntent(activity);
            if(IntentHelper.isIntentResolvable(selectionIntent, activity)) {
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put(KEY_INTENT, selectionIntent);
                fields.put(KEY_SELECTOR, selector);
                fields.put(KEY_ICON, IntentHelper.getIconForIntent(selectionIntent, activity));
                fields.put(KEY_NAME, selector.getName());
                options.add(fields);
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(activity, options, R.layout.select_content,
                new String[]{KEY_ICON, KEY_NAME},
                new int[]{R.id.select_content_icon, R.id.select_content_text});
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView) {
                    ImageView image = (ImageView)view.findViewById(R.id.select_content_icon);
                    image.setImageDrawable((Drawable)data);
                    return true;
                }
                return false;
            }
        });

        final ContentSelection cs = new ContentSelection(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.selectattachment_title);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> sel = options.get(which);
                IContentSelector selector = (IContentSelector)sel.get(KEY_SELECTOR);
                cs.setSelector(selector);
                Intent intent = (Intent)sel.get(KEY_INTENT);
                activity.startActivityForResult(intent, requestCode);
            }
        });

        Dialog dialog = builder.create();
        dialog.show();

        return cs;
    }

    public ContentObject createSelectedAttachment(ContentSelection selection, Intent intent) {
        IContentSelector selector = selection.getSelector();
        if(selector != null) {
            ContentObject object = selector.createObjectFromSelectionResult(selection.getActivity(), intent);
            if(object != null) {
                object.setState(ContentObject.State.SELECTED);
                object.setAvailable(true);
            }
            return object;
        }
        return null;
    }

}
