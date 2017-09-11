package com.supermap.desktop.WorkflowView.meta.metaProcessImplements;

import com.supermap.data.Dataset;
import com.supermap.data.DatasetType;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.ProcessOutputResultProperties;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.lbs.Interface.IServerService;
import com.supermap.desktop.lbs.params.CommonSettingCombine;
import com.supermap.desktop.lbs.params.JobResultResponse;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.messageBus.NewMessageBus;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.datas.types.BasicTypes;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.parameters.ParameterPanels.DefaultOpenServerMap;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;

/**
 * 矢量裁剪分析
 *
 * @author XiaJT
 */
public class MetaProcessOverlayanalystgeo extends MetaProcess {

	private ParameterIServerLogin parameterIServerLogin = new ParameterIServerLogin();
	ParameterInputDataType parameterInputDataType = new ParameterInputDataType();
	private ParameterBigDatasourceDatasource parameterOverlayDatasource;
	private ParameterSingleDataset parameterOverlayDataset;
	private ParameterComboBox parameterOverlayTypeComboBox;
	private ParameterDefaultValueTextField parameterDataBaseName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_DataBaseName"));
	private ParameterDefaultValueTextField parameterTextFieldAddress = new ParameterDefaultValueTextField(CoreProperties.getString("String_Server"));
	private ParameterDefaultValueTextField parameterTextFieldUserName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_UserName"));
	private ParameterPassword parameterTextFieldPassword = new ParameterPassword(ProcessProperties.getString("String_PassWord"));

	public MetaProcessOverlayanalystgeo() {
		initComponents();
		initComponentState();
		initConstraint();
		initListener();
	}

	private void initComponents() {
		parameterTextFieldAddress.setDefaultWarningValue("192.168.15.248");
		parameterTextFieldAddress.setRequisite(true);
		parameterDataBaseName.setDefaultWarningValue("supermap");
		parameterDataBaseName.setRequisite(true);
		parameterTextFieldUserName.setDefaultWarningValue("postgres");
		parameterTextFieldUserName.setRequisite(true);
		parameterTextFieldPassword.setSelectedItem("supermap");
		parameterTextFieldPassword.setRequisite(true);
		parameterOverlayDatasource = new ParameterBigDatasourceDatasource();
		parameterOverlayDatasource.setDescribe(CommonProperties.getString("String_Label_Datasource"));
		parameterOverlayDatasource.setRequisite(true);
		parameterOverlayDataset = new ParameterSingleDataset(DatasetType.REGION);
		parameterOverlayDataset.setRequisite(true);
		parameterOverlayDataset.setDescribe(CommonProperties.getString("String_Label_Dataset"));

		parameterOverlayTypeComboBox = new ParameterComboBox(CoreProperties.getString("String_OverlayAnalystType"));
		parameterOverlayTypeComboBox.setRequisite(true);
		parameterOverlayTypeComboBox.setItems(
				new ParameterDataNode(CoreProperties.getString("String_Clip"), "clip"),
				new ParameterDataNode(CoreProperties.getString("String_Intersect"), "intersect")
		);

		ParameterCombine parameterCombineOverlay = new ParameterCombine();
		parameterCombineOverlay.setDescribe(CommonProperties.getString("String_clipDataset"));
		parameterCombineOverlay.addParameters(parameterTextFieldAddress,
				parameterDataBaseName,
				parameterTextFieldUserName,
				parameterTextFieldPassword,
				parameterOverlayDatasource,
				parameterOverlayDataset);
		ParameterCombine parameterCombineSetting = new ParameterCombine();
		parameterCombineSetting.setDescribe(ProcessProperties.getString("String_AnalystSet"));
		parameterCombineSetting.addParameters(parameterOverlayTypeComboBox);

		parameters.addParameters(parameterIServerLogin, parameterInputDataType, parameterCombineOverlay, parameterCombineSetting);
		parameters.addInputParameters("overlay", Type.UNKOWN, parameterCombineOverlay);// 缺少对应的类型
		parameters.addOutputParameters("OverlayResult", ProcessOutputResultProperties.getString("String_VectorAnalysisResult"), BasicTypes.STRING, null);
	}

	private void initComponentState() {
		parameterInputDataType.setSupportDatasetType(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION);
		Dataset defaultBigDataStoreDataset = DatasetUtilities.getDefaultBigDataStoreDataset();
		if (defaultBigDataStoreDataset != null && DatasetType.REGION == defaultBigDataStoreDataset.getType()) {
			parameterOverlayDatasource.setSelectedItem(defaultBigDataStoreDataset.getDatasource());
			parameterOverlayDataset.setSelectedItem(defaultBigDataStoreDataset);

		}
	}

	private void initConstraint() {
		EqualDatasourceConstraint equalOverlayDatasource = new EqualDatasourceConstraint();
		equalOverlayDatasource.constrained(parameterOverlayDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalOverlayDatasource.constrained(parameterOverlayDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
	}

	private void initListener() {

	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_overlayanaly");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful;
		try {
			IServerService service = parameterIServerLogin.login();
			CommonSettingCombine input = new CommonSettingCombine("input", "");
			parameterInputDataType.initSourceInput(input);
			Dataset overlayDataset = parameterOverlayDataset.getSelectedDataset();
			String inputOverlayStr = "{\\\"type\\\":\\\"pg\\\",\\\"info\\\":[{\\\"server\\\":\\\"" + parameterTextFieldAddress.getSelectedItem() + "\\\",\\\"datasetNames\\\":[\\\"" + overlayDataset.getName() + "\\\"],\\\"database\\\":\\\"" + parameterDataBaseName.getSelectedItem() + "\\\",\\\"user\\\":\\\"" + parameterTextFieldUserName.getSelectedItem() + "\\\",\\\"password\\\":\\\"" + parameterTextFieldPassword.getSelectedItem() + "\\\"}]}";
			CommonSettingCombine inputOverlay = new CommonSettingCombine("inputOverlay", inputOverlayStr);
			CommonSettingCombine mode = new CommonSettingCombine("mode", (String) parameterOverlayTypeComboBox.getSelectedData());
			CommonSettingCombine analyst = new CommonSettingCombine("analyst", "");
			analyst.add(inputOverlay, mode);

			CommonSettingCombine commonSettingCombine = new CommonSettingCombine("", "");
			commonSettingCombine.add(input, analyst);
			JobResultResponse response = service.queryResult(MetaKeys.OVERLAYANALYSTGEO, commonSettingCombine.getFinalJSon());
			CursorUtilities.setWaitCursor();
			if (null != response) {
				NewMessageBus messageBus = new NewMessageBus(response, DefaultOpenServerMap.INSTANCE);
				isSuccessful = messageBus.run();
			} else {
				isSuccessful = false;
			}

			parameters.getOutputs().getData("OverlayResult").setValue("");// TODO: 2017/6/26 也许没结果,but
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
		return MetaKeys.OVERLAYANALYSTGEO;
	}
}
