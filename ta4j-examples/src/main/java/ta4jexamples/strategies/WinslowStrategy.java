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
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class WinslowStrategy {

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator ema800  = new SMAIndicator(closePrice,800);

        SMAIndicator ema200  = new SMAIndicator(closePrice,200);

        EMAIndicator ema144  = new EMAIndicator(closePrice,144);
        EMAIndicator ema62  = new EMAIndicator(closePrice,62);

        MACDIndicator macd = new MACDIndicator(closePrice,12,26);

        EMAIndicator signal = new EMAIndicator(macd,9);

        RSIIndicator r = new RSIIndicator(closePrice, 9);
        Indicator sr = new StochasticRSIIndicator(r, 9);

        Indicator stochasticK = new SMAIndicator(sr, 14);
        Indicator stochasticD = new SMAIndicator(stochasticK, 3);


        Rule yuma = new CrossedUpIndicatorRule(closePrice, ema62)
                .and(new OverIndicatorRule(ema200, ema144))
                .and(new OverIndicatorRule(ema144, ema62))
                .and(new OverIndicatorRule(ema200, ema62))
                //.and(new OverIndicatorRule(ema800, ema200));
                //.and(new OverIndicatorRule(ema800, ema144))
                //.and(new OverIndicatorRule(ema800, ema62))
                //.and(new OverIndicatorRule(stochasticK, stochasticD))
                .and(new OverIndicatorRule(stochasticK, Decimal.valueOf(0.40)));

        Rule tucson = new BooleanRule(true);

        Rule flagStaff = new BooleanRule(true);

        Rule exitRule = new IsEqualRule(closePrice, ema200)
                .and(new OverIndicatorRule(ema200, ema144))
                .and(new OverIndicatorRule(ema144, ema62))
                .and(new OverIndicatorRule(ema200, ema62))
                .and(new OverIndicatorRule(ema800, ema200))
                .and(new OverIndicatorRule(ema800, ema144))
                .and(new OverIndicatorRule(ema800, ema62));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        Rule entryRule = yuma.xor(tucson).xor(flagStaff);

        exitRule = exitRule.xor(stopGain).xor(stopLoss);

        return new BaseStrategy("WinslowStrategy", entryRule, exitRule);
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
