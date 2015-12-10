/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package eu.amidst.core.inference.messagepassing;

import com.google.common.base.Stopwatch;
import eu.amidst.core.ModelFactory;
import eu.amidst.core.distribution.ConditionalDistribution;
import eu.amidst.core.distribution.UnivariateDistribution;
import eu.amidst.core.exponentialfamily.MomentParameters;
import eu.amidst.core.exponentialfamily.NaturalParameters;
import eu.amidst.core.inference.InferenceAlgorithm;
import eu.amidst.core.inference.InferenceEngine;
import eu.amidst.core.inference.Sampler;
import eu.amidst.core.io.BayesianNetworkLoader;
import eu.amidst.core.models.BayesianNetwork;
import eu.amidst.core.models.DAG;
import eu.amidst.core.variables.Variable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * This class extends the class {@link MessagePassingAlgorithm} and implements the interfaces {@link InferenceAlgorithm} and {@link Sampler}.
 * It handles and implements the Variational message passing (VMP) algorithm.
 * Winn, J.M., Bishop, C.M.: Variational message passing. Journal of Machine Learning Research 6 (2005) 661–694.
 *
 * <p> For an example of use follow this link
 * <a href="http://amidst.github.io/toolbox/CodeExamples.html#vmpexample"> http://amidst.github.io/toolbox/CodeExamples.html#vmpexample </a>  </p>
 */
public class VMP extends MessagePassingAlgorithm<NaturalParameters> implements InferenceAlgorithm, Sampler {

    /** Represents a test of the evidence lower bound (ELBO). */
    boolean testELBO=false;

    /**
     * Sets the testELBO value.
     * @param testELBO a {@code boolean} that represents the testELBO value to be set.
     */
    public void setTestELBO(boolean testELBO) {
        this.testELBO = testELBO;
    }

    /**
     * Returns the testELBO value.
     * @return the testELBO value.
     */
    public boolean getTestELBO() {
        return testELBO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<NaturalParameters> newSelfMessage(Node node) {
        Map<Variable, MomentParameters> momentParents = node.getMomentParents();
        Message<NaturalParameters> message = new Message(node);
        message.setVector(node.getPDist().getExpectedNaturalFromParents(momentParents));
        message.setDone(node.messageDoneFromParents());

        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message<NaturalParameters> newMessageToParent(Node child, Node parent) {
        Map<Variable, MomentParameters> momentChildCoParents = child.getMomentParents();

        Message<NaturalParameters> message = new Message<>(parent);
        message.setVector(child.getPDist().getExpectedNaturalToParent(child.nodeParentToVariable(parent), momentChildCoParents));
        message.setDone(child.messageDoneToParent(parent.getMainVariable()));

        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateCombinedMessage(Node node, Message<NaturalParameters> message) {
        node.getQDist().setNaturalParameters(message.getVector());
        node.setIsDone(message.isDone());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean testConvergence(){

        boolean convergence = false;
        //Compute lower-bound
        double newelbo = this.computeLogProbabilityOfEvidence();
        if (Math.abs(newelbo - local_elbo) < threshold) {
            convergence = true;
        }

        if (testELBO && (!convergence && (newelbo/nodes.size() < (local_elbo/nodes.size() - 0.01)) && local_iter>-1) || Double.isNaN(local_elbo)){
            throw new IllegalStateException("The elbo is not monotonically increasing at iter "+local_iter+": " + local_elbo/nodes.size() + ", "+ newelbo/nodes.size());
        }
        local_elbo = newelbo;
        return convergence;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double computeLogProbabilityOfEvidence(){
        return this.nodes.stream().filter(node-> node.isActive()).mapToDouble(node -> this.computeELBO(node)).sum();
    }

    /**
     * Computes the evidence lower bound (ELBO) for a given {@link Node}.
     * @param node a given {@link Node} object.
     * @return a {@code double} that represents the ELBO value.
     */
    public double computeELBO(Node node){

        Map<Variable, MomentParameters> momentParents = node.getMomentParents();

        double elbo=0;
        NaturalParameters expectedNatural = node.getPDist().getExpectedNaturalFromParents(momentParents);

        if (!node.isObserved()) {
            expectedNatural.substract(node.getQDist().getNaturalParameters());
            elbo += expectedNatural.dotProduct(node.getQDist().getMomentParameters());
            elbo -= node.getPDist().getExpectedLogNormalizer(momentParents);
            elbo += node.getQDist().computeLogNormalizer();
        }else {
            elbo += expectedNatural.dotProduct(node.getSufficientStatistics());
            elbo -= node.getPDist().getExpectedLogNormalizer(momentParents);
            elbo += node.getPDist().computeLogBaseMeasure(this.assignment);
        }

        if (elbo>0 && !node.isObserved() && Math.abs(expectedNatural.sum())<0.01) {
            elbo=0;
        }

        if (this.testELBO && ((elbo>2 && !node.isObserved()) || Double.isNaN(elbo))) {
            node.getPDist().getExpectedLogNormalizer(momentParents);
            throw new IllegalStateException("NUMERICAL ERROR!!!!!!!!: " + node.getMainVariable().getName() + ", " +  elbo + ", " + expectedNatural.sum());
        }

        return  elbo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BayesianNetwork getSamplingModel() {

        DAG dag = ModelFactory.newDAG(this.model.getVariables());

        List<ConditionalDistribution> distributionList =
                this.model.getVariables().getListOfVariables().stream()
                        .map(var -> (ConditionalDistribution)this.getPosterior(var))
                        .collect(Collectors.toList());

        return ModelFactory.newBayesianNetwork(dag, distributionList);
    }


    public static void main(String[] arguments) throws IOException, ClassNotFoundException {

        BayesianNetwork bn = BayesianNetworkLoader.loadFromFile("./networks/Munin1.bn");
        System.out.println(bn.getNumberOfVars());
        System.out.println(bn.getDAG().getNumberOfLinks());
        System.out.println(bn.getConditionalDistributions().stream().mapToInt(p->p.getNumberOfParameters()).max().getAsInt());

        VMP vmp = new VMP();
        InferenceEngine.setInferenceAlgorithm(vmp);
        Variable var = bn.getVariables().getVariableById(0);
        UnivariateDistribution uni = null;
        double avg  = 0;
        for (int i = 0; i < 20; i++)
        {
            Stopwatch watch = Stopwatch.createStarted();
            uni = InferenceEngine.getPosterior(var, bn);
            System.out.println(watch.stop());
            avg += watch.elapsed(TimeUnit.MILLISECONDS);
        }
        System.out.println(avg/20);
        System.out.println(uni);

    }

}