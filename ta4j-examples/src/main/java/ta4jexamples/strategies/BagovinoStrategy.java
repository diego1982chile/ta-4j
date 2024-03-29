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
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 2-Period RSI Strategy
 * <p>
 * @see // http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class BagovinoStrategy {


    private static int SHORT_EMA = 5;
    private static int LONG_EMA = 12;
    private static int RSI = 21;


    /*
    private static int SHORT_EMA = 107;
    private static int LONG_EMA = 21;
    private static int RSI = 194;
    */


    //63 11 35
    //47 45 172
    //64 16 33

    public static void setShortEma(int shortEma) {
        SHORT_EMA = shortEma;
    }

    public static void setLongEma(int longEma) {
        LONG_EMA = longEma;
    }

    public static void setRSI(int RSI) {
        BagovinoStrategy.RSI = RSI;
    }

    public static int getShortEma() {
        return SHORT_EMA;
    }

    public static int getLongEma() {
        return LONG_EMA;
    }

    public static int getRSI() {
        return RSI;
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

        EMAIndicator ema5  = new EMAIndicator(closePrice,SHORT_EMA);
        EMAIndicator ema12  = new EMAIndicator(closePrice,LONG_EMA);

        RSIIndicator rsi = new RSIIndicator(closePrice, RSI);

        Rule entryRule = new OverIndicatorRule(ema5, ema12).
                        and(new OverIndicatorRule(closePrice, ema12)).
                        and(new OverIndicatorRule(closePrice, ema5)).
                        and(new CrossedUpIndicatorRule(rsi, Decimal.valueOf(50))).
                        and(new IsRisingRule(rsi, 3)); // Signal 1

        Rule exitRule = new UnderIndicatorRule(ema5, ema12).
                        and(new UnderIndicatorRule(closePrice, ema12)).
                        and(new UnderIndicatorRule(closePrice, ema5)).
                        and(new CrossedDownIndicatorRule(rsi, Decimal.valueOf(50))).
                        and(new IsFallingRule(rsi, 3));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.or(stopGain).or(stopLoss);

        return new BaseStrategy("BagovinoStrategy", entryRule, exitRule);
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
            setShortEma((int) solution.getValues().get(0));
            setLongEma((int) solution.getValues().get(1));
            setRSI((int) solution.getValues().get(2));
        }
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new Pair<>("SHORT_EMA", getShortEma()));
        parameters.add(new Pair<>("LONG_EMA", getLongEma()));
        parameters.add(new Pair<>("RSI", getRSI()));

        return parameters;
    }
}
