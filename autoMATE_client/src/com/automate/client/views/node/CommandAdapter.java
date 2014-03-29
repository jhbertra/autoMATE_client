package com.automate.client.views.node;

import java.util.List; 

import com.automate.client.managers.command.Command;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CommandAdapter extends ArrayAdapter<Command> {

	private int mLayoutResource;
	private int mNameResource;
	
	private LayoutInflater mLayoutInflater;
	
	public CommandAdapter(Context context, int resource, int nameResourceId, List<Command> objects) {
		super(context, resource, nameResourceId, objects);
		mLayoutResource = resource;
		mNameResource = nameResourceId;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView name;
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(mLayoutResource, null);
			name = (TextView) convertView.findViewById(mNameResource);
			convertView.setTag(name);
		} else {
			name = (TextView) convertView.getTag();
		}
		
		Command command = getItem(position);
		
		name.setText(command.name);
		
		return convertView;
	}
		
}
