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
import cl.dsoto.trading.model.Solution;

import javafx.util.Pair;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class MovingAveragesStrategy {

    /*
    private static int SHORTER_EMA = 5;
    private static int SHORT_EMA = 14;
    private static int LONG_EMA = 21;
    private static int LONGER_EMA = 50;
    */

    private static int SHORTER_EMA = 150;
    private static int SHORT_EMA = 129;
    private static int LONG_EMA = 114;
    private static int LONGER_EMA = 104;

    //70 24 1 88
    //164 159 87 148
    //70 19 1 12


    public static int getShorterEma() {
        return SHORTER_EMA;
    }

    public static int getShortEma() {
        return SHORT_EMA;
    }

    public static int getLongEma() {
        return LONG_EMA;
    }

    public static int getLongerEma() {
        return LONGER_EMA;
    }

    public static void setShorterEma(int shorterEma) {
        SHORTER_EMA = shorterEma;
    }

    public static void setShortEma(int shortEma) {
        SHORT_EMA = shortEma;
    }

    public static void setLongEma(int longEma) {
        LONG_EMA = longEma;
    }

    public static void setLongerEma(int longerEma) {
        LONGER_EMA = longerEma;
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

        EMAIndicator ema5  = new EMAIndicator(closePrice,SHORTER_EMA);
        EMAIndicator ema14  = new EMAIndicator(closePrice,SHORT_EMA);
        EMAIndicator ema21  = new EMAIndicator(closePrice,LONG_EMA);
        EMAIndicator ema50  = new EMAIndicator(closePrice,LONGER_EMA);

        Rule entryRule = //new OverIndicatorRule(closePrice, ema50).
                        new CrossedUpIndicatorRule(ema5, ema21).
                        and(new CrossedUpIndicatorRule(ema14, ema21));

        Rule exitRule = //new UnderIndicatorRule(closePrice, ema50).
                        new CrossedDownIndicatorRule(ema5, ema21).
                        and(new CrossedDownIndicatorRule(ema14, ema21));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.xor(stopGain).xor(stopLoss);

        return new BaseStrategy("MovingAveragesStrategy", entryRule, exitRule);
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

        if(optimization.getSolutions().isEmpty()) {
            throw new Exception("No existen soluciones registradas para esta estrategia");
        }

        for (Solution solution : optimization.getSolutions()) {
            setShorterEma((int) solution.getValues().get(0));
            setShortEma((int) solution.getValues().get(1));
            setLongEma((int) solution.getValues().get(2));
            setLongerEma((int) solution.getValues().get(3));
        }

    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new Pair<>("SHORTER_EMA", getShorterEma()));
        parameters.add(new Pair<>("SHORT_EMA", getShortEma()));
        parameters.add(new Pair<>("LONG_EMA", getLongEma()));
        parameters.add(new Pair<>("LONGER_EMA", getLongerEma()));

        return parameters;
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
