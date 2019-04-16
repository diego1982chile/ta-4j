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
package ta4jexamples.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.ColorBlock;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.strategies.MovingMomentumStrategy;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

/**
 * This class builds a graphical chart showing the buy/sell signals of a strategy.
 */
public class BuyAndSellSignalsToChart {

    /**
     * Builds a JFreeChart time series from a Ta4j time series and an indicator.
     * @param barseries the ta4j time series
     * @param indicator the indicator
     * @param name the name of the chart time series
     * @return the JFreeChart time series
     */
    private static org.jfree.data.time.TimeSeries buildChartTimeSeries(TimeSeries barseries, Indicator<Decimal> indicator, String name) {
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
        for (int i = 0; i < barseries.getBarCount(); i++) {
            Bar bar = barseries.getBar(i);
            chartTimeSeries.add(new Minute(Date.from(bar.getEndTime().toInstant())), indicator.getValue(i).doubleValue());
        }
        return chartTimeSeries;
    }

    /**
     * Runs a strategy over a time series and adds the value markers
     * corresponding to buy/sell signals to the plot.
     * @param series a time series
     * @param strategy a trading strategy
     * @param plot the plot
     */
    private static void addBuySellSignals(TimeSeries series, Strategy strategy, XYPlot plot) {
        // Running the strategy
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        List<Trade> trades = seriesManager.run(strategy).getTrades();
        // Adding markers to plot
        for (Trade trade : trades) {
            // Buy signal
            double buySignalBarTime = new Minute(Date.from(series.getBar(trade.getEntry().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            Marker buyMarker = new ValueMarker(buySignalBarTime);
            buyMarker.setPaint(Color.BLUE);
            buyMarker.setLabel("B");
            plot.addDomainMarker(buyMarker);
            // Sell signal
            double sellSignalBarTime = new Minute(Date.from(series.getBar(trade.getExit().getIndex()).getEndTime().toInstant())).getFirstMillisecond();
            Marker sellMarker = new ValueMarker(sellSignalBarTime);
            sellMarker.setPaint(Color.RED);
            sellMarker.setLabel("S");
            plot.addDomainMarker(sellMarker);
        }
    }

    /**
     * Displays a chart in a frame.
     * @param chart the chart to be displayed
     */
    private static void displayChart(JFreeChart chart) {
        // Chart panel
        ChartPanel panel = new ChartPanel(chart);
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        panel.setPreferredSize(new Dimension(1024, 400));
        // Application frame
        ApplicationFrame frame = new ApplicationFrame("Ta4j example - Buy and sell signals to chart");
        frame.setContentPane(panel);
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    public static JFreeChart buildCandleStickChart(TimeSeries series, Strategy strategy) {
        /**
         * Creating the OHLC dataset
         */
        OHLCDataset ohlcDataset = createOHLCDataset(series);

        /**
         * Creating the additional dataset
         */
        TimeSeriesCollection xyDataset = createAdditionalDataset(series);

        /**
         * Creating the chart
         */
        JFreeChart chart = ChartFactory.createCandlestickChart("", "Time", "USD", ohlcDataset, true);
        // Candlestick rendering
        CandlestickRenderer renderer = new CandlestickRenderer();
        renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
        renderer.setDownPaint(Color.RED);
        renderer.setUpPaint(Color.GREEN);

        renderer.setDrawVolume(false);
        //renderer.setSeriesPaint(0, Color.GRAY);
        renderer.setSeriesOutlinePaint(0, Color.GRAY);

        XYPlot plot = chart.getXYPlot();
        plot.setDomainGridlinesVisible(true);
        plot.setDomainMinorGridlinesVisible(true);
        plot.setBackgroundAlpha(1);

        //plot.setDomainGridlinePaint(new Color(0xCCCCFF));
        //plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRenderer(renderer);
        // Additional dataset

        int index = 1;
        plot.setDataset(index, xyDataset);
        plot.mapDatasetToRangeAxis(index, 0);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(false, false);
        renderer2.setSeriesPaint(0, Color.LIGHT_GRAY);
        plot.setRenderer(index, renderer2);
        // Misc

        //plot.setRangeGridlinePaint(Color.BLACK);
        //plot.setBackgroundPaint(Color.white);
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);

        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        chart.getXYPlot().setRenderer(new MyCandlestickRenderer());


        //Running the strategy and adding the buy and sell signals to plot

        addBuySellSignals(series, strategy, plot);

        /**
         * Displaying the chart
         */
        displayChart(chart);

        return chart;
    }

    public static void main(String[] args) {

        // Getting the time series
        TimeSeries series = CsvTradesLoader.loadBitstampSeries();
        // Building the trading strategy
        Strategy strategy = MovingMomentumStrategy.buildStrategy(series);

        /*
          Building chart datasets
         */
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));

        /*
          Creating the chart
         */
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Bitstamp BTC", // title
                "Date", // x-axis label
                "Price", // y-axis label
                dataset, // data
                true, // create legend?
                true, // generate tooltips?
                false // generate URLs?
                );
        XYPlot plot = (XYPlot) chart.getPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

        /*
          Running the strategy and adding the buy and sell signals to plot
         */
        addBuySellSignals(series, strategy, plot);

        /*
          Displaying the chart
         */
        displayChart(chart);
    }

    /**
     * Builds a JFreeChart OHLC dataset from a ta4j time series.
     * @param series a time series
     * @return an Open-High-Low-Close dataset
     */
    private static OHLCDataset createOHLCDataset(TimeSeries series) {

        final int nbTicks = series.getBarCount();

        Date[] dates = new Date[nbTicks];
        double[] opens = new double[nbTicks];
        double[] highs = new double[nbTicks];
        double[] lows = new double[nbTicks];
        double[] closes = new double[nbTicks];
        double[] volumes = new double[nbTicks];

        for (int i = 0; i < nbTicks; i++) {
            Bar tick = series.getBar(i);
            dates[i] = Date.from(tick.getEndTime().toInstant());
            opens[i] = tick.getOpenPrice().doubleValue();
            highs[i] = tick.getMaxPrice().doubleValue();
            lows[i] = tick.getMinPrice().doubleValue();
            closes[i] = tick.getClosePrice().doubleValue();
            volumes[i] = tick.getVolume().doubleValue();
        }

        OHLCDataset dataset = new DefaultHighLowDataset("btc", dates, highs, lows, opens, closes, volumes);

        return dataset;
    }

    /**
     * Builds an additional JFreeChart dataset from a ta4j time series.
     * @param series a time series
     * @return an additional dataset
     */
    private static TimeSeriesCollection createAdditionalDataset(TimeSeries series) {
        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries("Btc price");
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar tick = series.getBar(i);

            chartTimeSeries.add(new Second(Date.from(tick.getEndTime().toInstant())), indicator.getValue(i).toDouble());
        }
        dataset.addSeries(chartTimeSeries);
        return dataset;
    }
}
