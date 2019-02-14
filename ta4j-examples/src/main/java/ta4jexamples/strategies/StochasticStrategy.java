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
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class StochasticStrategy {

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma21  = new SMAIndicator(closePrice,21);
        EMAIndicator ema5  = new EMAIndicator(closePrice,5);
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 8);
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK);

        RSIIndicator r = new RSIIndicator(closePrice, 8);
        Indicator sr = new StochasticRSIIndicator(r, 8);
        //Indicator stochasticK = new SMAIndicator(sr, 35);
        //Indicator stochasticD = new SMAIndicator(stochasticK, 5);

        EMAIndicator ema100  = new EMAIndicator(closePrice,100);
        EMAIndicator ema200  = new EMAIndicator(closePrice,200);

        Rule entryRule = new OverIndicatorRule(ema100, ema200)
                //.and(new CrossedUpIndicatorRule(stochasticK, stochasticD))
                .and(new OverIndicatorRule(stochasticK, stochasticD))
                .and(new OverIndicatorRule(stochasticK, Decimal.valueOf(20)))
                //.and(new OverIndicatorRule(stochasticD, Decimal.valueOf(20)))
                .and(new CrossedUpIndicatorRule(ema5, sma21));

        Rule exitRule = new UnderIndicatorRule(ema100, ema200)
                //.and(new CrossedDownIndicatorRule(stochasticK, stochasticD))
                .and(new UnderIndicatorRule(stochasticK, stochasticD))
                .and(new UnderIndicatorRule(stochasticK, Decimal.valueOf(80)))
                //.and(new UnderIndicatorRule(stochasticD, Decimal.valueOf(80)))
                .and(new CrossedDownIndicatorRule(ema5, sma21));

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
