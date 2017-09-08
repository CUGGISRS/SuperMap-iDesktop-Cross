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
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.messageBus.NewMessageBus;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.datas.types.BasicTypes;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.parameters.ParameterPanels.DefaultOpenServerMap;
import com.supermap.desktop.progress.Interface.IUpdateProgress;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.util.concurrent.CancellationException;

/**
 * @author XiaJT
 */
public class MetaProcessSingleQuery extends MetaProcess {

	private ParameterIServerLogin parameterIServerLogin = new ParameterIServerLogin();
	ParameterInputDataType parameterInputDataType = new ParameterInputDataType();
	private ParameterBigDatasourceDatasource parameterQueryDatasource;
	private ParameterSingleDataset parameterQueryDataset;
	private ParameterComboBox parameterQueryTypeComboBox;
	private ParameterDefaultValueTextField parameterDataBaseName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_DataBaseName"));
	private ParameterDefaultValueTextField parameterTextFieldAddress = new ParameterDefaultValueTextField(CoreProperties.getString("String_Server"));
	private ParameterDefaultValueTextField parameterTextFieldUserName = new ParameterDefaultValueTextField(ProcessProperties.getString("String_UserName"));
	private ParameterPassword parameterTextFieldPassword = new ParameterPassword(ProcessProperties.getString("String_PassWord"));

	public MetaProcessSingleQuery() {
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
		parameterQueryDatasource = new ParameterBigDatasourceDatasource();
		parameterQueryDatasource.setRequisite(true);
		parameterQueryDatasource.setDescribe(CommonProperties.getString("String_Label_Datasource"));
		parameterQueryDataset = new ParameterSingleDataset();
		parameterQueryDataset.setRequisite(true);
		parameterQueryDataset.setDescribe(CommonProperties.getString("String_Label_Dataset"));
		parameterQueryTypeComboBox = new ParameterComboBox(CoreProperties.getString("String_AnalystType"));
		parameterQueryTypeComboBox.setRequisite(true);
		parameterQueryTypeComboBox.setItems(
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_ContainCHS"), "CONTAIN"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_CrossCHS"), "CROSS"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_DisjointCHS"), "DISJOINT"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_IdentityCHS"), "IDENTITY"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_IntersectCHS"), "INTERSECT"),
				new ParameterDataNode(CoreProperties.getString("String_None"), "NONE"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_OverlapCHS"), "OVERLAP"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_TouchCHS"), "TOUCH"),
				new ParameterDataNode(CoreProperties.getString("String_SpatialQuery_WithinCHS"), "WITHIN")
		);
		ParameterCombine parameterCombineQuery = new ParameterCombine();
		parameterCombineQuery.setDescribe(ProcessProperties.getString("String_QueryData"));
		parameterCombineQuery.addParameters(parameterTextFieldAddress,
				parameterDataBaseName,
				parameterTextFieldUserName,
				parameterTextFieldPassword,
				parameterQueryDatasource,
				parameterQueryDataset);
		ParameterCombine parameterCombineSetting = new ParameterCombine();
		parameterCombineSetting.setDescribe(ProcessProperties.getString("String_AnalystSet"));
		parameterCombineSetting.addParameters(parameterQueryTypeComboBox);

		parameters.addParameters(parameterIServerLogin, parameterInputDataType, parameterCombineQuery, parameterCombineSetting);
		parameters.addInputParameters("Query", Type.UNKOWN, parameterCombineQuery);// 缺少对应的类型
		parameters.addOutputParameters("QueryResult", ProcessOutputResultProperties.getString("String_SingleDogQueryResult"), BasicTypes.STRING, null);
	}

	private void initComponentState() {
		parameterInputDataType.setSupportDatasetType(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION);
		Dataset defaultBigDataStoreDataset = DatasetUtilities.getDefaultBigDataStoreDataset();
		if (defaultBigDataStoreDataset != null && (DatasetType.POINT == defaultBigDataStoreDataset.getType() || DatasetType.LINE == defaultBigDataStoreDataset.getType()
				|| DatasetType.REGION == defaultBigDataStoreDataset.getType())) {
			parameterQueryDatasource.setSelectedItem(defaultBigDataStoreDataset.getDatasource());
			parameterQueryDataset.setSelectedItem(defaultBigDataStoreDataset);
		}
	}

	private void initConstraint() {
		EqualDatasourceConstraint equalQueryDatasource = new EqualDatasourceConstraint();
		equalQueryDatasource.constrained(parameterQueryDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalQueryDatasource.constrained(parameterQueryDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
	}

	private void initListener() {

	}


	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_SingleQuery");
	}

	@Override
	public boolean execute() {
		boolean isSuccessful;
		try {
			fireRunning(new RunningEvent(this, ProcessProperties.getString("String_Running")));
			IServerService service = parameterIServerLogin.login();
			CommonSettingCombine input = new CommonSettingCombine("input", "");
			parameterInputDataType.initSourceInput(input);
			Dataset queryDataset = parameterQueryDataset.getSelectedDataset();
			String queryType = (String) parameterQueryTypeComboBox.getSelectedData();
			CommonSettingCombine analyst = new CommonSettingCombine("analyst", "");
			String inputQuery = "{\\\"type\\\":\\\"pg\\\",\\\"info\\\":[{\\\"server\\\":\\\"" + parameterTextFieldAddress.getSelectedItem() + "\\\",\\\"datasetNames\\\":[\\\"" + queryDataset.getName() + "\\\"],\\\"database\\\":\\\"" + parameterDataBaseName.getSelectedItem() + "\\\",\\\"user\\\":\\\"" + parameterTextFieldUserName.getSelectedItem() + "\\\",\\\"password\\\":\\\"" + parameterTextFieldPassword.getSelectedItem() + "\\\"}]}";
			analyst.add(new CommonSettingCombine("inputQuery", inputQuery));
			analyst.add(new CommonSettingCombine("mode", queryType));

			CommonSettingCombine commonSettingCombine = new CommonSettingCombine("", "");
			commonSettingCombine.add(input, analyst);
			CursorUtilities.setWaitCursor();
			JobResultResponse response = service.queryResult(MetaKeys.SINGLE_QUERY, commonSettingCombine.getFinalJSon());
			if (null != response) {
				NewMessageBus messageBus = new NewMessageBus(response, DefaultOpenServerMap.INSTANCE);
				isSuccessful = messageBus.run();
			} else {
				isSuccessful = false;
			}

			fireRunning(new RunningEvent(this, 100, CoreProperties.getString(isSuccessful ? "String_Message_Succeed" : "String_Message_Failed")));
			parameters.getOutputs().getData("QueryResult").setValue("");
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
			return false;
		} finally {
			CursorUtilities.setDefaultCursor();
		}
		return isSuccessful;
	}

	@Override
	public String getKey() {
		return MetaKeys.SINGLE_QUERY;
	}
}
