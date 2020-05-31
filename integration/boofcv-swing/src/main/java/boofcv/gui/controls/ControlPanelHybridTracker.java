/*
 * Copyright (c) 2011-2020, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.gui.controls;

import boofcv.abst.feature.associate.AssociateDescTo2D;
import boofcv.abst.feature.describe.ConfigTemplateDescribe;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.tracker.ConfigTrackerHybrid;
import boofcv.abst.tracker.PointTracker;
import boofcv.alg.tracker.klt.ConfigPKlt;
import boofcv.factory.feature.associate.ConfigAssociate;
import boofcv.factory.feature.describe.ConfigDescribeRegionPoint;
import boofcv.factory.feature.detdesc.ConfigDetectDescribe;
import boofcv.factory.feature.detect.interest.ConfigDetectInterestPoint;
import boofcv.factory.tracker.FactoryPointTracker;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.pyramid.ConfigDiscreteLevels;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;

/**
 * Control panel for creating Detect-Describe-Associate style trackers
 *
 * @author Peter Abeles
 */
public class ControlPanelHybridTracker extends ControlPanelDetDescAssocBase {
	Listener listener;

	int selectedSelection = 0;
	JComboBox<String> spinnerSelection = combo(selectedSelection,"KLT","Detect","Describe","Associate");

	public ControlPanelPointTrackerKlt controlKlt;
	protected ConfigPKlt configKlt = new ConfigPKlt();
	protected ConfigTrackerHybrid configHybrid = new ConfigTrackerHybrid();

	// Container that specific controls are inserted into
	private JPanel controlPanel = new JPanel(new BorderLayout());
	private JPanel ddaPanel = new JPanel();

	public ControlPanelHybridTracker(Listener listener) {
		this.listener = listener;

		ddaPanel.setLayout(new BoxLayout(ddaPanel,BoxLayout.Y_AXIS));

		// Customize the tracker
		configKlt.toleranceFB = 3;
		configKlt.pruneClose = true;
		configKlt.templateRadius = 3;
		configKlt.pyramidLevels = ConfigDiscreteLevels.levels(4);
		configAssociate.greedy.scoreRatioThreshold = 0.75;
		configDetDesc.detectPoint.general.threshold = 100;
		configDetDesc.detectPoint.general.radius = 4;
		configDetDesc.detectPoint.shiTomasi.radius = 4;
		configDetDesc.describeTemplate.type = ConfigTemplateDescribe.Type.NCC;

		configDetDesc.typeDetector = ConfigDetectInterestPoint.DetectorType.POINT;
		configDetDesc.typeDescribe = ConfigDescribeRegionPoint.DescriptorType.TEMPLATE;
	}

	public ControlPanelHybridTracker(Listener listener,
									 @Nullable ConfigTrackerHybrid configHybrid,
									 @Nullable ConfigPKlt configKlt,
									 @Nullable ConfigDetectDescribe configDetDesc,
									 @Nullable ConfigAssociate configAssociate ) {
		this.listener = listener;
		ddaPanel.setLayout(new BoxLayout(ddaPanel, BoxLayout.Y_AXIS));

		this.configHybrid = configHybrid != null ? configHybrid : this.configHybrid;
		this.configKlt = configKlt != null ? configKlt : this.configKlt;
		this.configDetDesc = configDetDesc != null ? configDetDesc : this.configDetDesc;
		this.configAssociate = configAssociate != null ? configAssociate : this.configAssociate;
	}

	@Override
	public void initializeControlsGUI() {
		super.initializeControlsGUI();
		controlKlt = new ControlPanelPointTrackerKlt(()->listener.changedHybridTracker(),null,configKlt);
		controlKlt.setBorder(BorderFactory.createEmptyBorder());

		updateActiveControls(selectedSelection);

		addLabeled(spinnerSelection,"Component","Select a component of the tracker to modify");
		add(controlPanel);
	}

	private void updateActiveControls( int which ) {
		this.selectedSelection = which;
		controlPanel.removeAll();
		JPanel inside;
		if( which == 0 ) {
			inside = controlKlt;
		} else {
			inside = ddaPanel;
			ddaPanel.removeAll();
			switch (which) {
				case 1 -> { ddaPanel.add(comboDetect); ddaPanel.add(getDetectorPanel()); }
				case 2 -> { ddaPanel.add(comboDescribe); ddaPanel.add(getDescriptorPanel()); }
				case 3 -> { ddaPanel.add(comboAssociate); ddaPanel.add(getAssociatePanel()); }
			}
			ddaPanel.validate();
		}
		if( inside != null ) {
			controlPanel.add(BorderLayout.CENTER, inside);
		}
		controlPanel.validate();
		SwingUtilities.invokeLater(this::repaint);
	}

	public <T extends ImageBase<T>>
	PointTracker<T> createTracker(ImageType<T> imageType ) {
		Class inputType = imageType.getImageClass();

		DetectDescribePoint detDesc = createDetectDescribe(inputType);

		return FactoryPointTracker.hybrid(detDesc,new AssociateDescTo2D(createAssociate(detDesc)),
				configDetDesc.findNonMaxRadius(), configKlt, configHybrid, imageType.getImageClass());
	}

	@Override
	protected void handleControlsUpdated() { listener.changedHybridTracker(); }

	@Override
	public void controlChanged(final Object source) {
		if (source == comboDetect) {
			configDetDesc.typeDetector =
					ConfigDetectInterestPoint.DetectorType.values()[comboDetect.getSelectedIndex()];
		} else if (source == comboDescribe) {
			configDetDesc.typeDescribe =
					ConfigDescribeRegionPoint.DescriptorType.values()[comboDescribe.getSelectedIndex()];
		} else if (source == comboAssociate) {
			configAssociate.type = ConfigAssociate.AssociationType.values()[comboAssociate.getSelectedIndex()];
		}
		updateActiveControls(spinnerSelection.getSelectedIndex());
		listener.changedHybridTracker();
	}

	public interface Listener {
		void changedHybridTracker();
	}
}
