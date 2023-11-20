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

import cl.dsoto.trading.model.Optimization;
import cl.dsoto.trading.model.Solution;

import javafx.util.Pair;
import org.ta4j.core.*;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;
import ta4jexamples.loaders.CsvTradesLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 2-Period RSI Strategy
 * <p></p>
 * @see <a href="http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2">
 *     http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2</a>
 */
public class RSI2Strategy {



    private static int RSI = 14;

    private static int EMA = 21;
    private static int SMA_1 = 7;
    private static int SMA_2 = 4;


    /*
    private static int RSI = 7;
    private static int EMA = 11;
    private static int SMA_1 = 54;
    private static int SMA_2 = 16;
    */

    //185 63 10 145

    public static int getRSI() {
        return RSI;
    }

    public static void setRSI(int RSI) {
        RSI2Strategy.RSI = RSI;
    }

    public static int getEMA() {
        return EMA;
    }

    public static void setEMA(int EMA) {
        RSI2Strategy.EMA = EMA;
    }

    public static int getSma1() {
        return SMA_1;
    }

    public static void setSma1(int sma1) {
        SMA_1 = sma1;
    }

    public static int getSma2() {
        return SMA_2;
    }

    public static void setSma2(int sma2) {
        SMA_2 = sma2;
    }

    /**
     * @param series a time series
     * @return a 2-period RSI strategy
     */
    public static Strategy buildStrategy(TimeSeries series) {
        /*
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        //SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        //SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI);

        EMAIndicator ema21 = new EMAIndicator(closePrice, EMA);

        SMAIndicator sma7 = new SMAIndicator(closePrice, SMA_1);
        SMAIndicator sma4 = new SMAIndicator(closePrice, SMA_2);
        
        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(rsi, Decimal.valueOf(50)) // Signal 1
                .and(new IsRisingRule(rsi, 3)) // Signal 1
                .and(new OverIndicatorRule(sma4, sma7)) // Signal 1
                .and(new CrossedUpIndicatorRule(closePrice, ema21)); // Signal 2
        
        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(rsi, Decimal.valueOf(50)) // Signal 1
                .and(new IsFallingRule(rsi, 3)) // Signal 1
                .and(new UnderIndicatorRule(sma4, sma7)) // Signal 1
                .and(new CrossedDownIndicatorRule(closePrice, ema21)); // Signal 2

        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        exitRule = exitRule.or(stopGain).or(stopLoss);
        
        return new BaseStrategy("RSI2Strategy", entryRule, exitRule);
        */

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        Decimal fiveLine = Decimal.valueOf(5);
        Decimal ninetyFiveLine = Decimal.valueOf(95);

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        SMAIndicator longSma = new SMAIndicator(closePrice, 200);

        // We use a 2-period RSI indicator to identify buying
        // or selling opportunities within the bigger trend.
        RSIIndicator rsi = new RSIIndicator(closePrice, 2);

        // Entry rule
        // The long-term trend is up when a security is above its 200-period SMA.
        Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedDownIndicatorRule(rsi, fiveLine)) // Signal 1
                .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

        // Exit rule
        // The long-term trend is down when a security is below its 200-period SMA.
        Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
                .and(new CrossedUpIndicatorRule(rsi, ninetyFiveLine)) // Signal 1
                .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

        // TODO: Finalize the strategy

        return new BaseStrategy(entryRule, exitRule);
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
            setRSI((int) solution.getValues().get(0));
            setSma1((int) solution.getValues().get(1));
            setSma2((int) solution.getValues().get(2));
            setEMA((int) solution.getValues().get(3));
        }
    }

    public static List<Pair<String, Integer>> getParameters() {

        List<Pair<String, Integer>> parameters = new ArrayList<>();

        parameters.add(new Pair<>("RSI", getRSI()));
        parameters.add(new Pair<>("EMA", getEMA()));
        parameters.add(new Pair<>("SMA_1", getSma1()));
        parameters.add(new Pair<>("SMA_2", getSma2()));

        return parameters;
    }

    public int getVariables() {
        return this.getClass().getDeclaredFields().length;
    }

}
