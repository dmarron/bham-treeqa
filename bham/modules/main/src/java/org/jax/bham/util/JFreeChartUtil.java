/*
 * Copyright (c) 2008 The Jackson Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining  a copy
 * of this software and associated documentation files (the  "Software"), to
 * deal in the Software without restriction, including  without limitation the
 * rights to use, copy, modify, merge, publish,  distribute, sublicense, and/or
 * sell copies of the Software, and to  permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be  included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,  EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE  SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.jax.bham.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;

/**
 * Some utility functions for JFreeChart
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class JFreeChartUtil
{
    /**
     * Convert from a Java2D point to a graph point
     * @param java2DPoint
     *          the java 2D point to convert
     * @param chartPanel
     *          the chart panel to convert
     * @return
     *          the point
     */
    public static Point2D java2DPointToGraphPoint(
            Point2D java2DPoint,
            ChartPanel chartPanel)
    {
        JFreeChart chart = chartPanel.getChart();
        ChartRenderingInfo info =
            chartPanel.getChartRenderingInfo();
        Rectangle2D dataArea = info.getPlotInfo().getDataArea();
        XYPlot xyPlot = chart.getXYPlot();
        
        double graphX = xyPlot.getDomainAxis().java2DToValue(
                java2DPoint.getX(),
                dataArea,
                xyPlot.getDomainAxisEdge());
        double graphY = xyPlot.getRangeAxis().java2DToValue(
                java2DPoint.getY(),
                dataArea,
                xyPlot.getRangeAxisEdge());
        
        return new Point2D.Double(graphX, graphY);
    }
}
