package com.hoccer.talk.android;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hoccer.talk.android.R;
import com.hoccer.talk.android.R.id;
import com.hoccer.talk.android.R.layout;
import com.hoccer.talk.android.R.menu;
import com.hoccer.talk.android.model.TalkMessage;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class OldTalkActivity extends Activity {

	MessageStore mMessageStore;
	
	ListView mMessageList;
	EditText mMessageEdit;
	Button   mMessageSend;

	ScheduledExecutorService mBackgroundExecutor = Executors.newSingleThreadScheduledExecutor();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mMessageStore = new MessageStore(this);
		
		mMessageList = (ListView)findViewById(R.id.message_list);
		mMessageEdit = (EditText)findViewById(R.id.message_edit);
		mMessageSend = (Button)findViewById(R.id.message_send);
		
		mMessageList.setAdapter(mMessageStore);
		
		mMessageSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TalkMessage m = new TalkMessage("me", mMessageEdit.getText().toString());
				mMessageStore.add(m);
				mMessageEdit.setText("");
			}
		});
		
		generateTestMessages();
	}
	
	
	
	@Override
	protected void onPause() {
		mBackgroundExecutor.shutdownNow();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		generateMessagesInBackground();
	}



	private void generateTestMessages() {
		for(int i = 0; i < 10; i++) {
			boolean foo = (i & 2) == 0;
			TalkMessage m = new TalkMessage(foo ? "Pia" : "George", "Test message #" + i);
			mMessageStore.add(m);
		}
	}
	
	private void generateMessagesInBackground() {
		long wait = Math.round(Math.random() * 5000.0 + 1000.0);
		Log.d("Foo", "Message in " + wait);
		mBackgroundExecutor.schedule(new Runnable() {
			
			@Override
			public void run() {
				Log.d("Foo", "Delivering message from Udo");
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Log.d("Foo", "Scheduling next delivery");
						TalkMessage m = new TalkMessage("Udo", "Foobar!");

						mMessageStore.add(m);
						generateMessagesInBackground();
					}
				});

			}
		}, wait, TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	class MessageStore extends ArrayAdapter<TalkMessage> {

		LayoutInflater mInflater;
		
		public MessageStore(Context context) {
			super(context, R.layout.message_list_item, R.id.item_content, new ArrayList<TalkMessage>());
			mInflater = OldTalkActivity.this.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TalkMessage m = getItem(position);
			View v = convertView;
			
			if(v == null) {
				v = mInflater.inflate(R.layout.message_list_item, null);
			}
			
			TextView content = (TextView)v.findViewById(R.id.item_content);
			TextView sender = (TextView)v.findViewById(R.id.item_sender_name);
			
			content.setText(m.getContent());
			sender.setText(m.getSender());
			
			return v;
		}
		
		
		
	}

}
