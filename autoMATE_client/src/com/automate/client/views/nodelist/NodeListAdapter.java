package com.automate.client.views.nodelist;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.automate.client.R;
import com.automate.protocol.models.Node;

public class NodeListAdapter extends ArrayAdapter<Node> {

	private int mTitleResourceId;
	private int mSubtitleResourceId;
	private int mImageResourceId;
	private int mResource;
	private LayoutInflater mLayoutInflater;

	public NodeListAdapter(Context context, int resource, int titleResourceId, int subtitleResourceId, int imageResourceId, List<Node> objects) {
		super(context, resource, objects);
		this.mTitleResourceId = titleResourceId;
		this.mSubtitleResourceId = subtitleResourceId;
		this.mImageResourceId = imageResourceId;
		this.mResource = resource;
		this.mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		Node node = getItem(position);
		if(convertView == null) {
			holder = new Holder();
			convertView = this.mLayoutInflater.inflate(this.mResource, null);
			holder.image = (ImageView) convertView.findViewById(this.mImageResourceId);
			holder.nameView = (TextView) convertView.findViewById(this.mTitleResourceId);
			holder.manufacturerView = (TextView) convertView.findViewById(this.mSubtitleResourceId);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		//TODO add support for images
		holder.image.setImageResource(R.drawable.node_list);
		String nodeName = node.name;
		String modelName = node.model;
		String manufacturerName = node.manufacturerCode;
		if(nodeName == null || nodeName.isEmpty()) {
			holder.nameView.setText(modelName);
		} else {
			holder.nameView.setText(nodeName + "(" + modelName + ")");
		}
		holder.manufacturerView.setText(manufacturerName);
		
		return convertView;
	}
	
	public boolean contains(Node node) {
		for(int i = 0; i < this.getCount(); ++i) {
			if(this.getItem(i).equals(node)) return true;
		}
		return false;
	}
	
	private static class Holder {
		
		ImageView image;
		TextView nameView;
		TextView manufacturerView;
		
	}
	
}
