package com.supermap.desktop.WorkflowView.CtrlAction;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.WorkflowView.FormWorkflow;
import com.supermap.desktop.implement.CtrlAction;

/**
 * Created by highsad on 2017/2/28.
 */
public class CtrlActionConnect extends CtrlAction {
	public CtrlActionConnect(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		IForm form = Application.getActiveApplication().getMainFrame().getFormManager().getActiveForm();
		if (form instanceof FormWorkflow) {
			((FormWorkflow) form).getCanvas().getConnector().connecting();
		}
	}

	@Override
	public boolean enable() {
		return Application.getActiveApplication().getMainFrame().getFormManager().getActiveForm() instanceof FormWorkflow;
	}
}
