package com.automate.client.views.node;

import java.util.List;

import com.automate.protocol.models.Status;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StatusAdapter extends ArrayAdapter<Status<?>> {

	private int mLayoutResource;
	private int mNameResource;
	private int mValueResource;
	
	private LayoutInflater mLayoutInflater;
	
	public StatusAdapter(Context context, int resource, int nameResourceId, int valueResourceId, List<Status<?>> objects) {
		super(context, resource, nameResourceId, objects);
		mLayoutResource = resource;
		mNameResource = nameResourceId;
		mValueResource = valueResourceId;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	public void setStatuses(List<Status<?>> statuses) {
		this.clear();
		this.addAll(statuses);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;
		if(convertView == null) {
			convertView = mLayoutInflater.inflate(mLayoutResource, null);
			holder = new Holder();
			holder.name = (TextView) convertView.findViewById(mNameResource);
			holder.value = (TextView) convertView.findViewById(mValueResource);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		
		Status<?> status = getItem(position);
		
		holder.name.setText(status.name);
		holder.value.setText(status.value.toString());
		
		return convertView;
	}
	
	private class Holder {
		TextView name;
		TextView value;
	}
		
}
