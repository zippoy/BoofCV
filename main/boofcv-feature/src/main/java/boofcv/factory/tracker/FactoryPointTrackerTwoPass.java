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

package boofcv.factory.tracker;

import boofcv.abst.feature.associate.AssociateDescription2D;
import boofcv.abst.feature.describe.DescribeRegionPoint;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.abst.tracker.ConfigTrackerDda;
import boofcv.abst.tracker.PointTrackerTwoPass;
import boofcv.abst.tracker.PointTrackerTwoPassKltPyramid;
import boofcv.alg.feature.detect.interest.EasyGeneralFeatureDetector;
import boofcv.alg.feature.detect.interest.GeneralFeatureDetector;
import boofcv.alg.interpolate.InterpolateRectangle;
import boofcv.alg.tracker.klt.ConfigPKlt;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.factory.interpolate.FactoryInterpolation;
import boofcv.factory.transform.pyramid.FactoryPyramid;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import boofcv.struct.pyramid.PyramidDiscrete;

import static boofcv.factory.tracker.FactoryPointTracker.createShiTomasi;

/**
 * @author Peter Abeles
 */
public class FactoryPointTrackerTwoPass {

	/**
	 * Pyramid KLT feature tracker.
	 *
	 * @param config Config for the tracker. Try PkltConfig.createDefault().
	 * @param configExtract Configuration for extracting features
	 * @return KLT based tracker.
	 */
	public static <I extends ImageGray<I>, D extends ImageGray<D>>
	PointTrackerTwoPass<I> klt(ConfigPKlt config, ConfigGeneralDetector configExtract,
							   Class<I> imageType, Class<D> derivType) {

		GeneralFeatureDetector<I, D> detector = createShiTomasi(configExtract, derivType);

		InterpolateRectangle<I> interpInput = FactoryInterpolation.<I>bilinearRectangle(imageType);
		InterpolateRectangle<D> interpDeriv = FactoryInterpolation.<D>bilinearRectangle(derivType);

		ImageGradient<I,D> gradient = FactoryDerivative.sobel(imageType, derivType);

		PyramidDiscrete<I> pyramid = FactoryPyramid.discreteGaussian(
				config.pyramidLevels,-1,2,true, ImageType.single(imageType));

		return new PointTrackerTwoPassKltPyramid<>(config.config, config.toleranceFB,
				config.templateRadius, pyramid, detector, gradient, interpInput, interpDeriv);
	}

	public static <I extends ImageGray<I>, D extends ImageGray<D>, Desc extends TupleDesc>
	PointTrackerTwoPass<I> dda(GeneralFeatureDetector<I, D> detector,
							   DescribeRegionPoint<I, Desc> describe,
							   AssociateDescription2D<Desc> associate1,
							   AssociateDescription2D<Desc> associate2,
							   double scale,
							   ConfigTrackerDda config ,
							   Class<I> imageType) {

		EasyGeneralFeatureDetector<I,D> easy = new EasyGeneralFeatureDetector<>(detector, imageType, null);

//		DdaManagerGeneralPoint<I,D,Desc> manager = new DdaManagerGeneralPoint<>(easy, describe, scale);
//
//		if( associate2 == null )
//			associate2 = associate1;
//
//		return new DetectDescribeAssociateTwoPass<>(manager, associate1, associate2, config);
		return null;
	}

	public static <I extends ImageGray<I>, Desc extends TupleDesc>
	PointTrackerTwoPass<I> dda(DetectDescribePoint<I,Desc> detectDescribe,
							   AssociateDescription2D<Desc> associate1 ,
							   AssociateDescription2D<Desc> associate2 ,
							   ConfigTrackerDda config ) {


//		DdaManagerDetectDescribePoint<I,Desc> manager = new DdaManagerDetectDescribePoint<>(detectDescribe);
//
//		if( associate2 == null )
//			associate2 = associate1;
//
//		return new DetectDescribeAssociateTwoPass<>(manager, associate1, associate2, config);
		return null;
	}
}
