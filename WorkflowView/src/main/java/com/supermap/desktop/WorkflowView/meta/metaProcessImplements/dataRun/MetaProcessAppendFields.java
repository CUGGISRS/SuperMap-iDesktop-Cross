package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.dataRun;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetVector;
import com.supermap.data.Datasource;
import com.supermap.data.SteppedEvent;
import com.supermap.data.SteppedListener;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasetConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.ipls.ParameterCombine;
import com.supermap.desktop.process.parameter.ipls.ParameterDatasource;
import com.supermap.desktop.process.parameter.ipls.ParameterFieldComboBox;
import com.supermap.desktop.process.parameter.ipls.ParameterMultiFieldSet;
import com.supermap.desktop.process.parameter.ipls.ParameterSingleDataset;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetTypeUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;
import com.supermap.desktop.utilities.DatasourceUtilities;
import com.supermap.desktop.utilities.TabularUtilities;

import java.text.MessageFormat;

/**
 * Created by xie on 2017/8/5.
 * 追加列
 */
public class MetaProcessAppendFields extends MetaProcess {
	private final String INPUT_DATA = CommonProperties.getString("String_ColumnHeader_SourceData");
	private final String OUTPUT_DATA = CommonProperties.getString("String_ColumnHeader_TargetData");
	private ParameterCombine sourceDataCombine;
	private ParameterDatasource sourceDatasource;
	private ParameterSingleDataset sourceDataset;
	private ParameterFieldComboBox sourceLinkedField;
	private ParameterCombine targetDataCombine;
	private ParameterDatasource targetDatasource;
	private ParameterSingleDataset targetDataset;
	private ParameterFieldComboBox targetLinkedField;
	private ParameterMultiFieldSet multiFieldSet;

	public MetaProcessAppendFields() {
		initParameters();
		initConstrant();
	}

	private void initConstrant() {
		EqualDatasourceConstraint sourceDatasourceConstraint = new EqualDatasourceConstraint();
		sourceDatasourceConstraint.constrained(sourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		sourceDatasourceConstraint.constrained(sourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasourceConstraint targetDatasourceConstraint = new EqualDatasourceConstraint();
		targetDatasourceConstraint.constrained(targetDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		targetDatasourceConstraint.constrained(targetDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasetConstraint datasetConstraint1 = new EqualDatasetConstraint();
		datasetConstraint1.constrained(sourceDataset, ParameterSingleDataset.DATASET_FIELD_NAME);
		datasetConstraint1.constrained(multiFieldSet, ParameterMultiFieldSet.SOURCE_DATASET);

	}

	private void initParameters() {
		steppedListener = new SteppedListener() {
			@Override
			public void stepped(SteppedEvent steppedEvent) {
				fireRunning(new RunningEvent(MetaProcessAppendFields.this, steppedEvent.getPercent(), steppedEvent.getMessage()));
			}
		};
		Datasource datasource = DatasourceUtilities.getDefaultResultDatasource();
		Dataset dataset = DatasetUtilities.getDefaultDataset(DatasetTypeUtilities.getDatasetTypeVector());

		this.targetDatasource = new ParameterDatasource();
		this.targetDatasource.setReadOnlyNeeded(false);
		this.targetDataset = new ParameterSingleDataset();
		this.targetDataset.setDatasource(datasource);
		this.targetDataset.setDatasetTypes(DatasetTypeUtilities.getDatasetTypeVector());
		this.targetLinkedField = new ParameterFieldComboBox();
		this.targetLinkedField.setDescribe(ProcessProperties.getString("String_ConnectionField"));
		this.targetDataCombine = new ParameterCombine();
		this.targetDataCombine.setDescribe(OUTPUT_DATA);
		this.targetDataCombine.addParameters(targetDatasource, targetDataset, targetLinkedField);

		this.sourceDatasource = new ParameterDatasource();
		this.sourceDataset = new ParameterSingleDataset();
		this.sourceDataset.setDatasource(datasource);
		this.sourceDataset.setDatasetTypes(DatasetTypeUtilities.getDatasetTypeVector());
		this.sourceLinkedField = new ParameterFieldComboBox();
		this.sourceLinkedField.setDescribe(ProcessProperties.getString("String_ConnectionField"));
		this.sourceDataCombine = new ParameterCombine();
		this.sourceDataCombine.setDescribe(INPUT_DATA);
		this.sourceDataCombine.addParameters(sourceDatasource, sourceDataset, sourceLinkedField);
		this.multiFieldSet = new ParameterMultiFieldSet();
		if (null != dataset) {
			this.sourceDataset.setSelectedItem(dataset);
			this.targetDataset.setSelectedItem(dataset);
			this.sourceLinkedField.setFieldName((DatasetVector) dataset);
			this.targetLinkedField.setFieldName((DatasetVector) dataset);
			this.multiFieldSet.setDataset((DatasetVector) dataset);
		}

		this.parameters.addInputParameters(INPUT_DATA, DatasetTypes.VECTOR, sourceDataCombine);
		this.parameters.addOutputParameters(OUTPUT_DATA, DatasetTypes.VECTOR, targetDataCombine);
		this.parameters.setParameters(targetDataCombine, sourceDataCombine, multiFieldSet);
	}


	@Override
	public boolean execute() {
		boolean result = false;
		fireRunning(new RunningEvent(this, 0, "start"));
		DatasetVector datasetVector = (DatasetVector) targetDataset.getSelectedDataset();
		String sourceLinked = targetLinkedField.getFieldName();
		String targetLineed = sourceLinkedField.getFieldName();
		DatasetVector targetDatasetVector = (DatasetVector) sourceDataset.getSelectedDataset();
		if (null != multiFieldSet.getDatasetFieldInfo()) {
			String[] sourceFields = multiFieldSet.getDatasetFieldInfo().getSourceFields();
			String[] targetFields = multiFieldSet.getDatasetFieldInfo().getTargetFields();
			datasetVector.addSteppedListener(this.steppedListener);
			result = datasetVector.appendFields(targetDatasetVector, sourceLinked, targetLineed, sourceFields, targetFields);
			if (result) {
				fireRunning(new RunningEvent(this, 100, "success"));
				Application.getActiveApplication().getOutput().output(MessageFormat.format(ProcessProperties.getString("String_AppendFieldsSuccess"), targetDatasetVector.getName(), datasetVector.getName()));
				TabularUtilities.refreshTabularDatas(datasetVector);
			} else {
				fireRunning(new RunningEvent(this, 100, "failed"));
				Application.getActiveApplication().getOutput().output(MessageFormat.format(ProcessProperties.getString("String_AppendFieldsFailed"), targetDatasetVector.getName(), datasetVector.getName()));
			}
		} else {
			Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_AppendFieldsIsNull"));
		}
		targetDatasetVector.removeSteppedListener(this.steppedListener);
		return result;
	}

	@Override
	public String getKey() {
		return MetaKeys.APPEND_FIELDS;
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_AppendFields");
	}

	@Override
	public IParameters getParameters() {
		return this.parameters;
	}

	@Override
	public IParameterPanel getComponent() {
		return this.parameters.getPanel();
	}
}
