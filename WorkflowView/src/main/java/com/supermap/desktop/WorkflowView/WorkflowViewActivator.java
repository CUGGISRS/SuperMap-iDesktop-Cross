package com.supermap.desktop.WorkflowView;

import com.supermap.desktop.Application;
import com.supermap.desktop.CommonToolkit;
import com.supermap.desktop.Interface.IDataEntry;
import com.supermap.desktop.Interface.IFormManager;
import com.supermap.desktop.Interface.IFormWorkflow;
import com.supermap.desktop.Interface.IWorkflow;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.event.NewWindowEvent;
import com.supermap.desktop.event.NewWindowListener;
import com.supermap.desktop.event.WorkflowInitListener;
import com.supermap.desktop.process.core.Workflow;
import com.supermap.desktop.utilities.CursorUtilities;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;

import java.util.ArrayList;

/**
 * Created by highsad on 2017/7/26.
 */
public class WorkflowViewActivator implements BundleActivator {
	private static final String PROCESSTREE_CLASSNAME = "com.supermap.desktop.WorkflowView.ProcessManagerPanel";
	private static final String PARAMETERMANAGER_CLASSNAME = "com.supermap.desktop.WorkflowView.ParameterManager";

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		Application.getActiveApplication().getPluginManager().addPlugin("SuperMap.Desktop.WorkflowView", bundleContext.getBundle());
		System.out.println("Hello SuperMap === WorkflowView!!");

		Application.getActiveApplication().setWorkflowInitListener(new WorkflowInitListener() {
			@Override
			public IWorkflow init(Element element) {
				String name = element.getAttribute("name");
				Workflow workflow = new Workflow(name);
				workflow.serializeFrom(element.getAttribute("value"));
				return workflow;
			}
		});

		CommonToolkit.FormWrap.addNewWindowListener(new NewWindowListener() {
			@Override
			public void newWindow(NewWindowEvent evt) {
				newWindowEvent(evt);
			}
		});
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		System.out.println("Goodbye SuperMap === WorkflowView!!");
	}

	private void newWindowEvent(NewWindowEvent evt) {
		WindowType type = evt.getNewWindowType();
		if (type == WindowType.WORKFLOW) {
			IFormWorkflow formProcess = showProcess(evt.getNewWindowName());
			evt.setNewWindow(formProcess);
		}
	}

	private IFormWorkflow showProcess(String newWindowName) {
		FormWorkflow formWorkflow = null;

		try {
			IFormManager formManager = Application.getActiveApplication().getMainFrame().getFormManager();
			for (int i = 0; i < formManager.getCount(); i++) {
				if (formManager.get(i).getWindowType() == WindowType.WORKFLOW && formManager.get(i).getText().equals(newWindowName)) {
					if (formManager.getActiveForm() != formManager.get(i)) {
						formManager.setActiveForm(formManager.get(i));
					}
					return null;
				}
			}

			CursorUtilities.setWaitCursor();
			ArrayList<IDataEntry<String>> workflows = Application.getActiveApplication().getWorkflowEntries();
			for (IDataEntry<String> workflow : workflows) {
				if (workflow.getKey().equals(newWindowName)) {
					formWorkflow = FormWorkflow.serializeFrom(workflow.getValue());
					formWorkflow.setText(workflow.getKey());
					break;
				}
			}
			if (formWorkflow == null) {
				formWorkflow = new FormWorkflow(newWindowName);
			}
			formManager.showChildForm(formWorkflow);
			Application.getActiveApplication().getMainFrame().getDockbarManager().get(Class.forName(PROCESSTREE_CLASSNAME)).setVisible(true);
			Application.getActiveApplication().getMainFrame().getDockbarManager().get(Class.forName(PARAMETERMANAGER_CLASSNAME)).setVisible(true);
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			CursorUtilities.setDefaultCursor();
		}

		return formWorkflow;
	}

}
