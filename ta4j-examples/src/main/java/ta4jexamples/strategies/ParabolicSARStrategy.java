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
import org.ta4j.core.indicators.*;
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
public class ParabolicSARStrategy {

    /*
    private static int SAR_1 = 76;
    private static int SAR_2 = 100;
    private static int RSI = 10;
    private static int K = 6;
    private static int D = 43;
    */

    private static int SAR_1 = 32;
    private static int SAR_2 = 164;
    private static int RSI = 14;
    private static int K = 59;
    private static int D = 37;


    //124 64 32 13 2
    //4 150 39 4 26
    //112 174 193 30 50


    public static int getSar1() {
        return SAR_1;
    }

    public static int getSar2() {
        return SAR_2;
    }

    public static int getRSI() {
        return RSI;
    }

    public static int getK() {
        return K;
    }

    public static int getD() {
        return D;
    }

    public static void setSar1(int sar1) {
        SAR_1 = sar1;
    }

    public static void setSar2(int sar2) {
        SAR_2 = sar2;
    }

    public static void setRSI(int RSI) {
        ParabolicSARStrategy.RSI = RSI;
    }

    public static void setK(int k) {
        K = k;
    }

    public static void setD(int d) {
        D = d;
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

        ParabolicSarIndicator sar  = new ParabolicSarIndicator(series, Decimal.valueOf(SAR_1/100), Decimal.valueOf(SAR_2/100));
        AccelerationDecelerationIndicator ac = new AccelerationDecelerationIndicator(series);
        AwesomeOscillatorIndicator ao = new AwesomeOscillatorIndicator(series);
        RSIIndicator r = new RSIIndicator(closePrice, RSI);
        Indicator sr = new StochasticRSIIndicator(r, RSI);

        Indicator stochasticK = new SMAIndicator(sr, K);
        Indicator stochasticD = new SMAIndicator(stochasticK, D);


        Rule entryRule = new OverIndicatorRule(closePrice, sar)
                .and(new CrossedUpIndicatorRule(ac, Decimal.valueOf(0)))
                //.and(new CrossedUpIndicatorRule(ao, Decimal.valueOf(0)))
                .and(new IsRisingRule(ac,5))
                .and(new IsRisingRule(ao,5))
                //.and(new CrossedUpIndicatorRule(stochasticK, stochasticD))
                .and(new OverIndicatorRule(stochasticK, Decimal.valueOf(20)))
                .and(new OverIndicatorRule(stochasticD, Decimal.valueOf(20)));
                //.and(new CrossedUpIndicatorRule(stochasticK, stochasticD));

        Rule exitRule = new UnderIndicatorRule(closePrice, sar)
                .and(new CrossedDownIndicatorRule(ac, Decimal.valueOf(0)))
                //.and(new CrossedDownIndicatorRule(ao, Decimal.valueOf(0)));
                .and(new IsFallingRule(ac,5))
                .and(new IsFallingRule(ao,5))
                .and(new CrossedDownIndicatorRule(stochasticK, stochasticD))
                .and(new UnderIndicatorRule(stochasticK, Decimal.valueOf(80)))
                .and(new UnderIndicatorRule(stochasticD, Decimal.valueOf(80)));
                //.and(new CrossedDownIndicatorRule(stochasticK, stochasticD));

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.or(stopGain).or(stopLoss);

        return new BaseStrategy("ParabolicSARStrategy", entryRule, exitRule);
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
            setSar1((int) solution.getValues().get(0));
            setSar2((int) solution.getValues().get(1));
            setRSI((int) solution.getValues().get(2));
            setK((int) solution.getValues().get(3));
            setD((int) solution.getValues().get(4));
        }
    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new Pair<>("SAR_1", getSar1()));
        parameters.add(new Pair<>("SAR_2", getSar2()));
        parameters.add(new Pair<>("RSI", getRSI()));
        parameters.add(new Pair<>("K", getK()));
        parameters.add(new Pair<>("D", getD()));

        return parameters;
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
