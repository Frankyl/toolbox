package eu.amidst.core.learning;

import eu.amidst.core.database.DataBase;
import eu.amidst.core.database.StaticDataInstance;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.utils.BayesianNetworkSampler;
import eu.amidst.core.variables.Variable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by Hanen on 08/01/15.
 */
public class MLMultinomialsTest {

    @Test
    public void testingML() throws IOException, ClassNotFoundException {

        // load the true Asia Bayesian network
        BayesianNetwork asianet = BayesianNetworkLoader.loadFromFile("networks/asia.bn");

        System.out.println("\nAsia network \n ");
        //System.out.println(asianet.getDAG().toString());
        //System.out.println(asianet.toString());

        //Sampling from Asia BN
        BayesianNetworkSampler sampler = new BayesianNetworkSampler(asianet);
        sampler.setSeed(0);
        sampler.setParallelMode(true);
        /*try{
            sampler.sampleToAnARFFFile("./data/asiaSamples.arff", 10000);
        } catch (IOException ex){
        }*/

        //Load the sampled data
        //DataBase data = new StaticDataOnDiskFromFile(new ARFFDataReader(new String("data/asiaSamples.arff")));

        //Load the sampled data
        DataBase<StaticDataInstance> data = sampler.sampleToDataBase(10000);
        //Structure learning is excluded from the test, i.e., we use directly the initial Asia network structure
        // and just learn then test the parameter learning

        //Parameter Learning
        MaximumLikelihoodForBN.setBatchSize(1000);
        MaximumLikelihoodForBN.setParallelMode(true);
        BayesianNetwork bnet = MaximumLikelihoodForBN.learnParametersStaticModel(asianet.getDAG(), data);

        //Check if the probability distributions of each node
        for (Variable var : asianet.getStaticVariables()) {
            System.out.println("\n------ Variable " + var.getName() + " ------");
            System.out.println("\nTrue distribution:\n"+ asianet.getDistribution(var));
            System.out.println("\nLearned distribution:\n"+ bnet.getDistribution(var));
            assertTrue(bnet.getDistribution(var).equalDist(asianet.getDistribution(var), 0.05));
        }

        //Or check directly if the true and learned networks are equals
        assertTrue(bnet.equalBNs(asianet,0.05));
    }

}
