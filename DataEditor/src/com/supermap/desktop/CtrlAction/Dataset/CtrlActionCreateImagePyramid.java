package com.supermap.desktop.CtrlAction.Dataset;

import java.util.ArrayList;


import com.supermap.data.Dataset;
import com.supermap.data.DatasetGrid;
import com.supermap.data.DatasetImage;
import com.supermap.data.DatasetType;
import com.supermap.data.EngineType;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.progress.callable.CreateImagePyramidCallable;
import com.supermap.desktop.ui.controls.progress.FormProgressTotal;
import com.supermap.desktop.utilties.CursorUtilties;

public class CtrlActionCreateImagePyramid extends CtrlAction {

	public CtrlActionCreateImagePyramid(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		CursorUtilties.setWaitCursor();
		try {
			ArrayList<Dataset> datasets = new ArrayList<Dataset>();
			for (Dataset dataset : Application.getActiveApplication().getActiveDatasets()) {
				if (dataset instanceof DatasetGrid || dataset instanceof DatasetImage) {
					datasets.add(dataset);
				}
			}

			FormProgressTotal formProgressTotal = new FormProgressTotal(ControlsProperties.getString("String_Form_BuildDatasetPyramid"));
			formProgressTotal.doWork(new CreateImagePyramidCallable(datasets.toArray(new Dataset[datasets.size()])));
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			CursorUtilties.setDefaultCursor();
		}
	}

	@Override
	public boolean enable() {
		boolean enable = false;
		for (Dataset dataset : Application.getActiveApplication().getActiveDatasets()) {
			// 影像数据源，支持创建金字塔
			if (dataset.getDatasource().getConnectionInfo().getEngineType() == EngineType.IMAGEPLUGINS) {
				String server = dataset.getDatasource().getConnectionInfo().getServer();
				if (server.toLowerCase().endsWith(".img") || server.toLowerCase().endsWith(".tif") || server.toLowerCase().endsWith(".tiff")) {
					if (dataset.getType() == DatasetType.IMAGE) {
						DatasetImage datasetImage = (DatasetImage) dataset;
						if (!datasetImage.getHasPyramid()) {
							enable = true;
							break;
						}
					} else if (dataset.getType() == DatasetType.GRID) {
						DatasetGrid datasetGrid = (DatasetGrid) dataset;
						if (!datasetGrid.getHasPyramid()) {
							enable = true;
							break;
						}
					}
				}
			} else if (!Application.getActiveApplication().getActiveDatasets()[0].getDatasource().isReadOnly()) {
				if (dataset.getType() == DatasetType.IMAGE) {
					DatasetImage datasetImage = (DatasetImage) dataset;
					if (!datasetImage.getHasPyramid()) {
						enable = true;
						break;
					}
				} else if (dataset.getType() == DatasetType.GRID) {
					DatasetGrid datasetGrid = (DatasetGrid) dataset;
					if (!datasetGrid.getHasPyramid()) {
						enable = true;
						break;
					}
				}
			}
		}

		return enable;
	}
}
