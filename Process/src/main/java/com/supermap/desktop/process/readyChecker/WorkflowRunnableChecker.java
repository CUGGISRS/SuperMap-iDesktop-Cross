package com.supermap.desktop.process.readyChecker;

import com.supermap.desktop.Application;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.core.IReadyChecker;
import com.supermap.desktop.process.core.ReadyEvent;
import com.supermap.desktop.process.core.Workflow;
import com.supermap.desktop.process.enums.RunningStatus;

import java.text.MessageFormat;
import java.util.Vector;

/**
 * @author XiaJT
 */
public class WorkflowRunnableChecker<E extends Workflow> implements IReadyChecker<E> {
	@Override
	public boolean isReady(ReadyEvent<E> readyEvent) {
		Workflow workflow = readyEvent.getSourceData();
		Vector<IProcess> processes = workflow.getProcesses();
		Vector<IProcess> leadingProcesses = workflow.getLeadingProcesses();
		Vector<IProcess> freeProcesses = workflow.getFreeProcesses();
		processes.removeAll(freeProcesses);
		processes.removeAll(leadingProcesses);

		int size = -1;
		while (processes.size() != size && processes.size() != 0) {
			size = processes.size();
			for (int i = processes.size() - 1; i >= 0; i--) {
				Vector<IProcess> fromProcesses = workflow.getFromProcesses(processes.get(i));
				boolean isFormProcessExist = false;
				for (IProcess fromProcess : fromProcesses) {
					if (processes.contains(fromProcess)) {
						isFormProcessExist = true;
						break;
					}
				}
				if (!isFormProcessExist) {
					processes.remove(processes.get(i));
				}
			}
		}
		if (processes.size() > 0) {
			StringBuilder stringBuffer = new StringBuilder();
			for (IProcess process : processes) {
				process.setStatus(RunningStatus.WARNING);
				stringBuffer.append(", ").append("\"").append(process.getTitle()).append("\"");
			}
			String processTitles = stringBuffer.toString().substring(1);
			Application.getActiveApplication().getOutput().output(MessageFormat.format(ProcessProperties.getString("String_ProcessHasCircle"), processTitles));
			return false;
		}
		return true;
	}
}
