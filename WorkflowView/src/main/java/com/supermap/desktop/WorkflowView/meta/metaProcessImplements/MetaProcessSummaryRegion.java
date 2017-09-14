package com.supermap.desktop.WorkflowView.meta.metaProcessImplements;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetType;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.ProcessOutputResultProperties;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.lbs.Interface.IServerService;
import com.supermap.desktop.lbs.params.CommonSettingCombine;
import com.supermap.desktop.lbs.params.JobResultResponse;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.messageBus.NewMessageBus;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.parameters.ParameterPanels.DefaultOpenServerMap;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Created by caolp on 2017-08-05.
 * 区域汇总分析
 */
public class MetaProcessSummaryRegion extends MetaProcess {
	private ParameterIServerLogin parameterIServerLogin = new ParameterIServerLogin();
	ParameterInputDataType parameterInputDataType = new ParameterInputDataType();
	private ParameterComboBox parameterSummaryType = new ParameterComboBox(ProcessProperties.getString("String_summaryType"));
	private ParameterComboBox parameterMeshType = new ParameterComboBox(ProcessProperties.getString("String_MeshType"));
	private ParameterDefaultValueTextField parameterBounds = new ParameterDefaultValueTextField(ProcessProperties.getString("String_AnalystBounds"));
	private ParameterCheckBox parameterStandardFields = new ParameterCheckBox(ProcessProperties.getString("String_standardSummaryFields"));
	private ParameterCheckBox parameterWeightedFields = new ParameterCheckBox(ProcessProperties.getString("String_weightedSummaryFields"));
	private ParameterDefaultValueTextField parameterStatisticMode = new ParameterDefaultValueTextField(ProcessProperties.getString("String_StaticModel"));
	private ParameterTextField parameterFeildName = new ParameterTextField(ProcessProperties.getString("String_FeildName"));
	private ParameterDefaultValueTextField parameterStatisticMode1 = new ParameterDefaultValueTextField(ProcessProperties.getString("String_StaticModel"));
	private ParameterTextField parameterFeildName1 = new ParameterTextField(ProcessProperties.getString("String_FeildName"));
	private ParameterDefaultValueTextField parameterMeshSize = new ParameterDefaultValueTextField(ProcessProperties.getString("String_MeshSize"));
	private ParameterComboBox parameterMeshSizeUnit = new ParameterComboBox(ProcessProperties.getString("String_MeshSizeUnit"));
	private ParameterCheckBox parametersumShape = new ParameterCheckBox(ProcessProperties.getString("String_SumShape"));
	private ParameterBigDatasourceDatasource parameterBigDatasourceDatasource = new ParameterBigDatasourceDatasource();
	private ParameterSingleDataset parameterSingleDataset = new ParameterSingleDataset(DatasetType.LINE, DatasetType.REGION);
	private ParameterDefaultValueTextField parameterDataBaseName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_DataBaseName"));
	private ParameterDefaultValueTextField parameterTextFieldAddress = new ParameterDefaultValueTextField(CoreProperties.getString("String_Server"));
	private ParameterDefaultValueTextField parameterTextFieldUserName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_UserName"));
	private ParameterPassword parameterTextFieldPassword = new ParameterPassword(ProcessProperties.getString("String_PassWord"));

	public MetaProcessSummaryRegion() {
		initComponents();
		initComponentState();
		initComponentLayout();
		initConstraint();
	}

