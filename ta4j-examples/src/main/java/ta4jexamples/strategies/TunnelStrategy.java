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

import cl.dsoto.trading.model.Optimization;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

import java.util.List;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class TunnelStrategy {

    /*
    private static int SHORT_EMA = 5;
    private static int LONG_EMA = 12;
    private static int RSI = 21;
    */

    //private static int PERIOD = 50;

    private static int PERIOD = 6;

    private static int MACD_1 = 65;
    private static int MACD_2 = 133;
    private static int SIGNAL_EMA = 150;
    private static int TP_SIGNAL_EMA = 191;

    //6 65 133 150 191

    public static void setPERIOD(int PERIOD) {
        TunnelStrategy.PERIOD = PERIOD;
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

    public static void setTpSignalEma(int tpSignalEma) {
        TP_SIGNAL_EMA = tpSignalEma;
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
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);
        MinPriceIndicator minPrice = new MinPriceIndicator(series);

        WMAIndicator highWMA  = new WMAIndicator(maxPrice, PERIOD);
        WMAIndicator lowWMA  = new WMAIndicator(minPrice,PERIOD);

        MACDIndicator macd = new MACDIndicator(closePrice,MACD_1,MACD_2);

        SMAIndicator signal = new SMAIndicator(macd,SIGNAL_EMA);

        SMAIndicator tpSignal = new SMAIndicator(macd,TP_SIGNAL_EMA);

        Rule entryRule = new CrossedUpIndicatorRule(closePrice, highWMA)
                        .and(new OverIndicatorRule(macd, tpSignal))
                        .and(new OverIndicatorRule(tpSignal, signal));

        Rule exitRule = new CrossedDownIndicatorRule(closePrice, lowWMA)
                        .and(new UnderIndicatorRule(macd, tpSignal))
                        .and(new UnderIndicatorRule(tpSignal, signal));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.xor(stopGain).xor(stopLoss);

        return new BaseStrategy("TunnelStrategy", entryRule, exitRule);
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

    public static void mapFrom(Optimization optimization) throws Exception {

        List solution = null;

        if(!optimization.getSolutions().isEmpty()) {
            solution = optimization.getSolutions().get(0).getSolution();
        }

        if(solution == null) {
            throw new Exception("No existen soluciones registradas para esta estrategia");
        }

        setPERIOD((int) solution.get(0));
        setMacd1((int) solution.get(1));
        setMacd2((int) solution.get(2));
        setSignalEma((int) solution.get(3));
        setTpSignalEma((int) solution.get(4));
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
