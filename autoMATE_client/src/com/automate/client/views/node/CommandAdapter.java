package com.automate.client.views.node;

import java.util.ArrayList;
import java.util.List;  

import com.automate.client.managers.command.Command;
import com.automate.protocol.models.Status;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class CommandAdapter extends ArrayAdapter<Command> {

	private int mLayoutResource;
	private int mNameResource;

	private LayoutInflater mLayoutInflater;
	private List<Command> commands;

	private ConditionFilter mFilter = new ConditionFilter();

	public CommandAdapter(Context context, int resource, int nameResourceId, List<Command> objects) {
		super(context, resource, nameResourceId);
		mLayoutResource = resource;
		mNameResource = nameResourceId;
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.commands = objects;
		for(Command command : commands) {
			add(command);
		}
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

	public void setStatuses(List<Status<?>> statuses) {
		mFilter.setStatuses(statuses);
	}
	
	/* (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getFilter()
	 */
	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private class ConditionFilter extends Filter {

		private List<Status<?>> statuses;

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			synchronized (commands) {
				FilterResults results = new FilterResults();
				List<Command> filtered = new ArrayList<Command>();
				for(Command command : commands) {
					if(command.condition.conditionsMet(statuses)) {
						filtered.add(command);
					}
				}
				results.count = filtered.size();
				results.values = filtered;
				return results;
			}
		}

		public void setStatuses(List<Status<?>> statuses) {
			this.statuses = statuses;
			filter("");
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			synchronized (commands) {
				List<Command> commands = (List<Command>) results.values;
				notifyDataSetInvalidated();
				clear();
				for(Command command : commands) {
					add(command);
				}
			}
		}

	}

}
