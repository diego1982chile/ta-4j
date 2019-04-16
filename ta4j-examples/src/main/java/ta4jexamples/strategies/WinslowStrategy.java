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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
public class WinslowStrategy {

    /*
    private static int EMA_800 = 800;
    private static int EMA_200 = 200;
    private static int EMA_144 = 144;
    private static int EMA_62 = 62;
    private static int MACD_1 = 12;
    private static int MACD_2 = 26;
    private static int SIGNAL = 9;
    private static int STOCHASTIC_R = 9;
    private static int STOCHASTIC_K = 14;
    private static int STOCHASTIC_D = 3;
    */

    private static int EMA_800 = 150;
    private static int EMA_200 = 188;
    private static int EMA_144 = 142;
    private static int EMA_62 = 4;
    private static int MACD_1 = 3;
    private static int MACD_2 = 99;
    private static int SIGNAL = 162;
    private static int STOCHASTIC_R = 74;
    private static int STOCHASTIC_K = 100;
    private static int STOCHASTIC_D = 135;

    //150 188 142 4 3 99 162 74 100 135

    public static int getEma800() {
        return EMA_800;
    }

    public static int getEma200() {
        return EMA_200;
    }

    public static int getEma144() {
        return EMA_144;
    }

    public static int getEma62() {
        return EMA_62;
    }

    public static int getMacd1() {
        return MACD_1;
    }

    public static int getMacd2() {
        return MACD_2;
    }

    public static int getSIGNAL() {
        return SIGNAL;
    }

    public static int getStochasticR() {
        return STOCHASTIC_R;
    }

    public static int getStochasticK() {
        return STOCHASTIC_K;
    }

    public static int getStochasticD() {
        return STOCHASTIC_D;
    }

    public static void setEma800(int ema800) {
        EMA_800 = ema800;
    }

    public static void setEma200(int ema200) {
        EMA_200 = ema200;
    }

    public static void setEma144(int ema144) {
        EMA_144 = ema144;
    }

    public static void setEma62(int ema62) {
        EMA_62 = ema62;
    }

    public static void setMacd1(int macd1) {
        MACD_1 = macd1;
    }

    public static void setMacd2(int macd2) {
        MACD_2 = macd2;
    }

    public static void setSIGNAL(int SIGNAL) {
        WinslowStrategy.SIGNAL = SIGNAL;
    }

    public static void setStochasticR(int stochasticR) {
        STOCHASTIC_R = stochasticR;
    }

    public static void setStochasticK(int stochasticK) {
        STOCHASTIC_K = stochasticK;
    }

    public static void setStochasticD(int stochasticD) {
        STOCHASTIC_D = stochasticD;
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

        SMAIndicator ema800  = new SMAIndicator(closePrice,EMA_800);

        SMAIndicator ema200  = new SMAIndicator(closePrice,EMA_200);

        EMAIndicator ema144  = new EMAIndicator(closePrice,EMA_144);
        EMAIndicator ema62  = new EMAIndicator(closePrice,EMA_62);

        MACDIndicator macd = new MACDIndicator(closePrice,MACD_1,MACD_2);

        EMAIndicator signal = new EMAIndicator(macd,SIGNAL);

        RSIIndicator r = new RSIIndicator(closePrice, STOCHASTIC_R);
        Indicator sr = new StochasticRSIIndicator(r, STOCHASTIC_R);

        Indicator stochasticK = new SMAIndicator(sr, STOCHASTIC_K);
        Indicator stochasticD = new SMAIndicator(stochasticK, STOCHASTIC_D);


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

    public static void mapFrom(Optimization optimization) throws Exception {

        if(optimization.getSolutions().isEmpty()) {
            throw new Exception("No existen soluciones registradas para esta estrategia");
        }

        for (Solution solution : optimization.getSolutions()) {
            setEma800((int) solution.getValues().get(0));
            setEma200((int) solution.getValues().get(1));
            setEma144((int) solution.getValues().get(2));
            setEma62((int) solution.getValues().get(3));
            setMacd1((int) solution.getValues().get(4));
            setMacd2((int) solution.getValues().get(5));
            setSIGNAL((int) solution.getValues().get(6));
            setStochasticR((int) solution.getValues().get(7));
            setStochasticK((int) solution.getValues().get(8));
            setStochasticD((int) solution.getValues().get(9));
        }

    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new ImmutablePair<String, Integer>("EMA_800", getEma800()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_200", getEma200()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_144", getEma144()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_62", getEma62()));
        parameters.add(new ImmutablePair<String, Integer>("MACD_1", getMacd1()));
        parameters.add(new ImmutablePair<String, Integer>("MACD_2", getMacd2()));
        parameters.add(new ImmutablePair<String, Integer>("SIGNAL", getSIGNAL()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_R", getStochasticR()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_K", getStochasticK()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_D", getStochasticD()));

        return parameters;
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
