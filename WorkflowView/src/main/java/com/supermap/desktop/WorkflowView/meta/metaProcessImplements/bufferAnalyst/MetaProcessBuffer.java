package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.bufferAnalyst;

import com.supermap.analyst.spatialanalyst.BufferAnalyst;
import com.supermap.analyst.spatialanalyst.BufferAnalystParameter;
import com.supermap.analyst.spatialanalyst.BufferEndType;
import com.supermap.analyst.spatialanalyst.BufferRadiusUnit;
import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.ProcessOutputResultProperties;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.DatasourceConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasetConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.util.EnumParser;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author XiaJT
 * 重构界面-yuanR20174.9.6
 */
public class MetaProcessBuffer extends MetaProcess {
	private final static String INPUT_SOURCE_DATASET = CommonProperties.getString("String_GroupBox_SourceData");
	private final static String OUTPUT_DATASET = "BufferResult";

	private ParameterDatasourceConstrained datasource;
	private ParameterSingleDataset dataset;
	private ParameterEnum parameterBufferRange;
	private ParameterRadioButton radioButtonFlatOrRound;
	private ParameterCheckBox checkBoxBufferLeft;//左缓冲
	private ParameterCheckBox checkBoxBufferRight;//右缓冲
	private ParameterRadioButton radioButtonNumOrField;
	private ParameterTextField parameterTextFieldLeftRadius;
	private ParameterTextField parameterTextFieldRightRadius;
	private ParameterFieldComboBox comboBoxFieldLeft;
	private ParameterFieldComboBox comboBoxFieldRight;
	private ParameterNumber parameterTextFieldSemicircleLineSegment;
	private ParameterCheckBox parameterUnionBuffer;
	private ParameterCheckBox parameterRetainAttribute;
	private ParameterSaveDataset parameterSaveDataset;

	private final static String BUFFER_ROUND = ProcessProperties.getString("String_CheckBox_BufferRound");
	private final static String BUFFER_FLAT = ProcessProperties.getString("String_CheckBox_BufferFlat");

	private final static String VALUE_RELY = ProcessProperties.getString("String_Value_Rely");
	private final static String FIELD_RELY = ProcessProperties.getString("String_Field_Rely");

	public MetaProcessBuffer() {
		initParameters();
		initComponentState();
		initParameterConstraint();
		registerListener();
	}

