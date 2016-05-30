package com.fazland.recommenders.datascorer;

import org.apache.log4j.BasicConfigurator;

/**
 * Created by federicopanini on 26/05/16.
 */
public class DataScorerEvaluatorTestRunner {
    public static void main (String[] args) throws Exception {

        double[] neiborhoods = {1,2,4,8,16,32,64,128,512};
        double[] threshold = {0.98, 0.9, 0.85, 0.8, 075, 0.7};
        DataScorer a = new DataScorer("./profession_user.csv", neiborhoods, threshold);

        System.out.println("*********");
        a.doBoolItemTanimotoScore();
        a.doBoolItemLogLikelyhoodScore();
        System.out.println("*********");
    }

    private void generateGraph() {}
    private void generateCsvResults() {}
}
