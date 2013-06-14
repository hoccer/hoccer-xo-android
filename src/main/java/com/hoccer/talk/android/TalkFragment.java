package com.hoccer.talk.android;

import android.app.Activity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.apache.log4j.Logger;

public class TalkFragment extends SherlockFragment {

    protected Logger LOG = null;

    private TalkActivity mActivity;

    public TalkFragment() {
        LOG = Logger.getLogger(getClass());
    }

    public TalkActivity getTalkActivity() {
        return mActivity;
    }

    @Override
    public void onAttach(Activity activity) {
        LOG.info("onAttach()");
        super.onAttach(activity);

        if(activity instanceof TalkActivity) {
            mActivity = (TalkActivity)activity;
        } else {
            throw new RuntimeException("Talk fragments need to be in a talk activity");
        }
    }

}
