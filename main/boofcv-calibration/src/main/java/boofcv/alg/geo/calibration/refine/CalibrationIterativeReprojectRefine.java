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

package boofcv.alg.geo.calibration.refine;

import boofcv.abst.geo.calibration.DetectorFiducialCalibration;
import boofcv.alg.geo.calibration.CalibrationObservation;
import boofcv.struct.distort.Point2Transform2_F64;
import boofcv.struct.distort.Point2Transform3_F64;
import boofcv.struct.distort.Point3Transform2_F64;
import boofcv.struct.image.GrayF32;
import lombok.Getter;
import lombok.Setter;

/**
 * This [1] attempts to improve the 2D image location accuracy for landmarks by taking the previously estimated
 * calibration parameters, undistorting the region around the found calibration pattern, and redetecting the corners
 * from a canonical frontal parallel view. In the parallel view landmark detectors work best.
 *
 * This will work with narrow and wide FOV camera models.
 *
 * <ol>
 *     <li>Datta, Ankur, Jun-Sik Kim, and Takeo Kanade. "Accurate camera calibration using iterative refinement
 *     of control points." 2009 IEEE 12th International Conference on Computer Vision Workshops, ICCV Workshops.
 *     IEEE, 2009.</li>
 * </ol>
 *
 * @author Peter Abeles
 */
public class CalibrationIterativeReprojectRefine
{
	@Getter @Setter DetectorFiducialCalibration detector;

	public void setImage( GrayF32 image ) {

	}

	public void setUndistortNarrow(Point2Transform2_F64 distort, Point2Transform2_F64 undistort ) {

	}

	public void setUndistortWide(Point3Transform2_F64 distort, Point2Transform3_F64 undistort ) {

	}

	public boolean refine( CalibrationObservation input, CalibrationObservation output ) {
		// TODO take landmarks and find location in undistorted pixel coordinates
		// TODO Find a mapping to arbtirary frontal parallel view
		// TODO Find a rectangle that contains all corners plus extra
		// TODO compute full undistort equation to a good view
		// TODO run calibration detector in rendered image
		// TODO undo the undistort and compute output

		return true;
	}

	/**
	 * Returns how much the location of corners changed.
	 * @return
	 */
	public double getAverageChange() {
		return 0.0;
	}
}
