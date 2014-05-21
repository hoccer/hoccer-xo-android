package com.hoccer.xo.android.adapter;

import com.hoccer.xo.release.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by jacob on 21.05.14.
 */
public class EmojiGridAdapter extends BaseAdapter {
    List<String> mEmojiList;

    public EmojiGridAdapter() {
        initEmojiList();
    }

    private void initEmojiList() {
        mEmojiList = new ArrayList<String>();
        mEmojiList.addAll(getEmojisAsList(0xd83d, 0xde00, 0, 66));
//        mEmojiList.addAll(getEmojisAsList(0xd83d, 0xde00, 70, 81));
    }

    private List<String> getEmojisAsList(int char1, int char2, int rangeStart, int rangeEnd) {
        List<String> emojiList = new ArrayList<String>();
        char[] emoji = new char[2];
        emoji[0] = (char) char1;
        for(int i = rangeStart; i < rangeEnd; i++) {
            emoji[1] = (char) (char2 + i);
            emojiList.add(new String(emoji));
        }
        return emojiList;
    }

    @Override
    public int getCount() {
        return mEmojiList.size();
    }

    @Override
    public String getItem(int position) {
        return mEmojiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.emoji_grid_item, null);
        }

        TextView emojiButton = (TextView) convertView.findViewById(R.id.btn_emoji);
        emojiButton.setText(getItem(position));

        return convertView;
    }
}
