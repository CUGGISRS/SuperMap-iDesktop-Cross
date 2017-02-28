package com.supermap.desktop.process.core;

import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.events.RunningListener;
import com.supermap.desktop.process.parameter.interfaces.IData;
import com.supermap.desktop.process.parameter.interfaces.IParameters;

import javax.swing.event.EventListenerList;
import java.util.Vector;

/**
 * Created by highsad on 2017/1/5.
 */
public abstract class AbstractProcess implements IProcess {

	private EventListenerList listenerList = new EventListenerList();

	protected IProcessGroup parent;
	@Override
	public abstract IParameters getParameters();

	@Override
	public abstract void run();

	@Override
	public abstract String getKey();

	@Override
	public abstract Vector<IData> getInputs();

	@Override
	public abstract Vector<IData> getOutputs();

	@Override
	public void addRunningListener(RunningListener listener) {
		this.listenerList.add(RunningListener.class, listener);
	}

	@Override
	public void removeRunningListener(RunningListener listener) {
		this.listenerList.remove(RunningListener.class, listener);
	}

	protected void fireRunning(RunningEvent e) {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RunningListener.class) {
				((RunningListener) listeners[i + 1]).running(e);
			}
		}
	}

	@Override
	public IProcessGroup getParent() {
		return parent;
	}

	@Override
	public void setParent(ProcessGroup parent) {
		this.parent = parent;
	}
}