package com.automate.client.managers.status;

import java.util.List;

import com.automate.client.managers.IListener;
import com.automate.protocol.models.Status;

public interface StatusListener extends IListener {

	public void onStatusUpdating(long nodeId);
	
	public void onStatusUpdated(long nodeId, List<Status<?>> statuses);
	
	public void onStatusUpdateCancelled(long nodeId);
	
	public void onStatusUpdateFailed(long nodeId);
	
}
