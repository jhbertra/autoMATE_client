package com.automate.client.managers.discovery;

public class DeviceInfo {

	public final long modelId;
	public final String deviceName;
	public final int maxVersionMajor;
	public final int maxVersionMinor;
	
	public DeviceInfo(long modelId, String deviceName, int maxVersionMajor, int maxVersionMinor) {
		this.modelId = modelId;
		this.deviceName = deviceName;
		this.maxVersionMajor = maxVersionMajor;
		this.maxVersionMinor = maxVersionMinor;
	}
}
