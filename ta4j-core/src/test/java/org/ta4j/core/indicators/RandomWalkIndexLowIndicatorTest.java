/*
  The MIT License (MIT)

  Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)

  Permission is hereby granted, free of charge, to any person obtaining a copy of
  this software and associated documentation files (the "Software"), to deal in
  the Software without restriction, including without limitation the rights to
  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  the Software, and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be included in all
  copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.ta4j.core.indicators;

import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.mocks.MockBar;
import org.ta4j.core.mocks.MockTimeSeries;

import java.util.ArrayList;
import java.util.List;

import static org.ta4j.core.TATestsUtils.assertDecimalEquals;

/**
 * The Class RandomWalkIndexLowIndicatorTest.
 */
public class RandomWalkIndexLowIndicatorTest {

    protected TimeSeries data;

    @Before
    public void setUp() {

        List<Bar> bars = new ArrayList<Bar>();
        bars.add(new MockBar(44.98, 45.05, 45.17, 44.96));
        bars.add(new MockBar(45.05, 45.10, 45.15, 44.99));
        bars.add(new MockBar(45.11, 45.19, 45.32, 45.11));
        bars.add(new MockBar(45.19, 45.14, 45.25, 45.04));
        bars.add(new MockBar(45.12, 45.15, 45.20, 45.10));
        bars.add(new MockBar(45.15, 45.14, 45.20, 45.10));
        bars.add(new MockBar(45.13, 45.10, 45.16, 45.07));
        bars.add(new MockBar(45.12, 45.15, 45.22, 45.10));
        bars.add(new MockBar(45.15, 45.22, 45.27, 45.14));
        bars.add(new MockBar(45.24, 45.43, 45.45, 45.20));
        bars.add(new MockBar(45.43, 45.44, 45.50, 45.39));
        bars.add(new MockBar(45.43, 45.55, 45.60, 45.35));
        bars.add(new MockBar(45.58, 45.55, 45.61, 45.39));
        bars.add(new MockBar(45.45, 45.01, 45.55, 44.80));
        bars.add(new MockBar(45.03, 44.23, 45.04, 44.17));
        bars.add(new MockBar(44.23, 43.95, 44.29, 43.81));
        bars.add(new MockBar(43.91, 43.08, 43.99, 43.08));
        bars.add(new MockBar(43.07, 43.55, 43.65, 43.06));
        bars.add(new MockBar(43.56, 43.95, 43.99, 43.53));
        bars.add(new MockBar(43.93, 44.47, 44.58, 43.93));
        data = new MockTimeSeries(bars);
    }

    @Test
    public void randomWalkIndexLow() {
        RandomWalkIndexLowIndicator rwil = new RandomWalkIndexLowIndicator(data, 5);

        assertDecimalEquals(rwil.getValue(6), 0.2355);
        assertDecimalEquals(rwil.getValue(7), 0.6762);
        assertDecimalEquals(rwil.getValue(8), 0.3454);
        assertDecimalEquals(rwil.getValue(9), 0.0000);
        assertDecimalEquals(rwil.getValue(10), -0.5548);
        assertDecimalEquals(rwil.getValue(11), -0.4925);
        assertDecimalEquals(rwil.getValue(12), -0.4177);
        assertDecimalEquals(rwil.getValue(13), 0.7110);
        assertDecimalEquals(rwil.getValue(14), 1.3945);
        assertDecimalEquals(rwil.getValue(15), 1.7809);
        assertDecimalEquals(rwil.getValue(16), 2.1609);
        assertDecimalEquals(rwil.getValue(17), 2.1307);
        assertDecimalEquals(rwil.getValue(18), 1.7366);
    }
}
