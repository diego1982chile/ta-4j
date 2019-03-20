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
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class FXBootCampStrategy {

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema5  = new EMAIndicator(closePrice,5);
        SMAIndicator sma8  = new SMAIndicator(closePrice,8);

        EMAIndicator ema21  = new EMAIndicator(closePrice,21);
        EMAIndicator ema55  = new EMAIndicator(closePrice,55);
        EMAIndicator ema200  = new EMAIndicator(closePrice,200);

        MACDIndicator macd = new MACDIndicator(closePrice,21,55);

        EMAIndicator ema8 = new EMAIndicator(macd,8);

        RSIIndicator r = new RSIIndicator(closePrice, 8);
        Indicator sr = new StochasticRSIIndicator(r, 8);

        Indicator stochasticK = new SMAIndicator(sr, 35);
        Indicator stochasticD = new SMAIndicator(stochasticK, 5);


        Rule entryRule = new OverIndicatorRule(ema21, ema55)
                .and(new OverIndicatorRule(ema21, ema200))
                //.and(new OverIndicatorRule(ema55, ema200))
                .and(new OverIndicatorRule(ema5, sma8))
                .and(new OverIndicatorRule(macd, ema8))
                .and(new CrossedUpIndicatorRule(stochasticK, stochasticD));

        Rule exitRule = new UnderIndicatorRule(ema21, ema55)
                .and(new UnderIndicatorRule(ema21, ema200))
                //.and(new UnderIndicatorRule(ema55, ema200))
                .and(new CrossedDownIndicatorRule(ema5, sma8));
                //.and(new CrossedDownIndicatorRule(macd, ema8));
                //.and(new CrossedDownIndicatorRule(stochasticK, stochasticD));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.xor(stopGain).xor(stopLoss);

        return new BaseStrategy("FXBootCampStrategy", entryRule, exitRule);
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
