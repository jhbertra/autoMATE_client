package com.automate.client.managers.node;

import java.util.List;

import com.automate.client.managers.IListener;
import com.automate.protocol.models.Node;


public interface NodeListener extends IListener {

	public void onNodeAdded(Node node);
	
	public void onNodesAdded(List<Node> nodes);
	
	public void onNodeRemoved(Node node);
	
	public void onNodeListDownload();

	public void onNodeListDownloadFailed();
	
	public void afterNodeListDownload(List<Node> nodes);
	
}