	private void initParameterConstraint() {
		DatasourceConstraint.getInstance().constrained(parameterSaveDataset, ParameterSaveDataset.DATASOURCE_FIELD_NAME);

		EqualDatasourceConstraint equalDatasourceConstraint = new EqualDatasourceConstraint();
		equalDatasourceConstraint.constrained(datasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalDatasourceConstraint.constrained(dataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasetConstraint equalDatasetConstraint = new EqualDatasetConstraint();
		equalDatasetConstraint.constrained(dataset, ParameterSingleDataset.DATASET_FIELD_NAME);
		equalDatasetConstraint.constrained(comboBoxFieldLeft, ParameterFieldComboBox.DATASET_FIELD_NAME);
		equalDatasetConstraint.constrained(comboBoxFieldRight, ParameterFieldComboBox.DATASET_FIELD_NAME);
	}

	private void initParameters() {

		String[] parameterDataNodes = new String[]{CommonProperties.getString("String_DistanceUnit_Kilometer"),
				CommonProperties.getString("String_DistanceUnit_Meter"),
				CommonProperties.getString("String_DistanceUnit_Decimeter"),
				CommonProperties.getString("String_DistanceUnit_Centimeter"),
				CommonProperties.getString("String_DistanceUnit_Millimeter"),
				CommonProperties.getString("String_DistanceUnit_Foot"),
				CommonProperties.getString("String_DistanceUnit_Inch"),
				CommonProperties.getString("String_DistanceUnit_Mile"),
				CommonProperties.getString("String_DistanceUnit_Yard"),
		};
		String[] values = new String[]{"KiloMeter", "Meter", "DeciMeter", "CentiMeter", "MiliMeter", "Foot", "Inch", "Mile", "Yard"};
		datasource = new ParameterDatasourceConstrained();
		dataset = new ParameterSingleDataset(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION);
		datasource.setDescribe(CommonProperties.getString("String_SourceDatasource"));
		parameterBufferRange = new ParameterEnum(new EnumParser(BufferRadiusUnit.class, values, parameterDataNodes)).setDescribe(ProcessProperties.getString("Label_BufferRadius"));

		radioButtonFlatOrRound = new ParameterRadioButton();
		ParameterDataNode gound = new ParameterDataNode(ProcessProperties.getString("String_CheckBox_BufferRound"), BUFFER_ROUND);
		ParameterDataNode flat = new ParameterDataNode(ProcessProperties.getString("String_CheckBox_BufferFlat"), BUFFER_FLAT);
		radioButtonFlatOrRound.setItems(new ParameterDataNode[]{gound, flat});

		checkBoxBufferLeft = new ParameterCheckBox(ProcessProperties.getString("String_CheckBox_Left"));
		checkBoxBufferRight = new ParameterCheckBox(ProcessProperties.getString("String_CheckBox_Right"));

		radioButtonNumOrField = new ParameterRadioButton();
		ParameterDataNode num = new ParameterDataNode(ProcessProperties.getString("String_Value_Rely"), VALUE_RELY);
		ParameterDataNode field = new ParameterDataNode(ProcessProperties.getString("String_Field_Rely"), FIELD_RELY);
		radioButtonNumOrField.setItems(new ParameterDataNode[]{num, field});

		parameterTextFieldLeftRadius = new ParameterTextField(ProcessProperties.getString("String_leftRadius"));
		parameterTextFieldRightRadius = new ParameterTextField(ProcessProperties.getString("String_rightRadius"));
		comboBoxFieldLeft = new ParameterFieldComboBox(ProcessProperties.getString("String_leftRadius"));
		comboBoxFieldRight = new ParameterFieldComboBox(ProcessProperties.getString("String_rightRadius"));
		parameterUnionBuffer = new ParameterCheckBox(ProcessProperties.getString("String_UnionBufferItem"));
		parameterRetainAttribute = new ParameterCheckBox(ProcessProperties.getString("String_RetainAttribute"));
		parameterTextFieldSemicircleLineSegment = new ParameterNumber(ProcessProperties.getString("Label_SemicircleLineSegment"));
		parameterTextFieldSemicircleLineSegment.setMaxBit(0);
		parameterTextFieldSemicircleLineSegment.setMinValue(4);
		parameterTextFieldSemicircleLineSegment.setMaxValue(200);
		// 设置是否为必要参数-yuanR
		//ParameterLabel labelSplit = new ParameterLabel();
		//labelSplit.setDescribe("-----------------------------------------------------");

		parameterSaveDataset = new ParameterSaveDataset();
		ParameterCombine parameterCombineSourceData = new ParameterCombine();
		parameterCombineSourceData.addParameters(datasource, dataset);
		parameterCombineSourceData.setDescribe(ControlsProperties.getString("String_GroupBox_SourceDataset"));

		ParameterCombine parameterCombineBufferType = new ParameterCombine(ParameterCombine.HORIZONTAL);
		parameterCombineBufferType.addParameters(radioButtonFlatOrRound, checkBoxBufferLeft, checkBoxBufferRight);

		ParameterCombine parameterCombineBufferRadio = new ParameterCombine();
		parameterCombineBufferRadio.addParameters(parameterBufferRange,
				parameterCombineBufferType,
				radioButtonNumOrField,
				parameterTextFieldLeftRadius, parameterTextFieldRightRadius,
				comboBoxFieldLeft, comboBoxFieldRight);
		parameterCombineBufferRadio.setDescribe(ControlsProperties.getString("String_BufferRadius"));

		ParameterCombine parameterCombineParameter = new ParameterCombine();
		parameterCombineParameter.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));
		parameterCombineParameter.addParameters(
				parameterUnionBuffer, parameterRetainAttribute, parameterTextFieldSemicircleLineSegment);

		ParameterCombine parameterCombineResult = new ParameterCombine();
		parameterCombineResult.addParameters(parameterSaveDataset);
		parameterCombineResult.setDescribe(CommonProperties.getString("String_GroupBox_ResultData"));

		parameters.setParameters(
				parameterCombineSourceData,
				parameterCombineBufferRadio,
				parameterCombineParameter,
				parameterCombineResult
		);
		parameterCombineSourceData.setRequisite(true);
		this.parameters.addInputParameters(INPUT_SOURCE_DATASET, DatasetTypes.SIMPLE_VECTOR, parameterCombineSourceData);
		this.parameters.addOutputParameters(OUTPUT_DATASET, ProcessOutputResultProperties.getString("String_BufferResult"), DatasetTypes.REGION, parameterCombineResult);
	}

