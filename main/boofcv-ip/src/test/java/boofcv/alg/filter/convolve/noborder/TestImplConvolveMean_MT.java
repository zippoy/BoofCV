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

package boofcv.alg.filter.convolve.noborder;

import boofcv.alg.misc.GImageMiscOps;
import boofcv.concurrency.WorkArrays;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageBase;
import boofcv.testing.BoofTesting;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Peter Abeles
 */
public class TestImplConvolveMean_MT {
	Random random = new Random(234);
	int width = 100,height=90;

	/**
	 * Compares results to single threaded
	 */
	@Test
	void compareToSingle() {
		int count = 0;
		Method[] methods = ImplConvolveMean_MT.class.getMethods();
		for( Method m : methods ) {
			String name = m.getName();
			if( !(name.equals("horizontal") || name.equals("vertical")) )
				continue;

			// look up the test method
			Class[] params = m.getParameterTypes();
			Method testM = BoofTesting.findMethod(ImplConvolveMean.class,name,params);


			ImageBase input = GeneralizedImageOps.createImage(params[0],width,height,2);
			ImageBase expected = GeneralizedImageOps.createImage(params[1],width,height,2);
			ImageBase found = GeneralizedImageOps.createImage(params[1],width,height,2);

//			System.out.println("Method "+name+" "+input.getImageType());

			GImageMiscOps.fillUniform(input,random,0,200);

			try {
				if( name.equals("horizontal")) {
					testM.invoke(null, input, expected, 4, 8);
					m.invoke(null, input, found, 4, 8);
				} else {
					WorkArrays workArrays = GeneralizedImageOps.createWorkArray(input.imageType);
					testM.invoke(null, input, expected, 4, 8, workArrays);
					m.invoke(null, input, found, 4, 8, workArrays);
				}
			} catch( Exception e ) {
				e.printStackTrace();
				fail("Exception");
			}

			BoofTesting.assertEquals(expected,found,1);
			count++;
		}
		assertEquals(10,count);
	}
}