package com.supermap.desktop.CtrlAction.Dataset;

import com.supermap.data.Datasource;
import com.supermap.data.EngineType;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.controls.CollectionDataset.JDialogCreateCollectionDataset;

/**
 * Created by xie on 2017/7/19.
 * 新建数据集集合
 */
public class CtrlActionNewCollectionDataset extends CtrlAction {
	public CtrlActionNewCollectionDataset(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	protected void run() {
		//0表示创建的是矢量数据集集合
		JDialogCreateCollectionDataset collectionDataset = new JDialogCreateCollectionDataset(0);
		collectionDataset.isSetDatasetCollectionCount(false);
		collectionDataset.showDialog();
	}

	@Override
	public boolean enable() {
		boolean enable = false;
		Datasource[] datasources = Application.getActiveApplication().getActiveDatasources();
		if (datasources != null && datasources.length > 0) {
			for (Datasource datasource : datasources) {
				//暂时只支持postgreSql的引擎类型
				if (null != datasource && !datasource.isReadOnly()
						&& (datasource.getEngineType().equals(EngineType.POSTGRESQL) || datasource.getEngineType().equals(EngineType.UDB) ||
						datasource.getEngineType().equals(EngineType.ORACLEPLUS))) {
					enable = true;
					break;
				}
			}
		}
		return enable;
	}
}