	private void setComponentEnable() {
		checkBoxBufferLeft.setEnabled(dataset.getSelectedDataset() != null && radioButtonFlatOrRound.isEnabled() && ((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT));
		checkBoxBufferRight.setEnabled(dataset.getSelectedDataset() != null && radioButtonFlatOrRound.isEnabled() && ((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT));
		radioButtonNumOrField.setEnabled(dataset.getSelectedDataset() != null);
		parameterTextFieldLeftRadius.setEnabled((!radioButtonFlatOrRound.isEnabled() ||
				!((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) ||
				(((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) &&
						Boolean.valueOf(checkBoxBufferLeft.getSelectedItem().toString()))) &&
				(((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(VALUE_RELY))
		);

		parameterTextFieldRightRadius.setEnabled(dataset.getSelectedDataset().getType().equals(DatasetType.LINE) &&
				((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(VALUE_RELY) &&
				((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) &&
				Boolean.valueOf(checkBoxBufferRight.getSelectedItem().toString())
		);
		comboBoxFieldLeft.setEnabled((!radioButtonFlatOrRound.isEnabled() ||
				!((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) ||
				(((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) &&
						Boolean.valueOf(checkBoxBufferLeft.getSelectedItem().toString()))) &&
				(((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(FIELD_RELY))
		);
		comboBoxFieldRight.setEnabled(dataset.getSelectedDataset().getType().equals(DatasetType.LINE) &&
				((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(FIELD_RELY) &&
				((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) &&
				Boolean.valueOf(checkBoxBufferRight.getSelectedItem().toString())
		);
	}

	private void initComponentState() {
		parameterBufferRange.setSelectedItem(BufferRadiusUnit.Meter);
		parameterTextFieldLeftRadius.setSelectedItem("10");
		parameterTextFieldRightRadius.setSelectedItem("10");
		parameterTextFieldSemicircleLineSegment.setSelectedItem("100");
		parameterBufferRange.setRequisite(true);
		parameterTextFieldLeftRadius.setRequisite(true);
		parameterTextFieldSemicircleLineSegment.setRequisite(true);

		radioButtonFlatOrRound.setSelectedItem(radioButtonFlatOrRound.getItemAt(0));
		checkBoxBufferLeft.setSelectedItem(true);
		checkBoxBufferRight.setSelectedItem(true);
		radioButtonNumOrField.setSelectedItem(radioButtonNumOrField.getItemAt(0));
		Dataset datasetVector = DatasetUtilities.getDefaultDataset(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION);
		if (datasetVector != null) {
			datasource.setSelectedItem(datasetVector.getDatasource());
			dataset.setSelectedItem(datasetVector);
			comboBoxFieldLeft.setFieldName((DatasetVector) datasetVector);
			comboBoxFieldRight.setFieldName((DatasetVector) datasetVector);
			radioButtonFlatOrRound.setEnabled(dataset.getSelectedDataset().getType().equals(DatasetType.LINE));
			setComponentEnable();
		} else {
			radioButtonFlatOrRound.setEnabled(false);
			checkBoxBufferLeft.setEnabled(false);
			checkBoxBufferRight.setEnabled(false);
			radioButtonNumOrField.setEnabled(false);
		}
		parameterSaveDataset.setSelectedItem("result_buffer");
		FieldType[] fieldType = {FieldType.INT16, FieldType.INT32, FieldType.INT64, FieldType.SINGLE, FieldType.DOUBLE};
		comboBoxFieldLeft.setFieldType(fieldType);
		comboBoxFieldRight.setFieldType(fieldType);
		parameterRetainAttribute.setSelectedItem(true);
	}

	private void registerListener() {
		dataset.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (dataset.getSelectedDataset() != null) {
					radioButtonFlatOrRound.setEnabled(dataset.getSelectedDataset().getType().equals(DatasetType.LINE));
					setComponentEnable();
				}
			}
		});
		radioButtonFlatOrRound.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setComponentEnable();
			}
		});
		checkBoxBufferLeft.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setComponentEnable();
			}
		});
		checkBoxBufferRight.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setComponentEnable();
			}
		});
		radioButtonNumOrField.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				setComponentEnable();
			}
		});
		parameterUnionBuffer.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ParameterCheckBox.PARAMETER_CHECK_BOX_VALUE)) {
					parameterRetainAttribute.setEnabled(!(boolean) evt.getNewValue());
				}
			}
		});
	}

	@Override
	public IParameterPanel getComponent() {
		return parameters.getPanel();
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_BufferAnalyst");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful = false;

		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			// fixme 数据集来源
			DatasetVector datasetVector = null;
			if (this.getParameters().getInputs().getData(INPUT_SOURCE_DATASET) != null
					&& this.getParameters().getInputs().getData(INPUT_SOURCE_DATASET).getValue() instanceof DatasetVector) {
				datasetVector = (DatasetVector) this.getParameters().getInputs().getData(INPUT_SOURCE_DATASET).getValue();
			} else {
				datasetVector = (DatasetVector) dataset.getSelectedItem();
			}

			BufferRadiusUnit radiusUnit = (BufferRadiusUnit) parameterBufferRange.getSelectedData();
			boolean isUnion = "true".equalsIgnoreCase((String) parameterUnionBuffer.getSelectedItem());
			boolean isAttributeRetained = "true".equalsIgnoreCase((String) parameterRetainAttribute.getSelectedItem());
			int semicircleLineSegment = Integer.valueOf(((String) parameterTextFieldSemicircleLineSegment.getSelectedItem()));
			Object radiusLeft = null;
			Object radiusRight = null;
			if (parameterTextFieldLeftRadius.isEnabled() || (comboBoxFieldLeft.isEnabled() && comboBoxFieldLeft.getSelectedItem() != "")) {
				radiusLeft = ((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(VALUE_RELY) ? Integer.valueOf((String) parameterTextFieldLeftRadius.getSelectedItem()) : comboBoxFieldLeft.getFieldName();
			} else if (parameterTextFieldRightRadius.isEnabled() || (comboBoxFieldRight.isEnabled() && comboBoxFieldRight.getSelectedItem() != "")) {
				radiusRight = ((ParameterDataNode) radioButtonNumOrField.getSelectedItem()).getData().equals(VALUE_RELY) ? Integer.valueOf((String) parameterTextFieldRightRadius.getSelectedItem()) : comboBoxFieldRight.getFieldName();
			} else {
				Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_NullRadius_Error"));
				return false;
			}
			if (datasetVector.getType().equals(DatasetType.POINT) && radiusLeft instanceof Integer && (int) radiusLeft < 0) {
				Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_MinusRadius_Error"));
				return false;
			} else if (datasetVector.getType().equals(DatasetType.LINE) && ((radiusLeft instanceof Integer && (Integer) radiusLeft < 0) || (radiusRight instanceof Integer && (Integer) radiusRight < 0))) {
				Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_MinusRadius_Error"));
				return false;
			}

			Datasource resultDatasource = parameterSaveDataset.getResultDatasource();
			String resultName = parameterSaveDataset.getDatasetName();

			DatasetVectorInfo vectorInfo = new DatasetVectorInfo();
			vectorInfo.setName(resultDatasource.getDatasets().getAvailableDatasetName(resultName));
			vectorInfo.setType(DatasetType.REGION);
			DatasetVector result = resultDatasource.getDatasets().create(vectorInfo);
			result.setPrjCoordSys(datasetVector.getPrjCoordSys());

			BufferAnalystParameter parameter = new BufferAnalystParameter();
			parameter.setEndType(((ParameterDataNode) radioButtonFlatOrRound.getSelectedItem()).getData().equals(BUFFER_FLAT) == true ? BufferEndType.FLAT : BufferEndType.ROUND);
			parameter.setRadiusUnit(radiusUnit);
			if (radiusLeft != null)
				parameter.setLeftDistance(radiusLeft);
			if (radiusRight != null)
				parameter.setRightDistance(radiusRight);
			parameter.setSemicircleLineSegment(semicircleLineSegment);

			BufferAnalyst.addSteppedListener(this.steppedListener);
			isSuccessful = BufferAnalyst.createBuffer(datasetVector, result, parameter, isUnion, isAttributeRetained);

			this.getParameters().getOutputs().getData(OUTPUT_DATASET).setValue(result);

			fireRunning(new RunningEvent(this, 100, "finished"));
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			BufferAnalyst.removeSteppedListener(this.steppedListener);
		}


		return isSuccessful;
	}

	@Override
	public String getKey() {
		return MetaKeys.BUFFER;
	}

//	public static void main(String[] args) {
//		new MetaProcessBuffer();
//	}

}
