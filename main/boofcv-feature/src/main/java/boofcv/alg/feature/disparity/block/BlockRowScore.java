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

import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;

/**
 * Interface for computing disparity scores across an entire row
 *
 * <p>MUST BE THREAD SAFE</p>
 *
 * @author Peter Abeles
 */
public interface BlockRowScore<T extends ImageBase<T>,Array> {

	/**
	 * Specifies the input images
	 *
	 * @param left left image
	 * @param right right image
	 */
	void setInput( T left, T right );

	/**
	 * For a given disparity, the score for each region on the left share many components in common.
	 * Because of this the scores are computed with disparity being the outer most loop
	 *
	 * @param row Image row being examined
	 * @param scores Storage for disparity scores.
	 * @param minDisparity Minimum disparity to consider
	 * @param maxDisparity Maximum disparity to consider
	 * @param regionWidth Size of the sample region's width
	 * @param elementScore Storage for scores of individual pixels
	 */
	void scoreRow(int row, Array scores,
				  int minDisparity , int maxDisparity , int regionWidth ,
				  Array elementScore);

	void score(int elementMax, int indexLeft, int indexRight,
			   Array elementScore);

	/**
	 * If true then the score needs to be normalized
	 */
	boolean isRequireNormalize();

	/**
	 * Additional normalization that's applied after the score for a region is known. Currently only used by
	 * {@link BlockRowScoreNcc}.
	 *
	 * @param row Image row being examined
	 * @param scores Storage for disparity scores.
	 * @param minDisparity Minimum disparity to consider
	 * @param maxDisparity Maximum disparity to consider
	 * @param regionWidth Size of the sample region's width
	 * @param regionHeight Size of the sample region's height
	 */
	void normalizeRegionScores(int row, Array scores,
							   int minDisparity, int maxDisparity, int regionWidth, int regionHeight,
							   Array scoresNorm );

	/**
	 * Applies normalization to a single row
	 *
	 * @param row Row that is being normalized
	 * @param colLeft column in left image
	 * @param colRight column in right image
	 * @param numCols number of columns
	 * @param regionWidth width of the region
	 * @param regionHeight height of the region
	 * @param scores array with scores that are to be normalized
	 * @param indexScores first index in scores that is to be normalized
	 */
	void normalizeScore( int row , int colLeft , int colRight , int numCols, int regionWidth , int regionHeight,
						 Array scores , int indexScores , Array scoresNorm );

	ImageType<T> getImageType();

	abstract class ArrayS32<T extends ImageBase<T>> implements BlockRowScore<T,int[]> {
		T left, right;

