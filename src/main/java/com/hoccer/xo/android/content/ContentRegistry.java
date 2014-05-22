package com.hoccer.xo.android.content;

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
import com.hoccer.talk.content.IContentObject;
import com.hoccer.xo.android.base.XoActivity;
import com.hoccer.xo.android.content.audio.AudioViewCache;
import com.hoccer.xo.android.content.audio.MusicSelector;
import com.hoccer.xo.android.content.clipboard.ClipboardSelector;
import com.hoccer.xo.android.content.data.DataViewCache;
import com.hoccer.xo.android.content.image.ImageSelector;
import com.hoccer.xo.android.content.image.ImageViewCache;
import com.hoccer.xo.android.content.image.VideoSelector;
import com.hoccer.xo.android.content.image.VideoViewCache;
import com.hoccer.xo.android.content.location.LocationViewCache;
import com.hoccer.xo.android.content.location.MapsLocationSelector;
import com.hoccer.xo.android.content.vcard.ContactSelector;
import com.hoccer.xo.android.content.vcard.ContactViewCache;
import com.hoccer.xo.android.util.IntentHelper;
import com.hoccer.xo.release.R;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import com.hoccer.xo.android.content.audio.ButtonAudioViewCache;

/**
 * Content registry
 *
 * This singleton is responsible for attachment and avatar selection and viewing.
 *
 * Essentially, this keeps a registry of selectors and viewers and provides
 * some frontend methods for using them.
 *
 */
public class ContentRegistry {

    private static final Logger LOG = Logger.getLogger(ContentRegistry.class);

    private static ContentRegistry INSTANCE = null;

    /**
     * Get the content registry singleton
     *
     * The given context will be used to initialize the registry
     * if there isn't one already.
     *
     * The given context MUST be the application context.
     *
     * @param applicationContext to work in
     * @return the content registry
     */
    public static synchronized ContentRegistry get(Context applicationContext) {
        if(INSTANCE == null) {
            INSTANCE = new ContentRegistry(applicationContext);
        }
        return INSTANCE;
    }

    /** Context for this registry */
    Context mContext;

    /** The avatar selector (usually a GallerySelector) */
    IContentSelector mAvatarSelector;

    /** Active attachment selectors (only the supported ones)  */
    List<IContentSelector> mAttachmentSelectors = new ArrayList<IContentSelector>();

    /** Active attachment viewer providers (only the supported ones) */
    List<ContentViewCache> mAttachmentViewCaches = new ArrayList<ContentViewCache>();

    private ClipboardSelector mClipboardSelector;

    private ContentRegistry(Context context) {
        mContext = context;
        initialize();
    }

    /**
     * Initialize the registry
     *
     * This methods activates supported content selectors and viewers.
     */
    private void initialize() {
        mAvatarSelector = new ImageSelector(mContext);

        initializeSelector(new ImageSelector(mContext));
        initializeSelector(new VideoSelector(mContext));
        initializeSelector(new MusicSelector(mContext));
        initializeSelector(new ContactSelector(mContext));
        initializeSelector(new MapsLocationSelector(mContext));
//        initializeSelector(new DataSelector(mContext));

        mClipboardSelector = new ClipboardSelector(mContext);

        mAttachmentViewCaches.add(new ImageViewCache());
        mAttachmentViewCaches.add(new VideoViewCache());
//        mAttachmentViewCaches.add(new ButtonAudioViewCache());
        mAttachmentViewCaches.add(new AudioViewCache());
        mAttachmentViewCaches.add(new ContactViewCache());
        mAttachmentViewCaches.add(new LocationViewCache());
        mAttachmentViewCaches.add(new DataViewCache());
    }

    /**
     * Check if the given selector is supported on this device
     *
     * Adds the selector to the active list when supported.
     *
     * @param selector to add if supported
     */
    private void initializeSelector(IContentSelector selector) {
        Intent intent = selector.createSelectionIntent(mContext);
        if(IntentHelper.isIntentResolvable(intent, mContext)) {
            LOG.debug("content selector "  + selector.getName() + " / "+ selector.getClass().getSimpleName() + " activated");
            mAttachmentSelectors.add(selector);
        } else {
            LOG.warn("content selector "  + selector.getName() + " / "+ selector.getClass().getSimpleName() + " not supported");
        }
    }

    public String getContentDescription(IContentObject object) {
        String mediaTypeString = "Unknown file";
        String mediaType = object.getContentMediaType();
        if(mediaType != null) {
            if(mediaType.equals("image")) {
                mediaTypeString = "Image";
            } else if(mediaType.equals("audio")) {
                mediaTypeString = "Audio";
            } else if(mediaType.equals("video")) {
                mediaTypeString = "Video";
            } else if(mediaType.equals("contact")) {
                mediaTypeString = "Contact";
            } else if(mediaType.equals("location")) {
                mediaTypeString = "Location";
            } else if(mediaType.equals("data")) {
                mediaTypeString = "Data";
            }
        }

        String sizeString = "";
        if(object.getContentLength() > 0) {
            sizeString = " —" + humanReadableByteCount(object.getContentLength(), true);
        } else if (object.getTransferLength() > 0) {
            sizeString = " —" + humanReadableByteCount(object.getTransferLength(), true);
        }

        return mediaTypeString + sizeString;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) { return bytes + " B"; }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Creates a View for displaying the given content
     *
     * This should be called by ContentView when the displayed object changes.
     *
     * @param activity the view will be used in
     * @param contentObject to display
     * @param view that will host the returned view
     * @return a View set up for the given content
     */
    public View createViewForContent(Activity activity, IContentObject contentObject, ContentView view, boolean isLightTheme) {
        ContentViewCache contentViewCache = selectViewCacheForContent(contentObject);
        if(contentViewCache != null) {
            return contentViewCache.getViewForObject(activity, view, contentObject, isLightTheme);
        }
        return null;
    }

