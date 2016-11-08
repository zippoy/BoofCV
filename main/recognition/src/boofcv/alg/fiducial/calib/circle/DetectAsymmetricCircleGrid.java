/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.fiducial.calib.circle;

import boofcv.abst.filter.binary.InputToBinary;
import boofcv.alg.fiducial.calib.circle.EllipseClustersIntoAsymmetricGrid.Grid;
import boofcv.alg.shapes.ellipse.BinaryEllipseDetector;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageGray;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.EllipseRotated_F64;
import org.ddogleg.struct.FastQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Detects asymmetric grids of circles.  The grid is composed of two regular grids which are offset by half a period.
 * See image below for an example.  Rows and columns are counted by counting every row even if they are offset
 * from each other.  The returned grid will be put into canonical orientation, see below.</p>
 *
 * <p>
 * For each circle there is one control point.  The control point is first found by detecting all the ellipses, which
 * is what a circle appears to be under perspective distortion.  The center the ellipse might not match the physical
 * center of the circle.  The intersection of lines does not change under perspective distortion.  The outer common
 * tangent lines between neighboring ellipses are found.  Then the intersection of two such lines is found.  This
 * intersection will be the physical center of the circle.
 * </p>
 *
 * <center>
 * <img src="doc-files/asymcirclegrid.jpg"/>
 * </center>
 * Example of a 8 by 5 grid; row, column.
 *
 * <p>Canonical orientation is defined as having the rows/columns matched, element (0,0) being occupied,
 * and if there are multiple solutions, the solution will be selected with the node closest to the top left
 * being chosen.</p>
 *
 * @author Peter Abeles
 */
public class DetectAsymmetricCircleGrid<T extends ImageGray> {

	private BinaryEllipseDetector<T> ellipseDetector;
	private InputToBinary<T> inputToBinary;

	private GrayU8 binary = new GrayU8(1,1);

	// description of the calibration target
	private int numRows, numCols;

	// converts ellipses into clusters
	private EllipsesIntoClusters clustering;
	private EllipseClustersIntoAsymmetricGrid grider;

	// List of all the found valid grids in the image
	private List<Grid> validGrids = new ArrayList<>();

	// local work space for manipulating the order of points inside a grid
	private List<EllipseRotated_F64> work = new ArrayList<>();

	/**
	 * Creates and configures the detector
	 *
	 * @param numRows number of rows in grid
	 * @param numCols number of columns in grid
	 * @param inputToBinary Converts the input image into a binary image
	 * @param ellipseDetector Detects ellipses inside the image
	 * @param clustering Finds clusters of ellipses
	 */
	public DetectAsymmetricCircleGrid(int numRows, int numCols,
									  InputToBinary<T> inputToBinary,
									  BinaryEllipseDetector<T> ellipseDetector,
									  EllipsesIntoClusters clustering) {
		this.ellipseDetector = ellipseDetector;
		this.inputToBinary = inputToBinary;
		this.numRows = numRows;
		this.numCols = numCols;

		this.clustering = clustering;
		this.grider = new EllipseClustersIntoAsymmetricGrid();
	}

	/**
	 * Processes the image and finds grids.  To retrieve the found grids call {@link #getGrids()}
	 * @param gray Input image
	 */
	public void process(T gray) {
		this.binary.reshape(gray.width,gray.height);

		inputToBinary.process(gray, binary);

		ellipseDetector.process(gray, binary);
		List<EllipseRotated_F64> found = ellipseDetector.getFoundEllipses().toList();

		List<List<EllipsesIntoClusters.Node>> clusters = new ArrayList<>();

		clustering.process(found, clusters);

		pruneIncorrectSize(clusters, numRows*numCols);

		grider.process(found, clusters);

		FastQueue<Grid> grids = grider.getGrids();

		pruneIncorrectShape(grids,numRows,numCols);

		validGrids.clear();
		for (int i = 0; i < grids.size(); i++) {
			Grid g = grids.get(i);
			putGridIntoCanonical(g);
			validGrids.add( g );
		}
	}

