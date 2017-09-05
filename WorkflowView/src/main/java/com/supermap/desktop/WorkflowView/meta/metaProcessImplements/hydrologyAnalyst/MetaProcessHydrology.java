package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.hydrologyAnalyst;

import com.supermap.analyst.terrainanalyst.HydrologyAnalyst;
import com.supermap.data.Dataset;
import com.supermap.data.DatasetGrid;
import com.supermap.data.DatasetType;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.metaProcessImplements.MetaProcessGridAnalyst;
import com.supermap.desktop.process.constraint.ipls.DatasourceConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.ParameterCombine;
import com.supermap.desktop.process.parameter.ipls.ParameterDatasource;
import com.supermap.desktop.process.parameter.ipls.ParameterDatasourceConstrained;
import com.supermap.desktop.process.parameter.ipls.ParameterSaveDataset;
import com.supermap.desktop.process.parameter.ipls.ParameterSingleDataset;
import com.supermap.desktop.utilities.DatasetUtilities;

/**
 * Created By Chens on 2017/8/29 0029
 */
public abstract class MetaProcessHydrology extends MetaProcessGridAnalyst {
	protected String INPUT_DATA = SOURCE_PANEL_DESCRIPTION;;
	protected String OUTPUT_DATA;

	protected ParameterDatasourceConstrained sourceDatasource;
	protected ParameterSingleDataset sourceDataset;
	protected ParameterSaveDataset resultDataset;
	protected ParameterCombine sourceCombine;
	protected ParameterCombine resultCombine;

	public MetaProcessHydrology() {
		initField();
		initParameters();
		initParameterConstraint();
		initParametersState();
	}

	private void initParameters() {
		sourceDatasource = new ParameterDatasourceConstrained();
		sourceDataset = new ParameterSingleDataset(DatasetType.GRID);
		resultDataset = new ParameterSaveDataset();

		sourceCombine = new ParameterCombine();
		sourceCombine.setDescribe(INPUT_DATA);
		sourceCombine.addParameters(sourceDatasource, sourceDataset);
		resultCombine = new ParameterCombine();
		resultCombine.setDescribe(RESULT_PANEL_DESCRIPTION);
		resultCombine.addParameters(resultDataset);

		initParaComponent();
		parameters.addInputParameters(INPUT_DATA, DatasetTypes.GRID, sourceCombine);
		parameters.addOutputParameters(OUTPUT_DATA, getOutputText(),getOutputType(),resultCombine);
	}

	protected Type getOutputType() {
		return DatasetTypes.GRID;
	}

	private void initParameterConstraint() {
		EqualDatasourceConstraint constraintSource = new EqualDatasourceConstraint();
		constraintSource.constrained(sourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		constraintSource.constrained(sourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
		DatasourceConstraint.getInstance().constrained(resultDataset, ParameterSaveDataset.DATASOURCE_FIELD_NAME);
	}

	private void initParametersState() {
		DatasetGrid datasetGrid = DatasetUtilities.getDefaultDatasetGrid();
		if (datasetGrid != null) {
			sourceDatasource.setSelectedItem(datasetGrid.getDatasource());
			sourceDataset.setSelectedItem(datasetGrid);
		}
		resultDataset.setSelectedItem(getResultName());
	}

	protected void initField() {

	}

	protected abstract void initParaComponent();

	protected abstract String getResultName();

	protected abstract String getOutputText();

	@Override
	public boolean childExecute() {
		boolean isSuccessful = false;
		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			DatasetGrid src = null;
			if (parameters.getInputs().getData(INPUT_DATA).getValue() != null) {
				src = (DatasetGrid) parameters.getInputs().getData(INPUT_DATA).getValue();
			} else {
				src = (DatasetGrid) sourceDataset.getSelectedItem();
			}
			HydrologyAnalyst.addSteppedListener(steppedListener);
			Dataset result = doWork(src);
			isSuccessful = result != null;
			this.getParameters().getOutputs().getData(OUTPUT_DATA).setValue(result);
			fireRunning(new RunningEvent(this,100,"finished"));
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}finally {
			HydrologyAnalyst.removeSteppedListener(steppedListener);
		}
		return isSuccessful;
	}

	protected abstract Dataset doWork(DatasetGrid src);
}
