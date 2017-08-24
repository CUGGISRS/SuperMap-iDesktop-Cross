package com.supermap.desktop.CtrlAction.Map.MapCatch;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.utilities.MapUtilities;
import com.supermap.mapping.Layer;
import com.supermap.ui.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yuanR on 2017/8/24 0024.
 * 以矩形框捕捉地图中的原点和目标点
 * 目前服务于工作流中的两点最短地表路径和两点最小耗费距离
 */
public class CtrlActionMapCatchAsRectangle extends CtrlAction {
	public CtrlActionMapCatchAsRectangle(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	private transient GeoRegion geoRegion;
	private static final int SEGMENTCOUNT = 50;

	@Override
	public void run() {
		super.run();
		setAction();
	}

	@Override
	public boolean enable() {
		boolean result = true;
		MapControl activeMapControl = ((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl();
		ArrayList<Layer> arrayList;
		arrayList = MapUtilities.getLayers(activeMapControl.getMap(), true);
		//是否存在图层
		if (arrayList.size() > 0) {
			HashMap<Dataset, Layer> layerMap = new HashMap<>();
			for (int i = 0; i < arrayList.size(); i++) {
				if (arrayList.get(i).getDataset() != null) {
					result = true;
					break;
				}
			}
		}

		Datasources datasources = Application.getActiveApplication().getWorkspace().getDatasources();
		ArrayList<Datasource> isCanUseDatasources = new ArrayList<>();
		for (int i = 0; i < datasources.getCount(); i++) {
			if (!datasources.get(i).isReadOnly()) {
				isCanUseDatasources.add(datasources.get(i));
			}
		}
		if (isCanUseDatasources == null || isCanUseDatasources.size() == 0) {
			result = false;
		}
		return result;
	}


	/**
	 * 设置Action事件
	 */
	private void setAction() {
		final MapControl activeMapControl = ((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl();
		activeMapControl.setTrackMode(TrackMode.TRACK);
		activeMapControl.setAction(Action.CREATERECTANGLE);
		activeMapControl.addMouseListener(controlMouseListener);
		activeMapControl.addTrackedListener(trackedListener);
	}

	private transient MouseListener controlMouseListener = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				MapControl control = ((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl();
				control.removeMouseListener(this);
				exitEdit();
			}
		}
	};

	private transient TrackedListener trackedListener = new TrackedListener() {

		@Override
		public void tracked(TrackedEvent arg0) {
			abstractTracked(arg0);
		}
	};

	private void abstractTracked(TrackedEvent arg0) {
		if (arg0.getGeometry() != null) {
			if (arg0.getGeometry() instanceof GeoRegion) {
				geoRegion = (GeoRegion) arg0.getGeometry().clone();
			} else if (arg0.getGeometry() instanceof GeoPie) {
				geoRegion = ((GeoPie) arg0.getGeometry()).convertToRegion(SEGMENTCOUNT).clone();
			}
			// 当获得GeoRegion后，弹出地图范围捕捉窗体
			DialogMapBoundsCatch dialogMapBoundsCatch = new DialogMapBoundsCatch(geoRegion);
			dialogMapBoundsCatch.showDialog();

			((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl().removeTrackedListener(trackedListener);
			exitEdit();
		} else {
			((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl().addMouseListener(controlMouseListener);
		}
	}

	private void exitEdit() {
		MapControl activeMapControl = ((IFormMap) Application.getActiveApplication().getActiveForm()).getMapControl();
		activeMapControl.setAction(Action.SELECT2);
		activeMapControl.setTrackMode(TrackMode.EDIT);
	}
}
