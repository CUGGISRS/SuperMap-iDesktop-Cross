package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.gridStatisticsAnalyst;

import com.supermap.analyst.spatialanalyst.BasicStatisticsAnalystResult;
import com.supermap.analyst.spatialanalyst.StatisticsAnalyst;
import com.supermap.data.DatasetGrid;
import com.supermap.data.DatasetType;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created By Chens on 2017/8/15 0015
 */
public class MetaProcessBasicStatistics extends MetaProcess {
	private final static String INPUT_DATA = SOURCE_PANEL_DESCRIPTION;

	private ParameterDatasourceConstrained sourceDatasource;
	private ParameterSingleDataset sourceDataset;
	private ParameterTextField textFieldMin;
	private ParameterTextField textFieldMax;
	private ParameterTextField textFieldAvg;
	private ParameterTextField textFieldStd;
	private ParameterTextField textFieldVar;
	private ParameterCheckBox checkBoxShow;
	private ParameterNumber numberGroupCount;
	private ParameterHistogram histogram;

	public MetaProcessBasicStatistics() {
		initParameters();
		initParameterState();
		initParameterConstraint();
		initListener();
	}

	private void initListener() {
		checkBoxShow.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				numberGroupCount.setEnabled(Boolean.parseBoolean(checkBoxShow.getSelectedItem().toString()));
			}
		});
	}

	private void initParameters() {
		sourceDatasource = new ParameterDatasourceConstrained();
		sourceDataset = new ParameterSingleDataset(DatasetType.GRID);
		textFieldMax = new ParameterTextField(ProcessProperties.getString("String_Label_MaxValue"));
		textFieldMin = new ParameterTextField(ProcessProperties.getString("String_Label_MinValue"));
		textFieldAvg = new ParameterTextField(ProcessProperties.getString("String_Label_Mean"));
		textFieldStd = new ParameterTextField(ProcessProperties.getString("String_Label_StandardDeviation"));
		textFieldVar = new ParameterTextField(ProcessProperties.getString("String_Label_Variance"));
		checkBoxShow = new ParameterCheckBox(ProcessProperties.getString("String_CheckBox_ShowHistogram"));
		numberGroupCount = new ParameterNumber(ProcessProperties.getString("String_Label_GroupCount"));
		histogram = new ParameterHistogram();

		ParameterCombine sourceCombine = new ParameterCombine();
		sourceCombine.setDescribe(CommonProperties.getString("String_GroupBox_SourceData"));
		sourceCombine.addParameters(sourceDatasource, sourceDataset);
		ParameterCombine resultCombine = new ParameterCombine();
		resultCombine.setDescribe(ProcessProperties.getString("String_GroupBox_StatisticsResult"));
		resultCombine.addParameters(textFieldMax, textFieldMin, textFieldAvg, textFieldStd, textFieldVar, checkBoxShow, numberGroupCount, histogram);

		parameters.setParameters(sourceCombine, resultCombine);
		this.parameters.addInputParameters(INPUT_DATA, DatasetTypes.GRID, sourceCombine);
	}

	private void initParameterState() {
		DatasetGrid datasetGrid = DatasetUtilities.getDefaultDatasetGrid();
		if (datasetGrid != null) {
			sourceDatasource.setSelectedItem(datasetGrid.getDatasource());
			sourceDataset.setSelectedItem(datasetGrid);
		}
		textFieldMax.setSelectedItem(0);
		textFieldMin.setSelectedItem(0);
		textFieldAvg.setSelectedItem(0);
		textFieldStd.setSelectedItem(0);
		textFieldVar.setSelectedItem(0);
		textFieldMax.setEnabled(false);
		textFieldMin.setEnabled(false);
		textFieldAvg.setEnabled(false);
		textFieldStd.setEnabled(false);
		textFieldVar.setEnabled(false);
		numberGroupCount.setSelectedItem(5);
		numberGroupCount.setMinValue(1);
		numberGroupCount.setMaxBit(-1);
		numberGroupCount.setEnabled(false);
	}

	private void initParameterConstraint() {
		EqualDatasourceConstraint constraintSource = new EqualDatasourceConstraint();
		constraintSource.constrained(sourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		constraintSource.constrained(sourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
	}

	@Override
	public IParameters getParameters() {
		return super.getParameters();
	}

	@Override
	public String getKey() {
		return MetaKeys.BASIC_STATISTIC;
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_Title_BasicStatistics");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful = false;
		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			StatisticsAnalyst.addSteppedListener(steppedListener);
			DatasetGrid src = null;
			if (parameters.getInputs().getData(INPUT_DATA).getValue() != null) {
				src = (DatasetGrid) parameters.getInputs().getData(INPUT_DATA).getValue();
			} else {
				src = (DatasetGrid) sourceDataset.getSelectedItem();
			}
			BasicStatisticsAnalystResult basicStatisticsAnalystResult = StatisticsAnalyst.basicStatistics(src);
			isSuccessful = basicStatisticsAnalystResult != null;
			textFieldMax.setSelectedItem(basicStatisticsAnalystResult.getMax());
			textFieldMin.setSelectedItem(basicStatisticsAnalystResult.getMin());
			textFieldAvg.setSelectedItem(basicStatisticsAnalystResult.getMean());
			double std = basicStatisticsAnalystResult.getStandardDeviation();
			textFieldStd.setSelectedItem(std);
			textFieldVar.setSelectedItem(Math.pow(std, 2));
			if (Boolean.valueOf(checkBoxShow.getSelectedItem().toString())) {
				int groupCount = Integer.parseInt(numberGroupCount.getSelectedItem().toString());
				histogram.setSelectedItem(StatisticsAnalyst.createHistogram(src,groupCount));
			}
			fireRunning(new RunningEvent(this,100,"finished"));
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		}finally {
			StatisticsAnalyst.removeSteppedListener(steppedListener);
		}

		return isSuccessful;
	}
}
