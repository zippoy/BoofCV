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

package boofcv.alg.feature.disparity.block;

import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Nested;

/**
 * @author Peter Abeles
 */
public class TestBlockRowScoreNcc {

	@Nested
	class F32 extends ChecksBlockRowScore<GrayF32,float[]> {

		F32() {
			super(1000, ImageType.single(GrayF32.class));
		}

		@Override
		public BlockRowScore<GrayF32, float[]> createAlg(int radiusWidth, int radiusHeight) {
			return new BlockRowScoreNcc.F32(radiusWidth,radiusHeight);
		}

		@Override
		public float[] createArray(int length) {
			return new float[length];
		}

		@Override
		public double naiveScoreRow(int cx, int cy, int disparity, int radius) {
			int x0 = Math.max(disparity,cx-radius);
			int x1 = Math.min(left.width,cx+radius+1);

			float total = 0;
			for (int x = x0; x < x1; x++) {
				float va = left.get(x,cy);
				float vb = right.get(x-disparity,cy);
				total += va*vb;
			}
			return total*(radius*2+1)/(x1-x0);
		}

		@Override
		public double naiveScoreRegion(int cx, int cy, int disparity, int radius) {
			return ncc(left,right,cx,cy,disparity,radius,UtilEjml.F_EPS);
		}

		@Override
		public double get(int index, float[] array) {
			return array[index];
		}
	}

	public static double ncc( ImageGray left , ImageGray right ,
							  int cx, int cy, int disparity, int radius , double eps )
	{
		int x0 = Math.max(disparity,cx-radius);
		int x1 = Math.min(left.width,cx+radius+1);
		int y0 = Math.max(0,cy-radius);
		int y1 = Math.min(left.width,cy+radius+1);

		double meanLeft = mean(left,x0,y0,x1,y1);
		double stdLeft = stdev(left,x0,y0,x1,y1,meanLeft);
		double meanRight = mean(right,x0,y0,x1,y1);
		double stdRight = stdev(right,x0,y0,x1,y1,meanRight);

		double total = 0;
		for (int y = y0; y < y1; y++) {
			double sumRow = 0;
			for (int x = x0; x < x1; x++) {
				double va = GeneralizedImageOps.get(left,x,y)-meanLeft;
				double vb = GeneralizedImageOps.get(right,x-disparity,y)-meanRight;

				sumRow += va*vb;
			}
//			sumRow *= (2*radius+1)/(x1-x0);
			total += sumRow/(x1-x0);
		}
//		total *= (2*radius+1)/(y1-y0);
		total /= (y1-y0);

		return total/(eps + stdLeft*stdRight);
	}

	public static double mean( ImageGray img , int x0, int y0, int x1, int y1 ) {
		double total = 0;
		for (int y = y0; y < y1; y++) {
			for (int x = x0; x < x1; x++) {
				total += GeneralizedImageOps.get(img,x,y);
			}
		}

		return total / ((y1-y0)*(x1-x0));
	}

	public static double stdev(ImageGray img , int x0, int y0, int x1, int y1 , double mean ) {
		double total = 0;
		for (int y = y0; y < y1; y++) {
			for (int x = x0; x < x1; x++) {
				double delta = GeneralizedImageOps.get(img,x,y) - mean;
				total += delta*delta;
			}
		}

		int N = (y1-y0)*(x1-x0);

		return Math.sqrt(total/N);
	}
}