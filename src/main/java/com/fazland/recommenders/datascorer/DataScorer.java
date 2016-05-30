package com.fazland.recommenders.datascorer;

import io.bretty.console.table.Alignment;
import io.bretty.console.table.ColumnFormatter;
import io.bretty.console.table.Precision;
import io.bretty.console.table.Table;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.DataModelBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.eval.AverageAbsoluteDifferenceRecommenderEvaluator;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.*;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by federicopanini on 26/05/16.
 */
public class DataScorer {

    /**
     * setup to use Log4j
     */
    private static final Logger log = LoggerFactory.getLogger(DataScorer.class);

    /**
     * double neighboorhoods;
     */
    private double[] neighborhoods;

    /**
     * double threshold
     */
    private double[] threshold;

    /**
     * String[] similarityClasses
     */
    private String[] similarityClasses = {"PearsonCorrelationSimilarity", "EuclideanDistanceSimilarity","TanimotoCoefficientSimilarity", "LogLikelihoodSimilarity"};

    /**
     * String booleanSimilarityClasses
     */
    private String[] booleanSimilarityClasses = {"TanimotoCoefficientSimilarity", "LogLikelihoodSimilarity"};

    /**
     * percentage of each user's preferences to use to produce recommendations; the rest are compared
     * to estimated preference values to evaluate. Tge evaluationPercentage is defined by 100-trainingPercentage
     */
    private double[][] trainingEvaluationPercentage;

    /**
     * final class to use/reference it inside anonymous functions
     *
     * DataModel model
     */
    final DataModel model;

    /**
     * This constructor is used for implicit ratings (boolean ratings)
     * @param ratingFile
     * @throws Exception
     */
    public DataScorer(String ratingFile) throws Exception {
        this.model = new FileDataModel(new File(ratingFile));
        this.setUp();
    }

    /**
     * This constructor is used for explicit rating
     *
     * @param ratingFile file path containing ratings
     * @param neighborhoods array of int containig the neighboorhood to evaluate
     * @param threshold threshold to substitute neighborhood evaluation
     * @throws Exception
     */
    public DataScorer(String ratingFile, double[] neighborhoods, double[] threshold) throws Exception {
        this.neighborhoods = neighborhoods;
        this.threshold = threshold;
        this.model = new FileDataModel(new File(ratingFile));
        this.setUp();
    }

    /**
     * micro setup environment
     *
     * @throws TasteException
     */
    private void setUp() throws TasteException{
        log.info("Total Number of Users : " + this.model.getNumUsers());
        log.info("Total Number of Item : " + this.model.getNumItems());

        //define training and evaluation percentage
        this.trainingEvaluationPercentage = new double[][] {
                {10,90},
                {15,85},
                {20,80},
                {25,75},
                {30,70},
                {35,65},
                {40,60},
                {45,55},
                {50,50},
                {55,45},
                {60,40},
                {65,35},
                {70,30},
                {75,25},
                {80,20},
                {85,15},
                {90,10},
                {95,5},
        };
    }

    /**
     * Generate ImplicitFeedback evaluation with Tanimoto Similarity algorithm
     * @throws Exception
     */
    public void doBoolItemTanimotoScore() throws  Exception{
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

        String[] headers = new String[this.trainingEvaluationPercentage.length];
        Number[] data = new Number[this.trainingEvaluationPercentage.length];
        double lowest = 0;
        int index = 0;

        for (int i=0;i<this.trainingEvaluationPercentage.length;i++) {
            RecommenderBuilder recommenderBuilder = null;
            recommenderBuilder = new RecommenderBuilder() {
                public Recommender buildRecommender(DataModel model) throws TasteException {
                    ItemSimilarity similarity = new TanimotoCoefficientSimilarity(model);

                    return new GenericBooleanPrefItemBasedRecommender(model, similarity);
                }
            };

            DataModelBuilder modelBuilder = new DataModelBuilder() {
                public DataModel buildDataModel(FastByIDMap<PreferenceArray> trainingData) {
                    return new GenericBooleanPrefDataModel(
                            GenericBooleanPrefDataModel.toDataMap(
                                    trainingData));
                }
            };

            double score = evaluator.evaluate(
                    recommenderBuilder, modelBuilder, model, (this.trainingEvaluationPercentage[i][0])/100, (this.trainingEvaluationPercentage[i][1])/100
            );

            headers[i] = String.valueOf(this.trainingEvaluationPercentage[i][0]) + " : " + String.valueOf(this.trainingEvaluationPercentage[i][1]);
            data[i] = score;
            if ((lowest == 0) || ((0 != lowest) && (score < lowest))) {
                lowest = score;
                index = i;
            }
        }

        headers[index] = headers[index] + "  *** -->";
        ColumnFormatter<String> head = ColumnFormatter.text(Alignment.CENTER,30);
        ColumnFormatter<Number> dat = ColumnFormatter.number(Alignment.RIGHT, 12, Precision.THREE);

        Table.Builder builder = new Table.Builder("Train / Eval", headers, head);
        builder.addColumn("Score", data, dat);
        Table table = builder.build();
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println(table);
    }

    /**
     * Generate ImplicitFeedback evaluation with LogLikelihood Similarity algorithm
     * @throws Exception
     */
    public void doBoolItemLogLikelyhoodScore() throws  Exception{
        RecommenderEvaluator evaluator = new AverageAbsoluteDifferenceRecommenderEvaluator();

        String[] headers = new String[this.trainingEvaluationPercentage.length];
        Number[] data = new Number[this.trainingEvaluationPercentage.length];
        double lowest = 0;
        int index = 0;

        for (int i=0;i<this.trainingEvaluationPercentage.length;i++) {
            RecommenderBuilder recommenderBuilder = null;
            recommenderBuilder = new RecommenderBuilder() {
                public Recommender buildRecommender(DataModel model) throws TasteException {
                    ItemSimilarity similarity = new LogLikelihoodSimilarity(model);

                    return new GenericBooleanPrefItemBasedRecommender(model, similarity);
                }
            };

            DataModelBuilder modelBuilder = new DataModelBuilder() {
                public DataModel buildDataModel(FastByIDMap<PreferenceArray> trainingData) {
                    return new GenericBooleanPrefDataModel(
                            GenericBooleanPrefDataModel.toDataMap(
                                    trainingData));
                }
            };

            double score = evaluator.evaluate(
                    recommenderBuilder, modelBuilder, model, (this.trainingEvaluationPercentage[i][0])/100, (this.trainingEvaluationPercentage[i][1])/100
            );

            headers[i] = String.valueOf(this.trainingEvaluationPercentage[i][0]) + " : " + String.valueOf(this.trainingEvaluationPercentage[i][1]);
            data[i] = score;
            if ((lowest == 0) || ((0 != lowest) && (score < lowest))) {
                lowest = score;
                index = i;
            }
        }

        headers[index] = headers[index] + "  *** -->";
        ColumnFormatter<String> head = ColumnFormatter.text(Alignment.CENTER,30);
        ColumnFormatter<Number> dat = ColumnFormatter.number(Alignment.RIGHT, 12, Precision.THREE);

        Table.Builder builder = new Table.Builder("Train / Eval", headers, head);
        builder.addColumn("Score", data, dat);
        Table table = builder.build();
        System.out.println("");
        System.out.println("");
        System.out.println("");
        System.out.println(table);
    }

}