	/**
	 * Puts the grid into a canonical orientation
	 */
	void putGridIntoCanonical(Grid g ) {
		// first put it into a plausible solution
		if( g.columns != numCols ) {
			rotateGridCCW(g);
		}

		if( g.get(0,0) == null ) {
			reverse(g);
		}

		// select the best corner for canonical
		if( g.columns%2 == 1 && g.rows%2 == 1) {
			int numRotationsCCW = closestCorner4(g);

			for (int i = 0; i < numRotationsCCW; i++) {
				rotateGridCCW(g);
			}
		} else if( g.columns%2 == 1 ) {
			if( g.get(0,0).center.normSq() > g.get(0,g.columns-1).center.normSq() ) {
				flipHorizontal(g);
			}
		} else if( g.rows%2 == 1 ) {
			if( g.get(0,0).center.normSq() > g.get(g.rows-1,0).center.normSq() ) {
				flipVertical(g);
			}
		}
	}

	/**
	 * Number of CCW rotations to put selected corner into the canonical location.  Only works
	 * when there are 4 possible solutions
	 * @param g The grid
	 * @return number of rotations
	 */
	static int closestCorner4(Grid g ) {
		double bestDistance = g.get(0,0).center.normSq();
		int bestIdx = 0;

		double d = g.get(0,g.columns-1).center.normSq();
		if( d < bestDistance ) {
			bestDistance = d;
			bestIdx = 3;
		}
		d = g.get(g.rows-1,g.columns-1).center.normSq();
		if( d < bestDistance ) {
			bestDistance = d;
			bestIdx = 2;
		}
		d = g.get(g.rows-1,0).center.normSq();
		if( d < bestDistance ) {
			bestIdx = 1;
		}

		return bestIdx;
	}

	/**
	 * performs a counter-clockwise rotation
	 */
	void rotateGridCCW( Grid g ) {

		work.clear();
		for (int i = 0; i < g.rows * g.columns; i++) {
			work.add(null);
		}

		for (int row = 0; row < g.rows; row++) {
			for (int col = 0; col < g.columns; col++) {
				work.set(col*g.rows + row, g.get(g.rows - row - 1,col));
			}
		}

		g.ellipses.clear();
		g.ellipses.addAll(work);
		int tmp = g.columns;
		g.columns = g.rows;
		g.rows = tmp;
	}

	/**
	 * Reverse the order of elements inside the grid
	 */
	void reverse( Grid g ) {
		work.clear();
		int N = g.rows*g.columns;
		for (int i = 0; i < N; i++) {
			work.add( g.ellipses.get(N-i-1));
		}

		g.ellipses.clear();
		g.ellipses.addAll(work);
	}

	void flipHorizontal( Grid g ) {

		work.clear();
		for (int row = 0; row < g.rows; row++) {
			for (int col = 0; col < g.columns; col++) {
				work.add(g.get(row, g.columns - col - 1));
			}
		}

		g.ellipses.clear();
		g.ellipses.addAll(work);
	}

	void flipVertical( Grid g ) {

		work.clear();
		for (int row = 0; row < g.rows; row++) {
			for (int col = 0; col < g.columns; col++) {
				work.add(g.get(g.rows-row-1, col));
			}
		}

		g.ellipses.clear();
		g.ellipses.addAll(work);
	}

	/**
	 * Remove grids which cannot possible match the expected shape
	 */
	static void pruneIncorrectShape(FastQueue<Grid> grids , int numRows, int numCols ) {
		// prune clusters which can't be a member calibration target
		for (int i = grids.size()-1; i >= 0; i--) {
			Grid g = grids.get(i);
			if ((g.rows != numRows || g.columns != numCols) && (g.rows != numCols || g.columns != numRows)) {
				grids.remove(i);
			}
		}
	}

	/**
	 * Prune clusters which do not have the expected number of elements
	 */
	static void pruneIncorrectSize(List<List<EllipsesIntoClusters.Node>> clusters, int N) {
		// prune clusters which can't be a member calibration target
		for (int i = clusters.size()-1; i >= 0; i--) {
			if( clusters.get(i).size() != N ) {
				clusters.remove(i);
			}
		}
	}

	/**
	 * List of grids found inside the image
	 */
	public List<Grid> getGrids() {
		return validGrids;
	}

	public List<Point2D_F64> getCalibrationPoints() {
		return null;
	}

	public GrayU8 getBinary() {
		return binary;
	}

	public int getColumns() {
		return numCols;
	}

	public int getRows() {
		return numRows;
	}
}
