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
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class MACDStrategy {

    private static int LONG_EMA = 50;
    private static int SHORT_EMA = 21;
    private static int SHORTER_EMA = 3;
    private static int MACD_1 = 12;
    private static int MACD_2 = 26;
    private static int SIGNAL_EMA = 9;

    private static int SHORT_EMA_TREND = 100;
    private static int LONG_EMA_TREND = 200;

    /*
    private static int LONG_EMA = 18;
    private static int SHORT_EMA = 32;
    private static int SHORTER_EMA = 140;
    private static int MACD_1 = 5;
    private static int MACD_2 = 6;
    private static int SIGNAL_EMA = 41;
    */

    //18 32 140 5 6 41


    public static void setLongEma(int longEma) {
        LONG_EMA = longEma;
    }

    public static void setShortEma(int shortEma) {
        SHORT_EMA = shortEma;
    }

    public static void setShorterEma(int shorterEma) {
        SHORTER_EMA = shorterEma;
    }

    public static void setMacd1(int macd1) {
        MACD_1 = macd1;
    }

    public static void setMacd2(int macd2) {
        MACD_2 = macd2;
    }

    public static void setSignalEma(int signalEma) {
        SIGNAL_EMA = signalEma;
    }

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        Decimal zeroLine = Decimal.valueOf(0);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema50  = new EMAIndicator(closePrice,LONG_EMA);
        EMAIndicator ema21  = new EMAIndicator(closePrice,SHORT_EMA);
        EMAIndicator ema3  = new EMAIndicator(closePrice,SHORTER_EMA);

        MACDIndicator macd = new MACDIndicator(closePrice,MACD_1,MACD_2);

        SMAIndicator signal = new SMAIndicator(macd,SIGNAL_EMA);

        /*
        Rule entryRule = new OverIndicatorRule(closePrice, ema21)
                .and(new OverIndicatorRule(macd, signal))
                .and(new CrossedUpIndicatorRule(closePrice, ema50));
                //.and(new CrossedUpIndicatorRule(ema3, ema21));
                //.and(new CrossedUpIndicatorRule(ema3, ema50));
                //.and(new OverIndicatorRule(macd, zeroLine));
        */

        Rule entryRule = new CrossedUpIndicatorRule(closePrice, ema3)
                .and(new OverIndicatorRule(macd, signal))
                .and(new OverIndicatorRule(ema3, ema21))
                .and(new OverIndicatorRule(ema3, ema50))
                .and(new IsRisingRule(ema50, 5))
                .and(new IsRisingRule(ema21, 5))
                /*.and(new IsRisingRule(macd, 5))
                .and(new IsRisingRule(signal, 5))*/
                //.and(new UnderIndicatorRule(macd, Decimal.valueOf(0.005)))
                .and(new OverIndicatorRule(macd, Decimal.valueOf(0)))
                .and(new OverIndicatorRule(signal, Decimal.valueOf(0)));


        Rule exitRule = new CrossedDownIndicatorRule(closePrice, ema3)
                .and(new UnderIndicatorRule(macd, signal))
                .and(new UnderIndicatorRule(ema3, ema21))
                .and(new UnderIndicatorRule(ema3, ema50))
                .and(new IsFallingRule(ema50, 5))
                .and(new IsFallingRule(ema21, 5))
                /*.and(new IsFallingRule(macd, 5))
                .and(new IsFallingRule(signal, 5))*/
                //.and(new OverIndicatorRule(macd, Decimal.valueOf(-0.005)))
                .and(new UnderIndicatorRule(macd, Decimal.valueOf(0)))
                .and(new UnderIndicatorRule(signal, Decimal.valueOf(0)));


        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.xor(stopGain).xor(stopLoss);

        /*
        Rule exitRule = new UnderIndicatorRule(closePrice, ema21)
                .and(new UnderIndicatorRule(macd, signal))
                .and(new CrossedDownIndicatorRule(closePrice, ema50));
                //.and(new CrossedDownIndicatorRule(ema3, ema21));
                //.and(new CrossedDownIndicatorRule(ema3, ema50));
                //.and(new UnderIndicatorRule(macd, zeroLine));
        */

        /*
        Rule entryRule = new CrossedDownIndicatorRule(closePrice, ema21)
                .and(new CrossedDownIndicatorRule(macd, signal))
                .and(new CrossedDownIndicatorRule(closePrice, ema50));
        //.and(new CrossedDownIndicatorRule(ema3, ema21))
        //.and(new CrossedDownIndicatorRule(ema3, ema50));
        //.and(new CrossedDownIndicatorRule(macd, zeroLine));

        Rule exitRule = new CrossedUpIndicatorRule(closePrice, ema21)
                .and(new CrossedUpIndicatorRule(macd, signal))
                .and(new CrossedUpIndicatorRule(closePrice, ema50));
        //.and(new CrossedUpIndicatorRule(ema3, ema21))
        //.and(new CrossedUpIndicatorRule(ema3, ema50));
        //.and(new CrossedUpIndicatorRule(macd, zeroLine));
        */

        return new BaseStrategy("MACDStrategy", entryRule, exitRule);
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
