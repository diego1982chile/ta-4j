/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ta4jexamples.strategies;

import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class StochasticStrategy {

    private static int SMA = 21;
    private static int EMA = 5;
    private static int RSI = 8;
    private static int K = 35;
    private static int D = 5;
    private static int SHORT_EMA = 100;
    private static int LONG_EMA = 200;

    /*
    private static int SMA = 58;
    private static int EMA = 144;
    private static int RSI = 180;
    private static int K = 61;
    private static int D = 51;
    private static int SHORT_EMA = 173;
    private static int LONG_EMA = 98;
    */

    //58 144 180 61 51 173 98

    //74 186 79 19 89 191 1

    public static void setSMA(int SMA) {
        StochasticStrategy.SMA = SMA;
    }

    public static void setEMA(int EMA) {
        StochasticStrategy.EMA = EMA;
    }

    public static void setRSI(int RSI) {
        StochasticStrategy.RSI = RSI;
    }

    public static void setK(int k) {
        K = k;
    }

    public static void setD(int d) {
        D = d;
    }

    public static void setShortEma(int shortEma) {
        SHORT_EMA = shortEma;
    }

    public static void setLongEma(int longEma) {
        LONG_EMA = longEma;
    }

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma21  = new SMAIndicator(closePrice,SMA);
        EMAIndicator ema5  = new EMAIndicator(closePrice,EMA);

        RSIIndicator r = new RSIIndicator(closePrice, RSI);
        Indicator sr = new StochasticRSIIndicator(r, RSI);

        Indicator stochasticK = new SMAIndicator(sr, K);
        Indicator stochasticD = new SMAIndicator(stochasticK, D);

        EMAIndicator ema100  = new EMAIndicator(closePrice,SHORT_EMA);
        EMAIndicator ema200  = new EMAIndicator(closePrice,LONG_EMA);

        Rule entryRule = new OverIndicatorRule(ema100, ema200)
                //.and(new OverIndicatorRule(stochasticK, stochasticD))
                //.and(new IsRisingRule(stochasticK, 5))
                //.and(new IsRisingRule(stochasticD, 5));
                //.and(new OverIndicatorRule(stochasticK, stochasticD))
                //.and(new UnderIndicatorRule(stochasticK, Decimal.valueOf(0.2)));
                //.and(new OverIndicatorRule(stochasticD, Decimal.valueOf(20)))
                .or(new CrossedUpIndicatorRule(ema5, sma21));

        Rule exitRule = new UnderIndicatorRule(ema100, ema200)
                //.and(new UnderIndicatorRule(stochasticK, stochasticD))
                //.and(new IsFallingRule(stochasticK, 5))
                //.and(new IsFallingRule(stochasticD, 5));
                //.and(new OverIndicatorRule(stochasticK, Decimal.valueOf(0.8)));
                //.and(new UnderIndicatorRule(stochasticD, Decimal.valueOf(80)))
                .or(new CrossedDownIndicatorRule(ema5, sma21));

        return new BaseStrategy("StochasticStrategy", entryRule, exitRule);
    }

    public static void main(String[] args) {

        // Getting the time series
        TimeSeries series = CsvTradesLoader.loadBitstampSeries();

        // Building the trading strategy
        Strategy strategy = buildStrategy(series);

        // Running the strategy
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);
        TradingRecord tradingRecord = seriesManager.run(strategy);
        System.out.println("Number of trades for the strategy: " + tradingRecord.getTradeCount());

        // Analysis
        System.out.println("Total profit for the strategy: " + new TotalProfitCriterion().calculate(series, tradingRecord));
    }

    String getName() {
        return this.getClass().getSimpleName();
    }

}
