package com.supermap.desktop.process.core;

import com.supermap.desktop.Application;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.enums.RunningStatus;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.events.RunningListener;
import com.supermap.desktop.process.events.StatusChangeEvent;
import com.supermap.desktop.process.events.StatusChangeListener;
import com.supermap.desktop.process.loader.DefaultProcessLoader;
import com.supermap.desktop.process.loader.IProcessLoader;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.Inputs;
import com.supermap.desktop.process.parameter.interfaces.datas.Outputs;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;

/**
 * Created by highsad on 2017/1/5.
 * 将进度条提示信息的设置在此基类中实现，不额外设置时，显示默认值-yuanR2017.9.11
 */
public abstract class AbstractProcess implements IProcess {

	protected volatile RunningStatus status = RunningStatus.NORMAL;
	private EventListenerList listenerList = new EventListenerList();
	private Workflow workflow;
	private Inputs inputs = new Inputs(this);
	private Outputs outputs = new Outputs(this);
	private int serialID = 0;

	protected static String RUNNINGMESSAGE = ProcessProperties.getString("String_Running");
	protected static String COMPLETEDMESSAGE = ProcessProperties.getString("String_Completed");
	protected static String FAILEDMESSAGE = ProcessProperties.getString("String_Failed");

	private ArrayList<IReadyChecker<IProcess>> processReadyCheckerList = new ArrayList<>();

	public AbstractProcess() {
		setSerialID(hashCode());
	}

	@Override
	public int getSerialID() {
		return this.serialID;
	}

	@Override
	public void setSerialID(int serialID) {
		this.serialID = serialID;
	}

	@Override
	public Workflow getWorkflow() {
		return this.workflow;
	}

	@Override
	public void setWorkflow(Workflow workflow) {
		if (this.workflow != null && this.workflow != workflow) {
			getParameters().unbindWorkflow(this.workflow);
		}
		Workflow oldWorkflow = this.workflow;
		this.workflow = workflow;
		checkReadyState();
		workflowChanged(oldWorkflow, workflow);
		if (this.workflow != null) {
			getParameters().bindWorkflow(this.workflow);
		}
	}

	protected void workflowChanged(Workflow oldWorkflow, Workflow workflow) {

	}

	@Override
	public abstract IParameters getParameters();

	@Override
	public synchronized final boolean run() {
		boolean isSuccessful = false;

		try {
			// 运行前，必要参数值是否异常判断-yuanR2017.9.8
			if (isReady()) {
				setStatus(RunningStatus.RUNNING);
				fireRunning(new RunningEvent(this, 0, RUNNINGMESSAGE));
				isSuccessful = execute();

				if (isSuccessful) {
					fireRunning(new RunningEvent(this, 100, COMPLETEDMESSAGE));
					setStatus(RunningStatus.COMPLETED);
				} else if (!isCancelled()) {
					fireRunning(new RunningEvent(this, 0, FAILEDMESSAGE));
					setStatus(RunningStatus.EXCEPTION);
				}
			} else {
				Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_ParameterError"));
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
			setStatus(RunningStatus.EXCEPTION);
		}
		return isSuccessful;
	}

	@Override
	public final boolean isReady() {
		if (!isReadyHook()) {
			return false;
		}
		// 参数是否准备就续-yuanR
		if (!getParameters().isReady()) {
			return false;
		}
		if (processReadyCheckerList.size() > 0) {
			for (IReadyChecker<IProcess> iProcessReadyChecker : processReadyCheckerList) {
				if (!iProcessReadyChecker.isReady(this)) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean isReadyHook() {
		return true;
	}

	@Override
	public boolean checkReadyState() {
		if (isReady()) {
			setStatus(RunningStatus.READY);
			return true;
		} else {
			setStatus(RunningStatus.WARNING);
			return false;
		}
	}

	@Override
	public final void cancel() {
		if (this.status != RunningStatus.NORMAL || this.status == RunningStatus.CANCELLED) {
			return;
		}

		setStatus(RunningStatus.CANCELLED);
	}

	@Override
	public final boolean isCancelled() {
		return this.status == RunningStatus.CANCELLED;
	}

	@Override
	public final void setStatus(RunningStatus status) {
		if (this.status != status) {
			RunningStatus oldStatus = this.status;
			this.status = status;
			fireStatusChange(new StatusChangeEvent(this, this.status, oldStatus));
		}
	}

	public abstract boolean execute();

	@Override
	public void reset() {
		RunningStatus oldStatus = this.status;

		RunningStatus currentStatus = isReady() ? RunningStatus.READY : RunningStatus.WARNING;
		if (oldStatus != currentStatus) {
			setStatus(currentStatus);
			fireStatusChange(new StatusChangeEvent(this, getStatus(), oldStatus));
		}
	}

	@Override
	public Class<? extends IProcessLoader> getLoader() {
		return DefaultProcessLoader.class;
	}

	@Override
	public RunningStatus getStatus() {
		return this.status;
	}

	@Override
	public abstract String getKey();

	@Override
	public Inputs getInputs() {
		return this.inputs;
	}


	@Override
	public Outputs getOutputs() {
		return this.outputs;
	}

	@Override
	public void addProcessReadyChecker(IReadyChecker<IProcess> processReadyChecker) {
		if (processReadyChecker != null && !processReadyCheckerList.contains(processReadyChecker)) {
			processReadyCheckerList.add(processReadyChecker);
		}
	}

	@Override
	public void removeProcessReadyChecker(IReadyChecker<IProcess> processReadyChecker) {
		if (processReadyChecker != null) {
			processReadyCheckerList.remove(processReadyChecker);
		}
	}

	@Override
	public void addRunningListener(RunningListener listener) {
		this.listenerList.add(RunningListener.class, listener);
	}

	@Override
	public void removeRunningListener(RunningListener listener) {
		this.listenerList.remove(RunningListener.class, listener);
	}

	@Override
	public void addStatusChangeListener(StatusChangeListener listener) {
		this.listenerList.add(StatusChangeListener.class, listener);
	}

	@Override
	public void removeStatusChangeListener(StatusChangeListener listener) {
		this.listenerList.remove(StatusChangeListener.class, listener);
	}

	protected void fireRunning(RunningEvent e) {
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == RunningListener.class) {
				((RunningListener) listeners[i + 1]).running(e);
			}
		}
	}

	protected void fireStatusChange(StatusChangeEvent e) {
		Object[] listeners = this.listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StatusChangeListener.class) {
				((StatusChangeListener) listeners[i + 1]).statusChange(e);
			}
		}
	}
}
