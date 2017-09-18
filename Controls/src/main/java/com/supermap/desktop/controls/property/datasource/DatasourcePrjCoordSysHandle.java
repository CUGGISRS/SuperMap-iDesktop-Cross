package com.supermap.desktop.controls.property.datasource;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.controls.property.PrjCoordSysHandle;
import com.supermap.desktop.controls.utilities.WorkspaceTreeManagerUIUtilities;
import com.supermap.desktop.core.Time;
import com.supermap.desktop.core.TimeType;
import com.supermap.desktop.progress.Interface.UpdateProgressCallable;
import com.supermap.desktop.ui.UICommonToolkit;
import com.supermap.desktop.ui.controls.progress.FormProgressTotal;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.concurrent.CancellationException;

public class DatasourcePrjCoordSysHandle extends PrjCoordSysHandle {
	private Datasource datasource;

	public DatasourcePrjCoordSysHandle(Datasource datasource) {
		super(datasource.getPrjCoordSys());
		this.datasource = datasource;
	}

	@Override
	public void change(PrjCoordSys targetPrj) {
		CursorUtilities.setWaitCursor();
		try {
			this.prj = targetPrj;
			this.datasource.setPrjCoordSys(this.prj);

			if (isApplyToDatasets()) {
				boolean isDatasetOpened = false;
				for (int i = 0; i < datasource.getDatasets().getCount(); i++) {
					if (DatasetUtilities.isDatasetOpened(datasource.getDatasets().get(i))) {
						isDatasetOpened = true;
						break;
					}
				}
				if ((isDatasetOpened && JOptionPane.OK_OPTION == UICommonToolkit.showConfirmDialog(MessageFormat.format(
						ControlsProperties.getString("String_DatasetNotClosed"), datasource.getAlias())))
						|| !isDatasetOpened) {
					DatasetUtilities.closeDataset(datasource.getDatasets());
					for (int i = 0; i < this.datasource.getDatasets().getCount(); i++) {
						this.datasource.getDatasets().get(i).setPrjCoordSys(this.prj);
					}
				}
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			CursorUtilities.setDefaultCursor();
		}
	}

	@Override
	public void convert(CoordSysTransMethod method, CoordSysTransParameter parameter, PrjCoordSys targetPrj) {
		this.datasource.setPrjCoordSys(targetPrj);
		if (isApplyToDatasets()) {
			// 关闭数据集
			boolean isDatasetOpened = false;
			for (int i = 0; i < datasource.getDatasets().getCount(); i++) {
				if (DatasetUtilities.isDatasetOpened(datasource.getDatasets().get(i))) {
					isDatasetOpened = true;
				}
			}
			if ((isDatasetOpened && JOptionPane.OK_OPTION == UICommonToolkit.showConfirmDialog(MessageFormat.format(
					ControlsProperties.getString("String_DatasetNotClosed"), datasource.getAlias())))
					|| !isDatasetOpened) {
				DatasetUtilities.closeDataset(datasource.getDatasets());
				FormProgressTotal formProgress = new FormProgressTotal();
				formProgress.doWork(new ConvertProgressCallable(this.datasource, method, parameter, targetPrj));
			}
		}

	}

	private boolean isApplyToDatasets() {
		return UICommonToolkit
				.showConfirmDialogYesNo(MessageFormat.format(ControlsProperties.getString("String_ApplyPrjCoordSys"), this.datasource.getAlias())) == 0;
	}

	private class ConvertProgressCallable extends UpdateProgressCallable {

		private CoordSysTransMethod method;
		private CoordSysTransParameter parameter;
		private PrjCoordSys targetPrj;
		private Datasource datasource;
		private int count;

		public ConvertProgressCallable(Datasource datasource, CoordSysTransMethod method, CoordSysTransParameter parameter, PrjCoordSys targetPrj) {
			this.datasource = datasource;
			this.method = method;
			this.parameter = parameter;
			this.targetPrj = targetPrj;
			this.count = datasource.getDatasets().getCount();
		}

		@Override
		public Boolean call() throws Exception {
			boolean result = true;

			try {
				if (this.datasource == null) {
					return false;
				}

				for (int i = count - 1; i >= 0; i--) {
					Dataset dataset = datasource.getDatasets().get(i);

					if (dataset == null) {
						continue;
					}

					if (dataset.getPrjCoordSys().getType() == PrjCoordSysType.PCS_NON_EARTH) {
						String message = MessageFormat.format(ControlsProperties.getString("String_DatasetPcsNonEarth"), datasource.getAlias(),
								dataset.getName());
						Application.getActiveApplication().getOutput().output(message);
						continue;
					}

					DatasetSteppedListener steppedListener = new DatasetSteppedListener(count - i - 1);
					dataset.addSteppedListener(steppedListener);

					if (dataset instanceof DatasetVector) {
						result = CoordSysTranslator.convert(dataset, this.targetPrj, this.parameter, this.method);
						if (result) {
							Application
									.getActiveApplication()
									.getOutput()
									.output(MessageFormat.format(ControlsProperties.getString("String_CoordSysTrans_VectorSuccess"), dataset.getDatasource()
											.getAlias(), dataset.getName()));
						} else {
							Application
									.getActiveApplication()
									.getOutput()
									.output(MessageFormat.format(ControlsProperties.getString("String_CoordSysTrans_Failed"), dataset.getDatasource()
											.getAlias(), dataset.getName()));
						}
					} else {
						String targetDatasetName = dataset.getDatasource().getDatasets().getAvailableDatasetName(dataset.getName());
						Dataset targetDataset = CoordSysTranslator.convert(dataset, this.targetPrj, dataset.getDatasource(), targetDatasetName, this.parameter,
								this.method);
						result = targetDataset != null;
						if (result) {
							Application
									.getActiveApplication()
									.getOutput()
									.output(MessageFormat.format(ControlsProperties.getString("String_CoordSysTrans_RasterSuccess"), dataset.getDatasource()
											.getAlias(), dataset.getName(), dataset.getDatasource().getAlias(), targetDatasetName));
						} else {
							Application
									.getActiveApplication()
									.getOutput()
									.output(MessageFormat.format(ControlsProperties.getString("String_CoordSysTrans_Failed"), dataset.getDatasource()
											.getAlias(), dataset.getName()));
						}
					}
				}
				WorkspaceTreeManagerUIUtilities.refreshNode(this.datasource);
			} catch (Exception e) {
				result = false;
				Application.getActiveApplication().getOutput().output(e);
			} finally {
				if (this.datasource != null) {
					DatasourcePrjCoordSysHandle.this.prj = this.datasource.getPrjCoordSys();
				}

				if (this.parameter != null) {
					this.parameter.dispose();
				}
			}
			return result;
		}

		private class DatasetSteppedListener implements SteppedListener {

			private int i;

			public DatasetSteppedListener(int i) {
				this.i = i;
			}

			@Override
			public void stepped(SteppedEvent arg0) {
				try {
					int totalPercent = (int) ((100 * this.i + arg0.getPercent()) / count);
					updateProgressTotal(arg0.getPercent(), totalPercent, Time.toString(arg0.getRemainTime(), TimeType.SECOND), arg0.getMessage());
				} catch (CancellationException e) {
					arg0.setCancel(true);
					datasource.getDatasets().get(i).removeSteppedListener(this);
				} finally {
					if (100 == arg0.getPercent()) {
						datasource.getDatasets().get(i).removeSteppedListener(this);
					}
				}

			}

		}
	}
}
