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
package ta4jexamples.bots;

import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import ta4jexamples.loaders.CsvTicksLoader;
import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.research.MultipleStrategy;
import ta4jexamples.strategies.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p/>
 */
public class LiveTestingTimeSeries {

    /** Close price of the last bar */
    private static Decimal LAST_BAR_CLOSE_PRICE;

    private static TimeSeries live;

    /**
     * Builds a moving time series (i.e. keeping only the maxBarCount last bars)
     * @param maxBarCount the number of bars to keep in the time series (at maximum)
     * @return a moving time series
     */
    private static TimeSeries initMovingTimeSeries(int maxBarCount) {
        //TimeSeries series = CsvTradesLoader.loadBitstampSeries();
        //TimeSeries series = CsvTicksLoader.load("EURUSD_Daily_201701020000_201712290000.csv");
        //TimeSeries series = CsvTicksLoader.load("2019_D.csv");
        TimeSeries series = CsvTicksLoader.load("2014_D.csv");
        System.out.print("Initial bar count: " + series.getBarCount());
        // Limitating the number of bars to maxBarCount
        series.setMaximumBarCount(maxBarCount);
        //series.setMaximumBarCount(series.getBarCount());
        LAST_BAR_CLOSE_PRICE = series.getBar(series.getEndIndex()).getClosePrice();
        System.out.println(" (limited to " + maxBarCount + "), close price = " + LAST_BAR_CLOSE_PRICE);

        //live = CsvTicksLoader.load("2020_D.csv");
        live = CsvTicksLoader.load("2015_D.csv");

        return series;
    }

    /**
     * @param series a time series
     * @return a dummy strategy
     */
    private static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        List<Strategy> strategies = new ArrayList<>();

        //strategies.add(CCICorrectionStrategy.buildStrategy(series));
        strategies.add(GlobalExtremaStrategy.buildStrategy(series));
        //strategies.add(MovingMomentumStrategy.buildStrategy(series));
        strategies.add(RSI2Strategy.buildStrategy(series));
        //strategies.add(MACDStrategy.buildStrategy(series));
        strategies.add(StochasticStrategy.buildStrategy(series));
        strategies.add(ParabolicSARStrategy.buildStrategy(series));
        strategies.add(MovingAveragesStrategy.buildStrategy(series));
        strategies.add(BagovinoStrategy.buildStrategy(series));
        //strategies.add(FXBootCampStrategy.buildStrategy(series));
        //strategies.add(TunnelStrategy.buildStrategy(series));
        //strategies.add(WinslowStrategy.buildStrategy(series));

        MultipleStrategy multipleStrategy = new MultipleStrategy(strategies);

        return multipleStrategy.buildStrategy(series);
    }

    /**
     * Generates a random bar.
     * @return a random bar
     */
    private static Bar generateRandomBar(int i) {
        /*
        if(live == null || live.isEmpty()) {
            //live = CsvTicksLoader.load("EURUSD_Daily_201801020000_201812310000.csv");
            live = CsvTicksLoader.load("2020_D.csv");
        }
        */
        LAST_BAR_CLOSE_PRICE = live.getBar(i).getClosePrice();
        return live.getBar(i);
    }

    public static void main(String[] args) throws InterruptedException {

        System.out.println("********************** Initialization **********************");
        // Getting the time series
        TimeSeries series = initMovingTimeSeries(200);

        // Building the trading strategy
        Strategy strategy = buildStrategy(series);

        // Initializing the trading history
        TradingRecord tradingRecord = new BaseTradingRecord();
        System.out.println("************************************************************");

        int STEP = 13;
        int OFFSET = 20;

        boolean flag = false;

        Bar newBar = null;

        /*
          We run the strategy for the 50 next bars.
         */
        for (int i = 0; i < live.getBarCount(); i++) {

            while(!flag) {
                try {
                    // New bar
                    Thread.sleep(30); // I know...
                    newBar = generateRandomBar(i);
                    System.out.println("------------------------------------------------------\n"
                            + "Bar "+i+" added, close price = " + newBar.getClosePrice().doubleValue());
                    series.addBar(newBar);
                    flag = true;
                }
                catch(IllegalArgumentException e) {
                    i++;
                }
            }

            flag = false;

            int endIndex = series.getEndIndex();
            if (strategy.shouldEnter(endIndex)) {
                // Our strategy should enter
                System.out.println("Strategy should ENTER on " + endIndex);
                boolean entered = tradingRecord.enter(endIndex, newBar.getClosePrice(), Decimal.TEN);
                if (entered) {
                    Order entry = tradingRecord.getLastEntry();
                    System.out.println("Entered on " + entry.getIndex()
                            + " (price=" + entry.getPrice().doubleValue()
                            + ", amount=" + entry.getAmount().doubleValue() + ")");
                }
            } else if (strategy.shouldExit(endIndex)) {
                // Our strategy should exit
                System.out.println("Strategy should EXIT on " + endIndex);
                boolean exited = tradingRecord.exit(endIndex, newBar.getClosePrice(), Decimal.TEN);
                if (exited) {
                    Order exit = tradingRecord.getLastExit();
                    System.out.println("Exited on " + exit.getIndex()
                            + " (price=" + exit.getPrice().doubleValue()
                            + ", amount=" + exit.getAmount().doubleValue() + ")");
                }
            }
        }

        // Getting the cash flow of the resulting trades
        CashFlow cashFlow = new CashFlow(series, tradingRecord);

        for (int i = 0; i < 10000; ++i) {
            try {
                System.out.println("CashFlow["+ i +"]: " + cashFlow.getValue(i));
            }
            catch (IndexOutOfBoundsException e) {
                return;
            }
        }
    }
}