    /**
     * Selects the viewer for a content object
     *
     * First viewer that can show the content will be chosen.
     *
     * @param contentObject that needs a view constructed
     * @return a matching content viewer
     */
    public ContentViewCache selectViewCacheForContent(IContentObject contentObject) {
        for(ContentViewCache viewCache: mAttachmentViewCaches) {
            if(viewCache.canViewObject(contentObject)) {
                return viewCache;
            }
        }
        return null;
    }

    /**
     * Starts avatar selection
     *
     * This will jump directly to the Android gallery.
     *
     * @param activity that is requesting the selection
     * @param requestCode identifying returned intents
     * @return a new selection handle object
     */
    public ContentSelection selectAvatar(Activity activity, int requestCode) {
        ContentSelection cs = new ContentSelection(activity, mAvatarSelector);
        Intent intent = mAvatarSelector.createSelectionIntent(activity);
        XoActivity xoActivity = (XoActivity)activity;
        xoActivity.startExternalActivityForResult(intent, requestCode);
        return cs;
    }

    /**
     * Create a content object from an intent returned by content selection
     *
     * Activities should call this when they receive results with the request
     * code they associate with avatar selection (as given to selectAvatar).
     *
     * @param selection handle for the in-progress avatar selection
     * @param intent returned from the selector
     * @return content object for selected avatar
     */
    public IContentObject createSelectedAvatar(ContentSelection selection, Intent intent) {
        return selection.getSelector().createObjectFromSelectionResult(selection.getActivity(), intent);
    }

    /* Keys for use with the internal SimpleAdapter in attachment selection */
    private static final String KEY_ICON = "icon";
    private static final String KEY_NAME = "name";
    private static final String KEY_INTENT = "intent";
    private static final String KEY_SELECTOR = "selector";

    /**
     * Starts content selection
     *
     * Will create and show a dialog above the given activity
     * that allows the users to select a source for content selection.
     *
     * @param activity that is requesting the selection
     * @param requestCode identifying returned intents
     * @return a new selection handle object
     */
    public ContentSelection selectAttachment(final Activity activity, final int requestCode) {
        // create handle representing this selection attempt
        final ContentSelection cs = new ContentSelection(activity);

        // collect selection intents and associated information
        final List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        for(IContentSelector selector: mAttachmentSelectors) {
            Intent selectionIntent = selector.createSelectionIntent(activity);
            if(IntentHelper.isIntentResolvable(selectionIntent, activity)) {
                Map<String, Object> fields = new HashMap<String, Object>();
                fields.put(KEY_INTENT, selectionIntent);
                fields.put(KEY_SELECTOR, selector);
                fields.put(KEY_ICON, selector.getContentIcon());
                fields.put(KEY_NAME, selector.getName());
                options.add(fields);
            }
        }

        // Add ClipboardSelector when it has something to process
        if (mClipboardSelector.canProcessClipboard()) {
            Map<String, Object> fields = createDataObjectFromContentSelector(activity, mClipboardSelector);
            if (fields != null) {
                options.add(fields);
            }
        }

        // prepare an adapter for the selection options
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

        // build the selection dialog
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
            public void onClick(DialogInterface dialog, int index) {
                Map<String, Object> sel = options.get(index);
                IContentSelector selector = (IContentSelector)sel.get(KEY_SELECTOR);
                cs.setSelector(selector);

                // handle ClipboardSelector differently
                if (selector instanceof ClipboardSelector) {
                    ClipboardSelector clipboardSelector = (ClipboardSelector)selector;
                    XoActivity xoActivity = (XoActivity)activity;
                    xoActivity.clipBoardItemSelected(clipboardSelector.selectObjectFromClipboard(xoActivity));
                } else {
                    Intent intent = (Intent) sel.get(KEY_INTENT);
                    XoActivity xoActivity = (XoActivity)activity;
                    xoActivity.startExternalActivityForResult(intent, requestCode);
                }
            }
        });

        // configure the dialog
        Dialog dialog = builder.create();

        // and show it
        dialog.show();

        // return the selection handle
        return cs;
    }

    /**
     * Create a content object from an intent returned by content selection
     *
     * Activities should call this when they receive results with the request
     * code they associate with content selection (as given to selectAttachment).
     *
     * @param selection handle for the in-progress content selection
     * @param intent returned from the selector
     * @return content object for selected content
     */
    public IContentObject createSelectedAttachment(ContentSelection selection, Intent intent) {
        IContentSelector selector = selection.getSelector();
        if(selector != null) {
            IContentObject object = selector.createObjectFromSelectionResult(selection.getActivity(), intent);
            return object;
        }
        return null;
    }

    /**
     * Creates a dialog entry data object from a given IContentSelector.
     *
     * @param activity that is requesting the selection
     * @param selector the given IContentSelector
     * @return a Map containing all relevant intent information
     */
    private Map<String, Object> createDataObjectFromContentSelector(final Activity activity, final IContentSelector selector) {

        Intent selectionIntent = selector.createSelectionIntent(activity);

        if (IntentHelper.isIntentResolvable(selectionIntent, activity)) {
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put(KEY_INTENT, selectionIntent);
            fields.put(KEY_SELECTOR, selector);
            fields.put(KEY_ICON, selector.getContentIcon());
            fields.put(KEY_NAME, selector.getName());
            return fields;
        }

        return null;
    }

}