		@Override
		public void setInput(T left, T right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public void scoreRow(int row, int[] scores,
							 int minDisparity, int maxDisparity, int regionWidth,
							 int[] elementScore) {
			final int regionRadius = regionWidth/2;
			// disparity as the outer loop to maximize common elements in inner loops, reducing redundant calculations
			for( int d = minDisparity; d <= maxDisparity; d++ ) {
				int dispFromMin = d - minDisparity;

				// number of individual columns the error is computed in
				final int colMax = left.width-d;

				// indexes that data is read to/from for different data structures
				int indexScore = left.width*dispFromMin + dispFromMin;
				int indexLeft = left.startIndex + left.stride*row + d;
				int indexRight = right.startIndex + right.stride*row;

				// Fill elementScore with scores for individual elements for this row at disparity d
				score(colMax, indexLeft, indexRight, elementScore);

				// Sum up to find score at column r
				int score = 0;
				for( int i = 0; i < regionWidth; i++ )
					score += elementScore[i];
				scores[indexScore+regionRadius] = score;

				// Compute left border from 0 to r-1
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+regionRadius-i-1] = score -= elementScore[regionWidth-i-1];
				}
				// Scale the score to remove bias from having fewer pixels
				for (int i = 0; i < regionRadius; i++) {
					// + w/2 will round to the closest integer instead of flooring
					int w = regionWidth-i-1;
					scores[indexScore+regionRadius-i-1] = (scores[indexScore+regionRadius-i-1]*regionWidth+w/2)/w;
				}

				// scores for the remaining inner columns. r+1 to colMax-r-1
				score = scores[indexScore+regionRadius];
				indexScore += regionRadius+1;
				for( int col = regionRadius; col < colMax-regionRadius-1; col++ , indexScore++ ) {
					scores[indexScore] = score += elementScore[col+regionRadius+1] - elementScore[col-regionRadius];
				}

				// Compute right border. colMax-r to colMax-1
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+i] = scores[indexScore+i-1] - elementScore[colMax-regionWidth+i];
				}
				for (int i = 0; i < regionRadius; i++) {
					int w = regionWidth-i-1;
					scores[indexScore+i] = (scores[indexScore+i]*regionWidth+w/2)/w;
				}
			}

		}

		@Override
		public void normalizeRegionScores(int row, int[] scores,
										  int minDisparity, int maxDisparity, int regionWidth, int regionHeight,
										  int[] scoresNorm )
		{
			// disparity as the outer loop to maximize common elements in inner loops, reducing redundant calculations
			for( int d = minDisparity; d <= maxDisparity; d++ ) {
				int dispFromMin = d - minDisparity;

				// number of individual columns the error is computed in
				final int colMax = left.width-d;

				// indexes that data is read to/from for different data structures
				int indexScore = left.width*dispFromMin + dispFromMin;

				normalizeScore(row,d,0,colMax,regionWidth,regionHeight,scores,indexScore,scoresNorm);
			}
		}

		@Override
		public void normalizeScore(int row, int colLeft, int colRight, int numCols, int regionWidth, int regionHeight,
								   int[] scores, int indexScores, int[] scoresNorm) {}
	}

	abstract class ArrayF32<T extends ImageBase<T>> implements BlockRowScore<T,float[]> {
		T left, right;

		@Override
		public void setInput(T left, T right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public void scoreRow(int row, float[] scores,
							 int minDisparity, int maxDisparity, int regionWidth,
							 float[] elementScore) {
			int regionRadius = regionWidth/2;
			// disparity as the outer loop to maximize common elements in inner loops, reducing redundant calculations
			for( int d = minDisparity; d <= maxDisparity; d++ ) {
				int dispFromMin = d - minDisparity;

				// number of individual columns the error is computed in
				final int colMax = left.width-d;

				// indexes that data is read to/from for different data structures
				int indexScore = left.width*dispFromMin + dispFromMin;
				int indexLeft = left.startIndex + left.stride*row + d;
				int indexRight = right.startIndex + right.stride*row;

				// Fill elementScore with scores for individual elements for this row at disparity d
				score(colMax, indexLeft, indexRight, elementScore);

				// Sum up to find score at column r
				float score = 0;
				for( int i = 0; i < regionWidth; i++ )
					score += elementScore[i];
				scores[indexScore+regionRadius] = score;

				// Compute left border from 0 to r-1
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+regionRadius-i-1] = score -= elementScore[regionWidth-i-1];
				}
				// Scale the score to remove bias from having fewer pixels
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+regionRadius-i-1] = scores[indexScore+regionRadius-i-1]*regionWidth/(regionWidth-1-i);
				}

				// scores for the remaining inner columns. r+1 to colMax-r-1
				score = scores[indexScore+regionRadius];
				indexScore += regionRadius+1;
				for( int col = regionRadius; col < colMax-regionRadius-1; col++ , indexScore++ ) {
					scores[indexScore] = score += elementScore[col+regionRadius+1] - elementScore[col-regionRadius];
				}

				// Compute right border. colMax-r to colMax-1
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+i] = scores[indexScore+i-1] - elementScore[colMax-regionWidth+i];
				}
				for (int i = 0; i < regionRadius; i++) {
					scores[indexScore+i] = scores[indexScore+i]*regionWidth/(regionWidth-i-1);
				}
			}
		}

		@Override
		public void normalizeRegionScores(int row, float[] scores,
										  int minDisparity, int maxDisparity, int regionWidth, int regionHeight, float[] scoresNorm ) {
			// disparity as the outer loop to maximize common elements in inner loops, reducing redundant calculations
			for( int d = minDisparity; d <= maxDisparity; d++ ) {
				int dispFromMin = d - minDisparity;

				// number of individual columns the error is computed in
				final int colMax = left.width-d;

				// indexes that data is read to/from for different data structures
				int indexScore = left.width*dispFromMin + dispFromMin;

				normalizeScore(row,d,0,colMax,regionWidth,regionHeight,scores,indexScore,scoresNorm);
			}
		}

		@Override
		public void normalizeScore(int row, int colLeft, int colRight, int numCols, int regionWidth, int regionHeight,
								   float[] scores, int indexScores, float[] scoresNorm) {}
	}
}
