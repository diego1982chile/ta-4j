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

    private static int EMA_50 = 50;
    private static int EMA_21 = 21;
    private static int MACD_12 = 12;
    private static int MACD_26 = 26;
    private static int EMA_9 = 9;

    public MACDStrategy(int ema50, int ema21, int macd12, int macd26, int ema9) {
        EMA_50 = ema50;
        EMA_21 = ema21;
        MACD_12 = macd12;
        MACD_26 = macd26;
        EMA_9 = ema9;
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

        EMAIndicator ema50  = new EMAIndicator(closePrice,EMA_50);
        EMAIndicator ema21  = new EMAIndicator(closePrice,EMA_21);
        EMAIndicator ema3  = new EMAIndicator(closePrice,3);

        MACDIndicator macd = new MACDIndicator(closePrice,MACD_12,MACD_26);

        EMAIndicator signal = new EMAIndicator(macd,EMA_9);

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
