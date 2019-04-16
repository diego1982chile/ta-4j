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
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
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
public class FXBootCampStrategy {

    /*
    private static int EMA_5 = 5;
    private static int SMA_8 = 8;
    private static int EMA_21 = 21;
    private static int EMA_55 = 55;
    private static int EMA_200 = 200;
    private static int MACD_1 = 21;
    private static int MACD_2 = 55;
    private static int EMA_8 = 8;
    private static int STOCHASTIC_R = 8;
    private static int STOCHASTIC_K = 35;
    private static int STOCHASTIC_D = 5;
    */

    private static int EMA_5 = 34;
    private static int SMA_8 = 123;
    private static int EMA_21 = 116;
    private static int EMA_55 = 101;
    private static int EMA_200 = 35;
    private static int MACD_1 = 4;
    private static int MACD_2 = 159;
    private static int EMA_8 = 164;
    private static int STOCHASTIC_R = 90;
    private static int STOCHASTIC_K = 160;
    private static int STOCHASTIC_D = 5;

    //34 123 116 101 35 4 159 164 90 160 5

    public static int getEma5() {
        return EMA_5;
    }

    public static void setEma5(int ema5) {
        EMA_5 = ema5;
    }

    public static int getSma8() {
        return SMA_8;
    }

    public static void setSma8(int sma8) {
        SMA_8 = sma8;
    }

    public static int getEma21() {
        return EMA_21;
    }

    public static void setEma21(int ema21) {
        EMA_21 = ema21;
    }

    public static int getEma55() {
        return EMA_55;
    }

    public static void setEma55(int ema55) {
        EMA_55 = ema55;
    }

    public static int getEma200() {
        return EMA_200;
    }

    public static void setEma200(int ema200) {
        EMA_200 = ema200;
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

    public static int getEma8() {
        return EMA_8;
    }

    public static void setEma8(int ema8) {
        EMA_8 = ema8;
    }

    public static int getStochasticR() {
        return STOCHASTIC_R;
    }

    public static void setStochasticR(int stochasticR) {
        STOCHASTIC_R = stochasticR;
    }

    public static int getStochasticK() {
        return STOCHASTIC_K;
    }

    public static void setStochasticK(int stochasticK) {
        STOCHASTIC_K = stochasticK;
    }

    public static int getStochasticD() {
        return STOCHASTIC_D;
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

        EMAIndicator ema5  = new EMAIndicator(closePrice,EMA_5);
        SMAIndicator sma8  = new SMAIndicator(closePrice,SMA_8);

        EMAIndicator ema21  = new EMAIndicator(closePrice,EMA_21);
        EMAIndicator ema55  = new EMAIndicator(closePrice,EMA_55);
        EMAIndicator ema200  = new EMAIndicator(closePrice,EMA_200);

        MACDIndicator macd = new MACDIndicator(closePrice,MACD_1, MACD_2);

        EMAIndicator ema8 = new EMAIndicator(macd,EMA_8);

        RSIIndicator r = new RSIIndicator(closePrice, STOCHASTIC_R);
        Indicator sr = new StochasticRSIIndicator(r, STOCHASTIC_R);

        Indicator stochasticK = new SMAIndicator(sr, STOCHASTIC_K);
        Indicator stochasticD = new SMAIndicator(stochasticK, STOCHASTIC_D);


        Rule entryRule = new OverIndicatorRule(ema21, ema55)
                .and(new OverIndicatorRule(ema21, ema200))
                .and(new OverIndicatorRule(ema55, ema200))
                .and(new CrossedUpIndicatorRule(ema5, sma8))
                .and(new OverIndicatorRule(macd, ema8))
                .and(new OverIndicatorRule(stochasticK, stochasticD));

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

    public static void mapFrom(Optimization optimization) throws Exception {

        if(optimization.getSolutions().isEmpty()) {
            throw new Exception("No existen soluciones registradas para esta estrategia");
        }

        for (Solution solution : optimization.getSolutions()) {
            setEma5((int) solution.getValues().get(0));
            setSma8((int) solution.getValues().get(1));
            setEma21((int) solution.getValues().get(2));
            setEma55((int) solution.getValues().get(3));
            setEma200((int) solution.getValues().get(4));
            setMacd1((int) solution.getValues().get(5));
            setMacd2((int) solution.getValues().get(6));
            setEma8((int) solution.getValues().get(7));
            setStochasticR((int) solution.getValues().get(8));
            setStochasticK((int) solution.getValues().get(9));
            setStochasticD((int) solution.getValues().get(10));
        }

    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new ImmutablePair<String, Integer>("EMA_5", getEma5()));
        parameters.add(new ImmutablePair<String, Integer>("SMA_8", getSma8()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_21", getEma21()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_55", getEma55()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_200", getEma200()));
        parameters.add(new ImmutablePair<String, Integer>("MACD_1", getMacd1()));
        parameters.add(new ImmutablePair<String, Integer>("MACD_2", getMacd2()));
        parameters.add(new ImmutablePair<String, Integer>("EMA_8", getEma8()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_R", getStochasticR()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_K", getStochasticK()));
        parameters.add(new ImmutablePair<String, Integer>("STOCHASTIC_D", getStochasticD()));

        return parameters;
    }


    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
