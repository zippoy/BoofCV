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

package boofcv.alg.scene;

import boofcv.alg.scene.PointTrackerToSimilarImages.Frame;
import georegression.struct.point.Point2D_F64;
import org.ejml.UtilEjml;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
class TestPointTrackerToSimilarImages {
	@Test
	void processFrame() {
		fail("Implement");
	}

	@Test
	void createFrameSaveObservations() {
		fail("Implement");
	}

	@Test
	void findRelatedPastFrames() {
		fail("Implement");
	}

	@Test
	void getImageIDs() {
		fail("Implement");
	}

	@Test
	void findSimilar() {
		fail("Implement");
	}

	@Test
	void lookupPixelFeats() {
		fail("Implement");
	}

	@Test
	void lookupMatches() {
		fail("Implement");
	}

	@Test
	void lookupShape() {
		fail("Implement");
	}

	@Nested
	class CheckFrame {
		/**
		 * Did some hackery to force the value of no-matches ot be -1
		 */
		@Test
		void valueOfNoMatch() {
			var frame = new Frame();
			assertEquals(-1,frame.id_to_index.get(345354));
		}

		@Test
		void getPixel() {
			var frame = new Frame();
			frame.initActive(5);

			frame.observations[2] = 3;
			frame.observations[3] = 6;

			var found = new Point2D_F64();

			frame.getPixel(1,found);
			assertEquals(3,found.x, UtilEjml.TEST_F64);
			assertEquals(6,found.y, UtilEjml.TEST_F64);
		}
	}
}