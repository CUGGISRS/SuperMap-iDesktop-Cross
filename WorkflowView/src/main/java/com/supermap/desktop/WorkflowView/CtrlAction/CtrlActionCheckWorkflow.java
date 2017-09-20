package com.supermap.desktop.WorkflowView.CtrlAction;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.WorkflowView.FormWorkflow;
import com.supermap.desktop.WorkflowView.WorkflowViewProperties;
import com.supermap.desktop.implement.CtrlAction;

/**
 * @author XiaJT
 */
public class CtrlActionCheckWorkflow extends CtrlAction {
	public CtrlActionCheckWorkflow(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	protected void run() {
		IForm activeForm = Application.getActiveApplication().getActiveForm();
		if (activeForm instanceof FormWorkflow) {
			if (((FormWorkflow) activeForm).isEditable() && ((FormWorkflow) activeForm).getWorkflow().isReady()) {
				Application.getActiveApplication().getOutput().output(WorkflowViewProperties.getString("String_WorkflowNoError"));
			}
		}
	}

	@Override
	public boolean enable() {
		IForm activeForm = Application.getActiveApplication().getActiveForm();
		return activeForm instanceof FormWorkflow;
	}
}
