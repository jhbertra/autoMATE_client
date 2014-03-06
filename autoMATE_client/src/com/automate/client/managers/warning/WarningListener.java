package com.automate.client.managers.warning;

import com.automate.client.managers.IListener;
import com.automate.protocol.models.Warning;

public interface WarningListener extends IListener {

	public void onWarningReceived(long nodeId, Warning warning);
	
}
