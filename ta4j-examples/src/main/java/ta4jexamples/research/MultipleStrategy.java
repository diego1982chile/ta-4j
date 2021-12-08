package ta4jexamples.research;

import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.*;

import java.util.List;

/**
 * Created by des01c7 on 07-08-18.
 */
public class MultipleStrategy {

    private List<Strategy> strategies;

    private List<Boolean> protectiveLimits;

    public MultipleStrategy(List<Strategy> strategies) {
        this.strategies = strategies;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<Strategy> strategies) {
        this.strategies = strategies;
    }

    public List<Boolean> getProtectiveLimits() {
        return protectiveLimits;
    }

    public MultipleStrategy(List<Strategy> strategies, List<Boolean> protectiveLimits) {
        this.strategies = strategies;
        this.protectiveLimits = protectiveLimits;
    }

    public void setProtectiveLimits(List<Boolean> protectiveLimits) {
        this.protectiveLimits = protectiveLimits;
    }

    /**
     * @param series a time series
     * @return a CCI correction strategy
     */
    public Strategy buildStrategy(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        Rule entryRule = new BooleanRule(false);
        Rule exitRule = new BooleanRule(false);

        for(int i = 0; i < strategies.size(); i++) {
            entryRule = entryRule.or(strategies.get(i).getEntryRule());
            exitRule = exitRule.or(strategies.get(i).getExitRule());
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Rule stopLoss = new StopLossRule(closePrice, Decimal.valueOf(1));
        Rule stopGain = new StopGainRule(closePrice, Decimal.valueOf(1));

        entryRule = entryRule.or(stopLoss).or(stopGain);
        exitRule = exitRule.or(stopLoss).or(stopGain).or(new WaitForRule(Order.OrderType.BUY, 20));

        Strategy strategy = new BaseStrategy(entryRule, exitRule);
        //strategy.setUnstablePeriod(5);
        return strategy;
    }

}
