/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package standardais;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Owner
 */
public abstract class AIS {

    ArrayList<Double> max;
    ArrayList<Double> min;
    ArrayList<Double> std_dev;
    ArrayList<Detector> mature;
    ArrayList<App> self;
    ArrayList<App> non_self;
    ArrayList<App> fp_self;
    int D;
    double W;
    int T;
    int r;
    String dataFile;
    String output;
    final int SELF_VAL = 10;
    JTextArea outputTextArea;
    boolean split;

    public AIS(int D, double W, int T, String dataFile, JTextArea outputTextArea) {
        this.D = D;
        this.W = W;
        this.T = T;
        this.dataFile = dataFile;
        this.outputTextArea = outputTextArea;
        split = true;
    }

    public AIS(int D, double W, String dataFile, JTextArea outputTextArea) {
        this.D = D;
        this.W = W;
        this.dataFile = dataFile;
        this.outputTextArea = outputTextArea;
        split = false;
    }

    public void runSystem() {
        max = new ArrayList();
        std_dev = new ArrayList();
        mature = new ArrayList();
        self = new ArrayList();
        fp_self = new ArrayList();
        non_self = new ArrayList();

        readInstances();
        calculateMax();
        calculateStdDev();
        setSelfValidation();
        if (split) {
            output = non_self.size() + "\t" + fp_self.size() + "\t" + D + "\t" + W + "\t" + T + "\t" + r + "\t";
        } else {
            output = non_self.size() + "\t" + fp_self.size() + "\t" + D + "\t" + W + "\t" + r + "\t";
        }
        generateDetectors();
        validateNonSelf();
        validateSelf();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                outputTextArea.append(output + "\n");
            }
        });
    }

    //Read in the instances from the dataset text file
    public void readInstances() {
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(dataFile));
            while ((line = br.readLine()) != null) {

                String[] feature = line.split(csvSplitBy);
                App app = new App();

                //@TODO change this
                boolean works = true;
                for (int i = 0; i < feature.length; i++) {
                    //if(i != 0 && i != 1) {
                    //System.out.println(feature[i]);
                    if (isNumeric(feature[i])) {
                        app.add(Double.parseDouble(feature[i]));
                    } else {
                        app.add(0.0);
                        works = false;
                    }
                    //}
                }
                /*
                if(line[0].endsWith("0")) {
                    self.add(app);
                }
                else {
                    non_self.add(app);
                }
                 */
                //P.A.A
                if (self.size() < 30 && works == true) {
                    self.add(app);
                } else if (works == true) {
                    non_self.add(app);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MobileAIS.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AIS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Calculate the max for each feature to be used during detector generation
    public void calculateMax() {
        for (int i = 0; i < self.get(0).size(); i++) {
            max.add(Double.MIN_VALUE);
            min.add(Double.MAX_VALUE);
        }

        for (int t = 0; t < self.get(0).size(); t++) {
            for (int i = 0; i < self.size() + non_self.size(); i++) {
                if (i < self.size()) {
                    if (self.get(i).get(t) > max.get(t)) {
                        max.set(t, self.get(i).get(t));
                    }else if(self.get(i).get(t) < min.get(t)){
                        min.set(t, self.get(i).get(t));
                    }
                } else if (non_self.get(i - self.size()).get(t) > max.get(t)) {
                    max.set(t, non_self.get(i - self.size()).get(t));
                }
            }
        }
    }

    //Calculate the standard deviation for each feature to be used during the detector splits
    public void calculateStdDev() {
        int totalInstances = self.size() + non_self.size();

        double[] features = new double[totalInstances];

        for (int i = 0; i < self.get(0).size(); i++) {
            for (int n = 0; n < self.size(); n++) {
                features[n] = self.get(n).get(i);
            }

            for (int n = 0; n < non_self.size(); n++) {
                features[n + self.size()] = non_self.get(n).get(i);
            }

            std_dev.add(getStdDev(features));
        }
    }

    private double getStdDev(double[] features) {
        return Math.sqrt(getVariance(features));
    }

    private double getVariance(double[] features) {
        double mean = getMean(features);
        double temp = 0;
        int count = 0;
        for (double a : features) {
            if (a != 0.0) {
                temp += (mean - a) * (mean - a);
                count++;
            }
        }
        return temp / count;
    }

    private double getMean(double[] features) {
        double sum = 0.0;
        int count = 0;
        for (double a : features) {
            if (a != 0.0) {
                sum += a;
                count++;
            }
        }
        return sum / count;
    }

    //Transfer self instances from the self set to the false positive self set for later validation
    private void setSelfValidation() {
        for (int i = 0; i < SELF_VAL; i++) {
            fp_self.add(self.remove(self.size() - 1));
        }
    }

    abstract void generateDetectors();

    abstract void validateNonSelf();

    abstract void validateSelf();

    private void printResults() {
        System.out.println(output);
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * @return the D
     */
    public int getD() {
        return D;
    }

    /**
     * @param D the D to set
     */
    public void setD(int D) {
        this.D = D;
    }

    /**
     * @return the W
     */
    public double getW() {
        return W;
    }

    /**
     * @param W the W to set
     */
    public void setW(double W) {
        this.W = W;
    }

    /**
     * @return the T
     */
    public int getT() {
        return T;
    }

    /**
     * @param T the T to set
     */
    public void setT(int T) {
        this.T = T;
    }

    /**
     * @return the r
     */
    public int getR() {
        return r;
    }

    /**
     * @param r the r to set
     */
    public void setR(int r) {
        this.r = r;
    }
}
