package com.hoccer.talk.android.fragment;

import java.util.logging.Logger;

import com.hoccer.talk.android.R;
import com.hoccer.talk.android.TalkActivity;
import com.hoccer.talk.logging.HoccerLoggers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class MessagingFragment extends Fragment {

	private static final Logger LOG =
			HoccerLoggers.getLogger(MessagingFragment.class);
	
	TalkActivity mActivity;
	
	ListView mMessageList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		LOG.info("onCreate()");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		LOG.info("onAttach()");
		super.onAttach(activity);
		
		if (activity instanceof TalkActivity) {
			mActivity = (TalkActivity) activity;
		} else {
			throw new ClassCastException(
				activity.toString() + " must implement TalkActivity");
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("onCreateView()");

		View v = inflater.inflate(R.layout.fragment_messaging, container, false);
		
		mMessageList = (ListView)v.findViewById(R.id.messaging_message_list);
		mMessageList.setAdapter(mActivity.makeMessageListAdapter());
		
		return v;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

}
