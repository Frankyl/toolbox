package eu.amidst.core.learning;

import com.google.common.base.Stopwatch;
import eu.amidst.core.datastream.DataStream;
import eu.amidst.core.datastream.DynamicDataInstance;
import eu.amidst.core.distribution.ConditionalDistribution;
import eu.amidst.core.distribution.ConditionalLinearGaussian;
import eu.amidst.core.distribution.Multinomial_MultinomialParents;
import eu.amidst.core.inference.VMP;
import eu.amidst.core.learning.dynamic.BayesianLearningEngineForDBN;
import eu.amidst.core.learning.dynamic.MaximumLikelihoodForDBN;
import eu.amidst.core.learning.dynamic.StreamingVariationalBayesVMPForDBN;
import eu.amidst.core.models.DynamicBayesianNetwork;
import eu.amidst.core.models.DynamicDAG;
import eu.amidst.core.utils.DynamicBayesianNetworkGenerator;
import eu.amidst.core.utils.DynamicBayesianNetworkSampler;
import eu.amidst.core.variables.DynamicVariables;
import eu.amidst.core.variables.Variable;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class StreamingVariationalBayesVMPForDBNTest extends TestCase {


    public static void test0(){

        DynamicBayesianNetworkGenerator.setNumberOfContinuousVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfDiscreteVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfStates(2);

        DynamicBayesianNetwork dbn = DynamicBayesianNetworkGenerator.generateDynamicNaiveBayes(new Random(0), 2, true);

        DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dbn);
        sampler.setSeed(0);
        DataStream<DynamicDataInstance> dataStream = sampler.sampleToDataBase(10,100);

        //dataStream.stream().forEach(d -> System.out.println(d.getValue(dbn.getDynamicVariables().getVariable("ClassVar"))));


        DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dbn.getDynamicDAG(), dataStream);

        System.out.println(bnet);

        StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
        svb.setWindowsSize(10);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
        vmp.setOutput(false);
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.0001);

        svb.setDynamicDAG(dbn.getDynamicDAG());
        svb.setDataStream(dataStream);

        //svb.initLearning();
        //System.out.println(svb.getPlateuVMPDBN().toStringParemetersTimeT());
        //System.out.println(svb.getPlateuVMPDBN().toStringTemporalClones());
        //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

        svb.runLearning();


        DynamicBayesianNetwork learnDBN = svb.getLearntDBN();

        for (ConditionalDistribution dist : learnDBN.getDistributionsTimeT()) {
            System.out.println("Real one:");
            System.out.println(dbn.getConditionalDistributionTimeT(dist.getVariable()).toString());
            System.out.println("Learnt one:");
            System.out.println(dist.toString());
            assertTrue(dist.equalDist(dbn.getConditionalDistributionTimeT(dist.getVariable()), 0.05));
        }

    }
    public static void test1(){

        DynamicBayesianNetworkGenerator.setNumberOfContinuousVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfDiscreteVars(5);
        DynamicBayesianNetworkGenerator.setNumberOfStates(2);

        DynamicBayesianNetwork dbn = DynamicBayesianNetworkGenerator.generateDynamicNaiveBayes(new Random(0), 2, true);

        DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dbn);
        sampler.setSeed(0);
        DataStream<DynamicDataInstance> dataStream = sampler.sampleToDataBase(1,5000);

        StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
        svb.setWindowsSize(1);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
        vmp.setOutput(false);
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

        BayesianLearningEngineForDBN.setDynamicDAG(dbn.getDynamicDAG());
        BayesianLearningEngineForDBN.setDataStream(dataStream);
        BayesianLearningEngineForDBN.runLearning();

        DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

        for (ConditionalDistribution dist : learnDBN.getDistributionsTimeT()) {
            System.out.println("Real one:");
            System.out.println(dbn.getConditionalDistributionTimeT(dist.getVariable()).toString());
            System.out.println("Learnt one:");
            System.out.println(dist.toString());
            assertTrue(dist.equalDist(dbn.getConditionalDistributionTimeT(dist.getVariable()), 0.05));
        }

    }

    public static void test2(){

        DynamicBayesianNetworkGenerator.setNumberOfContinuousVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfDiscreteVars(5);
        DynamicBayesianNetworkGenerator.setNumberOfStates(2);

        DynamicBayesianNetwork dbn = DynamicBayesianNetworkGenerator.generateDynamicNaiveBayes(new Random(0), 2, true);

        DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dbn);
        sampler.setSeed(0);
        DataStream<DynamicDataInstance> dataStream = sampler.sampleToDataBase(1000,1);

        StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
        svb.setWindowsSize(1);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

        BayesianLearningEngineForDBN.setDynamicDAG(dbn.getDynamicDAG());
        BayesianLearningEngineForDBN.setDataStream(dataStream);
        BayesianLearningEngineForDBN.runLearning();

        DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

        for (ConditionalDistribution dist : learnDBN.getDistributionsTime0()) {
            System.out.println("Real one:");
            System.out.println(dbn.getConditionalDistributionTime0(dist.getVariable()).toString());
            System.out.println("Learnt one:");
            System.out.println(dist.toString());
            assertTrue(dist.equalDist(dbn.getConditionalDistributionTime0(dist.getVariable()), 0.05));
        }

    }

    public static void test3(){

        DynamicBayesianNetworkGenerator.setNumberOfContinuousVars(2);
        DynamicBayesianNetworkGenerator.setNumberOfDiscreteVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfStates(2);

        DynamicBayesianNetwork dbn = DynamicBayesianNetworkGenerator.generateDynamicNaiveBayes(new Random(0), 2, true);

        System.out.println(dbn.toString());

        DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dbn);
        sampler.setSeed(10);
        DataStream<DynamicDataInstance> dataStream = sampler.sampleToDataBase(100,500);

        DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dbn.getDynamicDAG(), dataStream);

        System.out.println(bnet.toString());

        //dataStream.stream().forEach(d -> System.out.println(d.getValue(dbn.getDynamicVariables().getVariable("ContinuousVar1")) + ", "+ d.getValue(dbn.getDynamicVariables().getVariable("ContinuousVar2"))));

        StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
        svb.setWindowsSize(500);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
        vmp.setOutput(true);
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.001);
        BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

        BayesianLearningEngineForDBN.setDynamicDAG(dbn.getDynamicDAG());
        BayesianLearningEngineForDBN.setDataStream(dataStream);
        BayesianLearningEngineForDBN.runLearning();

        DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

        for (ConditionalDistribution dist : learnDBN.getDistributionsTimeT()) {
            System.out.println("Real one:");
            System.out.println(dbn.getConditionalDistributionTimeT(dist.getVariable()).toString());
            System.out.println("Learnt one:");
            System.out.println(dist.toString());
            //assertTrue(dist.equalDist(dbn.getConditionalDistributionTimeT(dist.getVariable()), 0.5));
        }

    }

    public static void test4(){

        DynamicBayesianNetworkGenerator.setNumberOfContinuousVars(5);
        DynamicBayesianNetworkGenerator.setNumberOfDiscreteVars(0);
        DynamicBayesianNetworkGenerator.setNumberOfStates(2);

        DynamicBayesianNetwork dbn = DynamicBayesianNetworkGenerator.generateDynamicNaiveBayes(new Random(0), 2, true);

        DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dbn);
        sampler.setSeed(0);
        DataStream<DynamicDataInstance> dataStream = sampler.sampleToDataBase(10000,1);

        StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
        svb.setWindowsSize(1);
        svb.setSeed(5);
        VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
        vmp.setTestELBO(true);
        vmp.setMaxIter(1000);
        vmp.setThreshold(0.0001);
        BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

        BayesianLearningEngineForDBN.setDynamicDAG(dbn.getDynamicDAG());
        BayesianLearningEngineForDBN.setDataStream(dataStream);
        BayesianLearningEngineForDBN.runLearning();

        DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

        for (ConditionalDistribution dist : learnDBN.getDistributionsTime0()) {
            System.out.println("Real one:");
            System.out.println(dbn.getConditionalDistributionTime0(dist.getVariable()).toString());
            System.out.println("Learnt one:");
            System.out.println(dist.toString());
            assertTrue(dist.equalDist(dbn.getConditionalDistributionTime0(dist.getVariable()), 0.5));
        }

    }

    @Test
    public void testGaussianChain() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 10; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            //Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            //Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);
            //dynamicDAG.getParentSetTimeT(varC).addParent(varC);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varC);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));
            //dynamicDAG.getParentSetTimeT(varC).addParent(dynamicVariables.getTemporalClone(varC));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i+10));

            //ConditionalLinearGaussian distA = dynamicNB.getConditionalDistributionTimeT(varA);
            //distA.setIntercept(0.0);
            //distA.setCoeffParents(new double[]{1.0});

            //ConditionalLinearGaussian distB = dynamicNB.getConditionalDistributionTimeT(varA);
            //distB.setIntercept(0.0);
            //distB.setCoeffParents(new double[]{1.0});

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(2, 1000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(100);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));
            //data.stream().forEach(d -> System.out.println(d.getValue(varA)));


            DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dynamicNB.getDynamicDAG(), data);

            System.out.println(watch.stop());
            System.out.println();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + bnet.getDistributionTimeT(var));
                assertTrue(bnet.getDistributionTimeT(var).equalDist(dynamicNB.getDistributionTimeT(var), 0.2));
            }

            System.out.println();

            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(10000);
            vmp.setThreshold(0.001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.2));
            }
            System.out.println();
            System.out.println();

        }
    }


    @Test
    public void testGaussianChain2() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 1; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            //Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            //Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            //Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varA);
            //dynamicDAG.getParentSetTimeT(varC).addParent(varC);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varC);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));
            dynamicDAG.getParentSetTimeT(varC).addParent(dynamicVariables.getTemporalClone(varC));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i+10));

            //ConditionalLinearGaussian distA = dynamicNB.getConditionalDistributionTimeT(varA);
            //distA.setIntercept(0.0);
            //distA.setCoeffParents(new double[]{1.0});



            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(2, 1000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(100);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));
            //data.stream().forEach(d -> System.out.println(d.getValue(varA)));


            DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dynamicNB.getDynamicDAG(), data);

            System.out.println(watch.stop());
            System.out.println();

            boolean skip = false;
            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + bnet.getDistributionTimeT(var));
                if (!(bnet.getDistributionTimeT(var).equalDist(dynamicNB.getDistributionTimeT(var), 0.2))) {
                    skip=true;
                    break;
                }
            }

            if (skip)
                continue;

            System.out.println();

            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(10000);
            vmp.setThreshold(0.001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.2));
            }
            System.out.println();
            System.out.println();

        }
    }


    @Test
    public void testGaussianChain3() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 1; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            //Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            //Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);
            //dynamicDAG.getParentSetTimeT(varC).addParent(varC);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varC);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));
            //dynamicDAG.getParentSetTimeT(varC).addParent(dynamicVariables.getTemporalClone(varC));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i+10));

            //ConditionalLinearGaussian distA = dynamicNB.getConditionalDistributionTimeT(varA);
            //distA.setIntercept(0.0);
            //distA.setCoeffParents(new double[]{1.0});

            ConditionalLinearGaussian distB = dynamicNB.getConditionalDistributionTimeT(varB);
            distB.setIntercept(0.0);
            distB.setCoeffParents(new double[]{1.0});
            distB.setVariance(0.1);

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            //sampler.setHiddenVar(varA);
            sampler.setMARVar(varA,0.9);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(20, 500);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(100);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));
            //data.stream().forEach(d -> System.out.println(d.getValue(varA)));



            System.out.println();

            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(10000);
            vmp.setThreshold(0.001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.2));
            }
            System.out.println();
            System.out.println();

        }
    }

    @Test
    public void testGaussianChain4() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 1; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            //Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            //Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);
            //dynamicDAG.getParentSetTimeT(varC).addParent(varC);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varC);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));
            //dynamicDAG.getParentSetTimeT(varC).addParent(dynamicVariables.getTemporalClone(varC));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i+10));

            ConditionalLinearGaussian distA = dynamicNB.getConditionalDistributionTimeT(varA);
            distA.setIntercept(0.0);
            distA.setCoeffParents(new double[]{1.0});
            distA.setVariance(0.000001);


            ConditionalLinearGaussian distB = dynamicNB.getConditionalDistributionTimeT(varB);
            distB.setIntercept(0.0);
            distB.setCoeffParents(new double[]{1.0});
            distB.setVariance(0.000001);

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setHiddenVar(varA);
            //sampler.setMARVar(varA,0.4);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(1, 10000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(100);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));
            //data.stream().forEach(d -> System.out.println(d.getValue(varA)));



            System.out.println();

            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(1000);
            svb.setSeed(0);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(10000);
            vmp.setThreshold(0.001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            System.out.println(svb.getLogMarginalProbability());

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                //assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.2));
            }
            System.out.println();
            System.out.println();

        }
    }



    @Test
    public void testMultinomialChain() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 10; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            //Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            //Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            //Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varB).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varC).addParent(classVar);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            //dynamicDAG.getParentSetTimeT(varB).addParent(varC);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i));

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(1, 10000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(1000);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));


            DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dynamicNB.getDynamicDAG(), data);

            System.out.println(watch.stop());
            System.out.println();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + bnet.getDistributionTimeT(var));
                assertTrue(bnet.getDistributionTimeT(var).equalDist(dynamicNB.getDistributionTimeT(var), 0.05));
            }


            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.05));
            }

        }
    }


    @Test
    public void testMultinomialChain2() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 10; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            //Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            //Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varB).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varC).addParent(classVar);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i));

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(1, 10000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(1000);
            MaximumLikelihoodForDBN.setParallelMode(true);

            Stopwatch watch = Stopwatch.createStarted();

            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));


            DynamicBayesianNetwork bnet = MaximumLikelihoodForDBN.learnDynamic(dynamicNB.getDynamicDAG(), data);

            System.out.println(watch.stop());
            System.out.println();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + bnet.getDistributionTimeT(var));
                assertTrue(bnet.getDistributionTimeT(var).equalDist(dynamicNB.getDistributionTimeT(var), 0.05));
            }


            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.05));
            }

        }
    }


    @Test
    public void testMultinomialChain3() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 5; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            //Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            //Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varB).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varC).addParent(classVar);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i));

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setMARVar(varA, 0.5);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(10, 10000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(1000);
            MaximumLikelihoodForDBN.setParallelMode(true);


            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));



            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(1000);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.06));
            }

        }
    }


    @Test
    public void testMultinomialChain4() throws IOException, ClassNotFoundException {

        for (int i = 0; i < 1; i++) {


            DynamicVariables dynamicVariables = new DynamicVariables();

            Variable varA = dynamicVariables.newMultinomialDynamicVariable("A",2);
            Variable varB = dynamicVariables.newMultinomialDynamicVariable("B",2);
            //Variable varC = dynamicVariables.newMultinomialDynamicVariable("C",2);

            //Variable varA = dynamicVariables.newGaussianDynamicVariable("A");
            //Variable varB = dynamicVariables.newGaussianDynamicVariable("B");
            //Variable varC = dynamicVariables.newGaussianDynamicVariable("C");

            DynamicDAG dynamicDAG = new DynamicDAG(dynamicVariables);

            //dynamicDAG.getParentSetTimeT(varA).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varB).addParent(classVar);
            //dynamicDAG.getParentSetTimeT(varC).addParent(classVar);

            //dynamicDAG.getParentSetTimeT(varA).addParent(varB);
            //dynamicDAG.getParentSetTimeT(varA).addParent(varC);
            dynamicDAG.getParentSetTimeT(varB).addParent(varA);



            dynamicDAG.getParentSetTimeT(varA).addParent(dynamicVariables.getTemporalClone(varA));
            //dynamicDAG.getParentSetTimeT(varB).addParent(dynamicVariables.getTemporalClone(varB));


            //The number of states for the class variable is equal to 2
            DynamicBayesianNetwork dynamicNB = DynamicBayesianNetwork.newDynamicBayesianNetwork(dynamicDAG);

            dynamicNB.randomInitialization(new Random(i));

            Multinomial_MultinomialParents distB = dynamicNB.getDistributionTimeT(varB);
            distB.getMultinomial(0).setProbabilities(new double[]{0.99, 0.01});
            distB.getMultinomial(1).setProbabilities(new double[]{0.01, 0.99});

            System.out.println(dynamicNB.getDynamicDAG().toString());
            System.out.println(dynamicNB.toString());


            //Sampling from the generated Dynamic NB
            DynamicBayesianNetworkSampler sampler = new DynamicBayesianNetworkSampler(dynamicNB);
            sampler.setMARVar(varA, 0.99);
            sampler.setSeed(0);

            //Sample from the dynamic NB given as inputs both nSequences (= 10000) and sequenceLength (= 100)

            DataStream<DynamicDataInstance> data = sampler.sampleToDataBase(1, 10000);


            //Structure learning is excluded from the test, i.e., we use directly the initial Dynamic Naive Bayes network structure
            // and just apply then test parameter learning

            //Parameter Learning
            MaximumLikelihoodForDBN.setBatchSize(1000);
            MaximumLikelihoodForDBN.setParallelMode(true);


            //data.stream().forEach(d -> System.out.println(d.getValue(varA) + ", "+ d.getValue(varB)));



            StreamingVariationalBayesVMPForDBN svb = new StreamingVariationalBayesVMPForDBN();
            svb.setWindowsSize(500);
            svb.setSeed(5);
            VMP vmp = svb.getPlateuVMPDBN().getVMPTimeT();
            vmp.setOutput(true);
            vmp.setTestELBO(true);
            vmp.setMaxIter(1000);
            vmp.setThreshold(0.0001);
            BayesianLearningEngineForDBN.setBayesianLearningAlgorithmForDBN(svb);

            BayesianLearningEngineForDBN.setDynamicDAG(dynamicNB.getDynamicDAG());
            BayesianLearningEngineForDBN.setDataStream(data);
            BayesianLearningEngineForDBN.runLearning();

            //System.out.println(svb.getPlateuVMPDBN().toStringTimeT());

            DynamicBayesianNetwork learnDBN = BayesianLearningEngineForDBN.getLearntDBN();

            //Check if the probability distributions of each node over both time 0 and T
            for (Variable var : dynamicNB.getDynamicVariables()) {
                System.out.println("\n---------- Variable " + var.getName() + " -----------");
                // time T
                System.out.println("\nTrue distribution at time T:\n" + dynamicNB.getDistributionTimeT(var));
                System.out.println("\nLearned distribution at time T:\n" + learnDBN.getDistributionTimeT(var));
                assertTrue(dynamicNB.getDistributionTimeT(var).equalDist(learnDBN.getDistributionTimeT(var), 0.05));
            }

        }
    }
}