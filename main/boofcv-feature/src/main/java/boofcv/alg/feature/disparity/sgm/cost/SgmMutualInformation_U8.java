/*
 * Copyright (c) 2011-2019, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.feature.disparity.sgm.cost;

import boofcv.struct.image.GrayU16;
import boofcv.struct.image.GrayU8;

/**
 * TODO document
 *
 * <p>[1] Hirschmuller, Heiko. "Stereo processing by semiglobal matching and mutual information."
 * IEEE Transactions on pattern analysis and machine intelligence 30.2 (2007): 328-341.</p>
 *
 * @author Peter Abeles
 */
public class SgmMutualInformation_U8 implements SgmCostBase.ComputeErrors<GrayU8> {
	SgmCostBase<GrayU8> owner;
	StereoMutualInformation mutual;

	public SgmMutualInformation_U8(StereoMutualInformation mutual) {
		this.mutual = mutual;
	}

	@Override
	public void process(int idxLeft, int idxRight, int idxOut, int disparityMin, int disparityMax, GrayU16 _costXD) {
		final int valLeft = owner.left.data[idxLeft] & 0xFF;
		final byte[] rightData = owner.right.data;
		final short[] costXD = _costXD.data;
		for (int d = disparityMin; d <= disparityMax; d++) {
			int valRight = rightData[idxRight--] & 0xFF;
			costXD[idxOut++] = (short)mutual.costScaled(valLeft,valRight);
		}
	}

	public StereoMutualInformation getMutual() {
		return mutual;
	}

	@Override
	public void setOwner(SgmCostBase<GrayU8> owner) {
		this.owner = owner;
	}
}
