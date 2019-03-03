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
package ta4jexamples;

import org.ta4j.core.*;
import org.ta4j.core.analysis.CashFlow;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;
import ta4jexamples.analysis.BuyAndSellSignalsToChart;
import ta4jexamples.indicators.IndicatorsToChart;
import ta4jexamples.loaders.CsvTicksLoader;
import ta4jexamples.loaders.CsvTradesLoader;
import ta4jexamples.research.MultipleStrategy;
import ta4jexamples.strategies.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Quickstart for ta4j.
 * <p></p>
 * Global example.
 */
public class Quickstart {

    public static void main(String[] args) {

        // Getting a time series (from any provider: CSV, web service, etc.)
        //TimeSeries series = CsvTradesLoader.loadBitstampSeries();
        TimeSeries series = CsvTicksLoader.load("EURUSD_Daily_201801020000_201812310000.csv");

        // Getting the close price of the bars
        Decimal firstClosePrice = series.getBar(0).getClosePrice();
        System.out.println("First close price: " + firstClosePrice.doubleValue());
        // Or within an indicator:
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        // Here is the same close price:
        System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal to firstClosePrice

        // Getting the simple moving average (SMA) of the close price over the last 5 bars
        SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
        // Here is the 5-bars-SMA value at the 42nd index
        System.out.println("5-bars-SMA value at the 42nd index: " + shortSma.getValue(42).doubleValue());

        // Getting a longer SMA (e.g. over the 30 last bars)
        SMAIndicator longSma = new SMAIndicator(closePrice, 30);


        // Ok, now let's building our trading rules!

        // Buying rules
        // We want to buy:
        //  - if the 5-bars SMA crosses over 30-bars SMA
        //  - or if the price goes below a defined price (e.g $800.00)
        Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma)
                .or(new CrossedDownIndicatorRule(closePrice, Decimal.valueOf("800")));

        // Selling rules
        // We want to sell:
        //  - if the 5-bars SMA crosses under 30-bars SMA
        //  - or if if the price looses more than 3%
        //  - or if the price earns more than 2%
        Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
                .or(new StopLossRule(closePrice, Decimal.valueOf("3")))
                .or(new StopGainRule(closePrice, Decimal.valueOf("2")));

        // Running our juicy trading strategy...
        TimeSeriesManager seriesManager = new TimeSeriesManager(series);

        //TradingRecord tradingRecord = seriesManager.run(new BaseStrategy(buyingRule, sellingRule));

        //TradingRecord tradingRecord = seriesManager.run(CCICorrectionStrategy.buildStrategy(series));

        //TradingRecord tradingRecord = seriesManager.run(GlobalExtremaStrategy.buildStrategy(series));

        //TradingRecord tradingRecord = seriesManager.run(MovingMomentumStrategy.buildStrategy(series));

        //TradingRecord tradingRecord = seriesManager.run(RSI2Strategy.buildStrategy(series));

        //TradingRecord tradingRecord = seriesManager.run(MACDStrategy.buildStrategy(series));

        List<Strategy> strategies = new ArrayList<>();

        //strategies.add(CCICorrectionStrategy.buildStrategy(series));
        //strategies.add(GlobalExtremaStrategy.buildStrategy(series));
        strategies.add(MovingMomentumStrategy.buildStrategy(series));
        //strategies.add(RSI2Strategy.buildStrategy(series));
        strategies.add(MACDStrategy.buildStrategy(series));
        strategies.add(StochasticStrategy.buildStrategy(series));
        //strategies.add(ParabolicSARStrategy.buildStrategy(series));
        strategies.add(MovingAveragesStrategy.buildStrategy(series));
        //strategies.add(BagovinoStrategy.buildStrategy(series));
        //strategies.add(FXBootCampStrategy.buildStrategy(series));

        //0 1 0 1 1 1 1 0 1 1

        MultipleStrategy multipleStrategy = new MultipleStrategy(strategies);

        TradingRecord tradingRecord = seriesManager.run(multipleStrategy.buildStrategy(series));

        System.out.println("Number of trades for our strategy: " + tradingRecord.getTradeCount());

        // Analysis

        // Getting the cash flow of the resulting trades
        CashFlow cashFlow = new CashFlow(series, tradingRecord);

        // Getting the profitable trades ratio
        AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
        System.out.println("Profitable trades ratio: " + profitTradesRatio.calculate(series, tradingRecord));
        // Getting the reward-risk ratio
        AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
        System.out.println("Reward-risk ratio: " + rewardRiskRatio.calculate(series, tradingRecord));

        // Total profit of our strategy
        // vs total profit of a buy-and-hold strategy
        AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(new TotalProfitCriterion());
        System.out.println("Our profit vs buy-and-hold profit: " + vsBuyAndHold.calculate(series, tradingRecord));

        for (int i = 0; i < tradingRecord.getTrades().size(); ++i) {
            System.out.println("Trade["+ i +"]: " + tradingRecord.getTrades().get(i).toString());
        }

        for (int i = 0; i < cashFlow.getSize(); ++i) {
            System.out.println("CashFlow["+ i +"]: " + cashFlow.getValue(i));
        }

        RSIIndicator r = new RSIIndicator(closePrice, 5);
        Indicator sr = new StochasticRSIIndicator(r, 5);

        Indicator stochasticK = new SMAIndicator(sr, 3);
        Indicator stochasticD = new SMAIndicator(stochasticK, 3);

        //IndicatorsToChart.displayChart(series, Arrays.asList(closePrice));

        BuyAndSellSignalsToChart.buildCandleStickChart(series, multipleStrategy.buildStrategy(series));
        IndicatorsToChart.displayChart(series, Arrays.asList(stochasticK, stochasticD));

        // Your turn!
    }
}