	private void initComponents() {
		parameterTextFieldAddress.setRequisite(true);
		parameterTextFieldAddress.setDefaultWarningValue("192.168.15.248");
		parameterDataBaseName.setRequisite(true);
		parameterDataBaseName.setDefaultWarningValue("supermap");
		parameterTextFieldUserName.setRequisite(true);
		parameterTextFieldUserName.setDefaultWarningValue("postgres");
		parameterTextFieldPassword.setRequisite(true);
		parameterTextFieldPassword.setSelectedItem("supermap");
		parameterSummaryType.setRequisite(true);
		parameterSummaryType.setItems(new ParameterDataNode(ProcessProperties.getString("String_summaryMesh"), "SUMMARYMESH"), new ParameterDataNode(ProcessProperties.getString("String_summaryRegion"), "SUMMARYREGION"));
		parameterMeshType.setRequisite(true);
		parameterMeshType.setItems(new ParameterDataNode(ProcessProperties.getString("String_QuadrilateralMesh"), "0"), new ParameterDataNode(ProcessProperties.getString("String_HexagonalMesh"), "1"));
		parameterBounds.setDefaultWarningValue("-74.050,40.650,-73.850,40.850");
		parameterStatisticMode.setToolTip(ProcessProperties.getString("String_StatisticsModeTip"));
		parameterStatisticMode1.setToolTip(ProcessProperties.getString("String_StatisticsModeTip"));
		parameterMeshSize.setDefaultWarningValue("100");
		parameterMeshSize.setRequisite(true);
		parameterMeshSizeUnit.setItems(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Meter"), "Meter"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Kilometer"), "Kilometer"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Yard"), "Yard"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Foot"), "Foot"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Mile"), "Mile")
		);
		parameterStandardFields.setSelectedItem(false);
		parameterWeightedFields.setSelectedItem(false);
		parametersumShape.setSelectedItem(true);
		parameterBigDatasourceDatasource.setRequisite(true);
		parameterBigDatasourceDatasource.setDescribe(ControlsProperties.getString("String_Label_ResultDatasource"));
		parameterSingleDataset.setRequisite(true);
		parameterSingleDataset.setDescribe(ProcessProperties.getString("String_RegionDataset"));
	}

	private void initComponentState() {
		parameterInputDataType.parameterDataInputWay.removeAllItems();
		parameterInputDataType.parameterDataInputWay.setItems(new ParameterDataNode(ProcessProperties.getString("String_UDBFile"), "1"), new ParameterDataNode(ProcessProperties.getString("String_PGDataBase"), "2"));
		parameterInputDataType.parameterSwitch.switchParameter("1");
		parameterInputDataType.setSupportDatasetType(DatasetType.LINE, DatasetType.REGION);
		Dataset defaultBigDataStoreDataset = DatasetUtilities.getDefaultBigDataStoreDataset();
		if (defaultBigDataStoreDataset != null && (DatasetType.LINE == defaultBigDataStoreDataset.getType() || DatasetType.REGION == defaultBigDataStoreDataset.getType())) {
			parameterBigDatasourceDatasource.setSelectedItem(defaultBigDataStoreDataset.getDatasource());
			parameterSingleDataset.setSelectedItem(defaultBigDataStoreDataset);
		}
	}

	private void initComponentLayout() {
		final ParameterCombine parameterCombineSetting = new ParameterCombine();
		parameterCombineSetting.setDescribe(ProcessProperties.getString("String_AnalystSet"));
		final ParameterCombine parameterCombine = new ParameterCombine();
		parameterCombine.addParameters(parameterMeshType, parameterBounds, parameterMeshSize, parameterMeshSizeUnit);
		final ParameterCombine parameterCombine1 = new ParameterCombine();
		parameterCombine1.addParameters(parameterTextFieldAddress,
				parameterDataBaseName,
				parameterTextFieldUserName,
				parameterTextFieldPassword,
				parameterBigDatasourceDatasource,
				parameterSingleDataset,
				parameterBounds);
		final ParameterSwitch parameterSwitch = new ParameterSwitch();
		parameterSwitch.add("0", parameterCombine);
		parameterSwitch.add("1", parameterCombine1);
		parameterSummaryType.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ParameterComboBox.comboBoxValue)) {
					if (parameterSummaryType.getSelectedData().toString().equals("SUMMARYMESH")) {
						parameterSwitch.switchParameter("0");
					} else {
						parameterSwitch.switchParameter("1");
					}
				}
			}
		});

		final ParameterCombine combineCheckBox = new ParameterCombine();
		combineCheckBox.addParameters(parameterFeildName, parameterStatisticMode);
		final ParameterSwitch switchStandardFields = new ParameterSwitch();
		switchStandardFields.add("0", new ParameterCombine());
		switchStandardFields.add("1", combineCheckBox);
		parameterStandardFields.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (parameterStandardFields.getSelectedItem().toString().equals("true")) {
					switchStandardFields.switchParameter("1");
				} else {
					switchStandardFields.switchParameter("0");
				}
			}
		});

		final ParameterCombine combineCheckBox1 = new ParameterCombine();
		combineCheckBox1.addParameters(parameterFeildName1, parameterStatisticMode1);
		final ParameterSwitch switchWeightedFields = new ParameterSwitch();
		switchWeightedFields.add("0", new ParameterCombine());
		switchWeightedFields.add("1", combineCheckBox1);
		parameterWeightedFields.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (parameterWeightedFields.getSelectedItem().toString().equals("true")) {
					switchWeightedFields.switchParameter("1");
				} else {
					switchWeightedFields.switchParameter("0");
				}
			}
		});

		parameterCombineSetting.addParameters(parameterSummaryType, parameterSwitch, parameterStandardFields, switchStandardFields,
				parameterWeightedFields, switchWeightedFields, parametersumShape);
		parameters.addParameters(parameterIServerLogin, parameterInputDataType, parameterCombineSetting);
		parameters.getOutputs().addData("SummaryRegionResult", ProcessOutputResultProperties.getString("String_BoundsAnalysisResult"), Type.UNKOWN);
	}

	private void initConstraint() {
		EqualDatasourceConstraint equalSourceDatasource = new EqualDatasourceConstraint();
		equalSourceDatasource.constrained(parameterBigDatasourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalSourceDatasource.constrained(parameterSingleDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
	}


	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_SummaryRegion");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful;
		try {
			if (parameterStandardFields.getSelectedItem().toString().equals("false") && parameterWeightedFields.getSelectedItem().toString().equals("false")) {
				Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_SummaryRegionMessage"));
				return false;
			}
			fireRunning(new RunningEvent(this, ProcessProperties.getString("String_Running")));
			IServerService service = parameterIServerLogin.login();
			CommonSettingCombine input = new CommonSettingCombine("input", "");
			CommonSettingCombine analyst = new CommonSettingCombine("analyst", "");
			parameterInputDataType.initSourceInput(input);
			CommonSettingCombine type = new CommonSettingCombine("type", parameterSummaryType.getSelectedData().toString());
			CommonSettingCombine bounds = new CommonSettingCombine("bounds", parameterBounds.getSelectedItem().toString());
			CommonSettingCombine sumShape = new CommonSettingCombine("sumShape", parametersumShape.getSelectedItem().toString());
			CommonSettingCombine standardSummaryFields = new CommonSettingCombine("standardSummaryFields", parameterStandardFields.getSelectedItem().toString());
			CommonSettingCombine weightedSummaryFields = new CommonSettingCombine("weightedSummaryFields", parameterWeightedFields.getSelectedItem().toString());
			CommonSettingCombine standardFields = new CommonSettingCombine("standardFields", parameterFeildName.getSelectedItem().toString());
			CommonSettingCombine standardStatisticModes = new CommonSettingCombine("standardStatisticModes", parameterStatisticMode.getSelectedItem().toString());
			CommonSettingCombine weightedFields = new CommonSettingCombine("weightedFields", parameterFeildName1.getSelectedItem().toString());
			CommonSettingCombine weightedStatisticModes = new CommonSettingCombine("weightedStatisticModes", parameterStatisticMode1.getSelectedItem().toString());
			if (parameterSummaryType.getSelectedData().toString().equals("SUMMARYMESH")) {
				CommonSettingCombine meshType = new CommonSettingCombine("meshType", parameterMeshType.getSelectedData().toString());
				CommonSettingCombine resolution = new CommonSettingCombine("resolution", parameterMeshSize.getSelectedItem().toString());
				CommonSettingCombine meshSizeUnit = new CommonSettingCombine("meshSizeUnit", parameterMeshSizeUnit.getSelectedData().toString());
				analyst.add(meshType, bounds, standardSummaryFields, weightedSummaryFields, resolution, meshSizeUnit, sumShape);
				if (parameterStandardFields.getSelectedItem().toString().equals("true")) {
					analyst.add(standardFields, standardStatisticModes);
				}
				if (parameterWeightedFields.getSelectedItem().toString().equals("true")) {
					analyst.add(weightedFields, weightedStatisticModes);
				}
			} else {
				Dataset dataset = parameterSingleDataset.getSelectedDataset();
				String regionDatasourceStr = "{\\\"type\\\":\\\"pg\\\",\\\"info\\\":[{\\\"server\\\":\\\"" + parameterTextFieldAddress.getSelectedItem() + "\\\",\\\"datasetNames\\\":[\\\"" + dataset.getName() + "\\\"],\\\"database\\\":\\\"" + parameterDataBaseName.getSelectedItem() + "\\\",\\\"user\\\":\\\"" + parameterTextFieldUserName.getSelectedItem() + "\\\",\\\"password\\\":\\\"" + parameterTextFieldPassword.getSelectedItem() + "\\\"}]}";
				CommonSettingCombine regionDatasource = new CommonSettingCombine("regionDatasource", regionDatasourceStr);
				analyst.add(regionDatasource, bounds, standardSummaryFields, weightedSummaryFields, sumShape);
				if (parameterStandardFields.getSelectedItem().toString().equals("true")) {
					analyst.add(standardFields, standardStatisticModes);
				}
				if (parameterWeightedFields.getSelectedItem().toString().equals("true")) {
					analyst.add(weightedFields, weightedStatisticModes);
				}
			}
			CommonSettingCombine commonSettingCombine = new CommonSettingCombine("", "");
			commonSettingCombine.add(input, analyst, type);
			JobResultResponse response = service.queryResult(MetaKeys.SUMMARY_REGION, commonSettingCombine.getFinalJSon());
			CursorUtilities.setWaitCursor();
			if (null != response) {
				NewMessageBus messageBus = new NewMessageBus(response, DefaultOpenServerMap.INSTANCE);
				isSuccessful = messageBus.run();
			} else {
				isSuccessful = false;
			}

			parameters.getOutputs().getData("SummaryRegionResult").setValue("");
		} catch (Exception e) {
			isSuccessful = false;
			Application.getActiveApplication().getOutput().output(e.getMessage());
		} finally {
			CursorUtilities.setDefaultCursor();
		}

		return isSuccessful;
	}

	@Override
	public String getKey() {
		return MetaKeys.SUMMARY_REGION;
	}

}
