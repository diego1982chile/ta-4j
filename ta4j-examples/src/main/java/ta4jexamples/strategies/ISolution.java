package ta4jexamples.strategies;

import cl.dsoto.trading.model.Execution;

/**
 * Created by des01c7 on 27-03-19.
 */
public interface ISolution {

    public void mapFrom(Execution execution) throws Exception;

    public int getVariables();
}
