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
package ta4jexamples.strategies;

import cl.dsoto.trading.model.Execution;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

import java.util.List;

/**
 * Moving momentum strategy.
 * <p></p>
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:moving_momentum">
 *     http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:moving_momentum</a>
 */
public class MovingMomentumStrategy implements ISolution {

    private static int SHORT_EMA = 9;
    private static int LONG_EMA = 26;

    private static int STOCHASTIC = 14;
    private static int MACD_1 = 9;
    private static int MACD_2 = 26;
    private static int SIGNAL_EMA = 18;

    public static int getShortEma() {
        return SHORT_EMA;
    }

    public static void setShortEma(int shortEma) {
        SHORT_EMA = shortEma;
    }

    public static int getLongEma() {
        return LONG_EMA;
    }

    public static void setLongEma(int longEma) {
        LONG_EMA = longEma;
    }

    public static int getSTOCHASTIC() {
        return STOCHASTIC;
    }

    public static void setSTOCHASTIC(int STOCHASTIC) {
        MovingMomentumStrategy.STOCHASTIC = STOCHASTIC;
    }

    public static int getMacd1() {
        return MACD_1;
    }

    public static void setMacd1(int macd1) {
        MACD_1 = macd1;
    }

    public static int getMacd2() {
        return MACD_2;
    }

    public static void setMacd2(int macd2) {
        MACD_2 = macd2;
    }

    public static int getSignalEma() {
        return SIGNAL_EMA;
    }

    public static void setSignalEma(int signalEma) {
        SIGNAL_EMA = signalEma;
    }

    /**
     * @param series a time series
     * @return a moving momentum strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // The bias is bullish when the shorter-moving average moves above the longer moving average.
        // The bias is bearish when the shorter-moving average moves below the longer moving average.
        EMAIndicator shortEma = new EMAIndicator(closePrice, SHORT_EMA);
        EMAIndicator longEma = new EMAIndicator(closePrice, LONG_EMA);

        StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(series, STOCHASTIC);

        MACDIndicator macd = new MACDIndicator(closePrice, MACD_1, MACD_2);
        EMAIndicator emaMacd = new EMAIndicator(macd, SIGNAL_EMA);
        
        // Entry rule
        Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedDownIndicatorRule(stochasticOscillK, Decimal.valueOf(20))) // Signal 1
                .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2
        
        // Exit rule
        Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
                .and(new CrossedUpIndicatorRule(stochasticOscillK, Decimal.valueOf(80))) // Signal 1
                .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.xor(stopGain).xor(stopLoss);
        
        return new BaseStrategy("MovingMomentumStrategy", entryRule, exitRule);
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

    @Override
    public void mapFrom(Execution execution) throws Exception {

        List solution = null;

        if(!execution.getSolutions().isEmpty()) {
            solution = execution.getSolutions().get(0).getSolution();
        }

        if(solution == null) {
            throw new Exception("No existen soluciones registradas para esta estrategia");
        }

        setShortEma((int) solution.get(0));
        setLongEma((int) solution.get(1));
        setSTOCHASTIC((int) solution.get(2));
        setMacd1((int) solution.get(3));
        setMacd2((int) solution.get(4));
        setSignalEma((int) solution.get(5));
    }

    @Override
    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }
}
