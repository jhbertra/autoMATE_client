package com.automate.client.managers;

public abstract class ManagerBase <T extends IListener> extends ListenerBinder<T> implements IManager<T> {

	public ManagerBase(Class<T> listenerClass) {
		super(listenerClass);
	}

	@Override
	public void start() {
		mBindAllowed = true;
		bindSelf();
		setupInitialState();
	}

	@Override
	public void stop() {
		synchronized (mListeners) {
			unbindAll();
			mBindAllowed = false;
		}
		unbindSelf();
		teardown();
	}
	
	protected abstract void unbindSelf();
	
	protected abstract void bindSelf();

	protected abstract void setupInitialState();
	
	protected abstract void teardown();
	
}
