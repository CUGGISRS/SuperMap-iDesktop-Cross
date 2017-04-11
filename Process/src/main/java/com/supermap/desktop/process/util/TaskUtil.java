package com.supermap.desktop.process.util;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IDockbar;
import com.supermap.desktop.process.ParameterManager;
import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.core.NodeException;
import com.supermap.desktop.process.core.NodeMatrix;
import com.supermap.desktop.process.meta.MetaProcess;
import com.supermap.desktop.process.tasks.ProcessTask;
import com.supermap.desktop.process.tasks.TaskStore;
import com.supermap.desktop.process.tasks.TasksManagerContainer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xie on 2017/2/28.
 */
public class TaskUtil {
	/**
	 * Utilities class,no public construction method
	 */
	private TaskUtil() {

	}

	private static final String TASKMANAGER = "com.supermap.desktop.process.tasks.TasksManagerContainer";
	private static final String PARAMETERMANAGER = "com.supermap.desktop.process.ParameterManager";

	public static ParameterManager getParameterManager(boolean isActive) {
		ParameterManager parameterManager = null;
		IDockbar dockbarPropertyContainer = null;
		try {
			dockbarPropertyContainer = Application.getActiveApplication().getMainFrame().getDockbarManager().get(Class.forName(PARAMETERMANAGER));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (dockbarPropertyContainer != null) {
			parameterManager = (ParameterManager) dockbarPropertyContainer.getInnerComponent();
		}
		if (isActive && dockbarPropertyContainer != null) {
			dockbarPropertyContainer.setVisible(true);
			dockbarPropertyContainer.active();
		}
		return parameterManager;
	}


	public static TasksManagerContainer getManagerContainer(boolean isActive) {
		TasksManagerContainer fileManagerContainer = null;
		IDockbar dockbarPropertyContainer = null;
		try {
			dockbarPropertyContainer = Application.getActiveApplication().getMainFrame().getDockbarManager().get(Class.forName(TASKMANAGER));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (dockbarPropertyContainer != null) {
			fileManagerContainer = (TasksManagerContainer) dockbarPropertyContainer.getInnerComponent();
		}
		if (isActive && dockbarPropertyContainer != null) {
			dockbarPropertyContainer.setVisible(true);
			dockbarPropertyContainer.active();
		}
		return fileManagerContainer;
	}

	/**
	 * Use ExecutorService to manage all task thread,
	 * If task's prev tasks execute finished,execute task;
	 *
	 * @param nodeMatrix
	 * @return
	 */
	public static void excuteTasks(final NodeMatrix nodeMatrix) {
		final CopyOnWriteArrayList<Object> processes = nodeMatrix.getAllNodes();
		ExecutorService eService = Executors.newCachedThreadPool();
		final Lock lock = new ReentrantLock();
		int size = processes.size();
		for (int i = 0; i < size; i++) {
			if (processes.get(i) instanceof IProcess) {
				final IProcess nowProcess = ((IProcess) processes.get(i));
				Thread thread = new Thread() {
					@Override
					public void run() {
						lock.lock();
						CopyOnWriteArrayList<Object> preNodes = null;
						try {
							preNodes = nodeMatrix.getPreNodes(nowProcess);
						} catch (NodeException e) {
							Application.getActiveApplication().getOutput().output(e);
						}
						boolean allPreTasksFinished = true;
						int preNodesSize = preNodes.size();
						if (preNodesSize > 0) {
							for (int j = 0; j < preNodesSize; j++) {
								if (((MetaProcess) preNodes.get(j)).isFinished()) {
									allPreTasksFinished = false;
									break;
								}
							}
						}
						if (allPreTasksFinished || preNodes.size() == 0) {
							TaskStore.getTask(nowProcess).doWork();
						}
						lock.unlock();
					}
				};
				eService.execute(thread);
			}
		}
		eService.shutdown();
	}

	public static ProcessTask getTask(IProcess process){
		ProcessTask task;
		if (null!= TaskStore.getTask(process)){
			task = (ProcessTask) TaskStore.getTask(process);
		}else{
			task = new ProcessTask(process);
			TaskStore.addTask(task);
		}
		return task;
	}

	public static void executeMatrix(NodeMatrix matrix) {
		List startNodes = matrix.getAllStartNodes();

		if (startNodes.size() > 0) {

		}
	}
}
