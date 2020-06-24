package com.appbestsmile.voicelikeme.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appbestsmile.voicelikeme.R;

import java.util.List;

public class TopicListAdapter extends ArrayAdapter<TopicItem> {

    private int resourceLayout;
    private Context mContext;

    public TopicListAdapter(Context context, int resource, List<TopicItem> items) {
        super(context, resource, items);
        this.resourceLayout = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        TopicItem topicItem = getItem(position);

        if (topicItem != null) {
            TextView textTopic = (TextView) v.findViewById(R.id.textTopic);
            TextView textCreatedDate = (TextView) v.findViewById(R.id.textCreatedDate);

            if (textTopic != null) {
                textTopic.setText(topicItem.getTitle());
            }

            if (textCreatedDate != null) {
                textCreatedDate.setText(topicItem.getCreatedDate());
            }
        }
        return v;
    }
}
