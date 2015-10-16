/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileais;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author James
 */
public class MobileAIS {

    private static ArrayList<Integer> max;
    private static ArrayList<Double> std;
    private static Self self;
    private static NonSelf nonSelf;
    private static double[] data;
    private static int D;
    private static double W;
    private static double T;
    private static int r;
    private static String controlParam;
    private static double var;
    private static String results;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        max = new ArrayList();
        std = new ArrayList();
        self = new Self();
        nonSelf = new NonSelf();

        setD(1000);//Integer.parseInt(JOptionPane.showInputDialog("Number of detectors"));
        setW(1);//Integer.parseInt(JOptionPane.showInputDialog("Width of the detectors"));
        setT(1);//Integer.parseInt(JOptionPane.showInputDialog("Tao to be used"));
        setR(510);//Integer.parseInt(JOptionPane.showInputDialog("r value"));

        readSubjects();

        data = new double[self.getSize() + nonSelf.getSize()];

        calculateMaxSTD();

        //run(getD());
        try {
            controlParam = JOptionPane.showInputDialog("Control Param");
            if (controlParam.compareTo("w") == 0 || controlParam.compareTo("t") == 0) {
                var = Double.parseDouble(JOptionPane.showInputDialog("Ending value to be used"));
            } else {
                var = Integer.parseInt(JOptionPane.showInputDialog("Value to be used"));
            }
        } catch (NullPointerException e) {
            System.out.println("Did not supply values");
            System.exit(0);
        }

        variation(controlParam, var);
        printResults();

    }

    public static void readSubjects() {
        try {
            Scanner scan = new Scanner(new File("Dataset.txt"));
            int count = 0;
            while (scan.hasNext()) {
                String next = scan.nextLine();

                String[] line = next.split("\t");
                App app = new App();
                for (int i = 0; i < line.length; i++) {
                    if (i != 0) {
                        app.addValue((int) Double.parseDouble(line[i]));
                    }
                }

                if (line[0].endsWith("0")) {
                    self.addApp(app);
                } else {
                    nonSelf.addApp(app);
                }

                count++;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MobileAIS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void calculateMaxSTD() {
        for (int i = 0; i < self.getApp(0).getSize(); i++) {
            max.add(0);
        }

        for (int t = 0; t < self.getApp(0).getSize(); t++) {
            for (int i = 0; i < self.getSize() + nonSelf.getSize(); i++) {
                if (i < self.getSize()) {
                    if (self.getApp(i).getValue(t) > max.get(t)) {
                        max.set(t, self.getApp(i).getValue(t));
                    }
                    data[i] = self.getApp(i).getValue(t);
                } else {
                    if (nonSelf.getApp(i - self.getSize()).getValue(t) > max.get(t)) {
                        max.set(t, nonSelf.getApp(i - self.getSize()).getValue(t));
                    }
                    data[i] = nonSelf.getApp(i - self.getSize()).getValue(t);
                }
            }
            std.add(getStdDev());
        }
    }

    public static void run(double n) {

        Random rand = new Random();
        ArrayList<Detector> mature = new ArrayList();

        for (int i = 0; i < n; i++) {
            Detector detector = new Detector();

            for (int t = 0; t < nonSelf.getApp(0).getSize(); t++) {
                int initial = rand.nextInt(max.get(t));
                double first = initial - getW();
                double second = initial + getW();

                if (first < 0) {
                    first = 0;
                }

                if (second > max.get(t)) {
                    second = max.get(t);
                }

                detector.addValue(first);
                detector.addValue(second);

            }
            boolean matches = false;
            int position = 0;
            for (int t = 0; t < self.getSize() / 2; t++) {
                if (detector.compareTo(self.getApp(t), getR()) == 0) {
                    matches = true;

                    Detector upDetector = detector.splitUpSTD(self.getApp(t), std, getT(), max);
                    Detector downDetector = detector.splitDownSTD(self.getApp(t), std, getT());

                    boolean upMatch = false;
                    boolean downMatch = false;

                    for (int m = 0; m < self.getSize() / 2; m++) {
                        if (upDetector.compareTo(self.getApp(m), getR()) == 0) {
                            upMatch = true;
                        }

                        if (downDetector.compareTo(self.getApp(m), getR()) == 0) {
                            downMatch = true;
                        }

                    }

                    if (upMatch == false) {
                        mature.add(upDetector);
                    }

                    if (downMatch == false) {
                        mature.add(downDetector);
                    }

                }
            }
            if (matches == false) {
                mature.add(detector);
            }
        }

        int matureDetectors = mature.size();

        int[] detected = new int[nonSelf.getSize()];

        for (int i = 0; i < detected.length; i++) {
            detected[i] = 0;
        }

        for (int i = 0; i < mature.size(); i++) {
            for (int t = 0; t < nonSelf.getSize(); t++) {
                if (mature.get(i).compareTo(nonSelf.getApp(t), getR()) == 0 && detected[t] == 0) {
                    detected[t] = 1;
                }
            }
        }

        int count = 0;

        for (int i = 0; i < detected.length; i++) {
            if (detected[i] == 1) {
                count++;
            }
        }

        int appIdentifiedTotal = nonSelf.getSize();
        double accuracy = 100 * ((double) count / (double) nonSelf.getSize());

        for (int i = 0; i < detected.length; i++) {
            detected[i] = 0;
        }

        for (int i = 0; i < mature.size(); i++) {
            for (int t = self.getSize() / 2; t < self.getSize(); t++) {
                if (mature.get(i).compareTo(self.getApp(t), getR()) == 0 && detected[t] == 0) {
                    detected[t - self.getSize() / 2] = 1;
                }
            }
        }

        count = 0;

        for (int i = 0; i < self.getSize() / 2; i++) {
            if (detected[i] == 1) {
                count++;
            }
        }

        int numberAppsMatchedTotal = (self.getSize() - (self.getSize() / 2));
        double falsePositive = ((double) count / ((double) (self.getSize()) / 2)) * 100;

        output(matureDetectors, appIdentifiedTotal, accuracy, numberAppsMatchedTotal, falsePositive);

    }

    public static double getMean() {
        double sum = 0.0;
        double count = 0.0;
        for (double a : data) {
            if (a != 0.0) {
                sum += a;
                count++;
            }
        }
        return sum / count;
    }

    public static double getVariance() {
        double mean = getMean();
        double temp = 0.0;
        double count = 0.0;
        for (double a : data) {
            if (a != 0.0) {
                temp += (mean - a) * (mean - a);
                count++;
            }
        }
        return temp / count;
    }

    public static double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public static void variation(String controlParams, double vars) {
        System.out.println("Varying " + controlParams);
        if (controlParams.compareTo("w") == 0) {
            int stop = (int) vars * 100;
            for (int i = 0; i < stop; i++) {
                double width = i * 0.01;
                System.out.println(width);
                setW(width);
                run(getD());
            }

        } else if (controlParams.compareTo("d") == 0) {
            for (int i = 0; i < vars; i++) {
                setD(i);
                System.out.println("d: " + getD());
                run(getD());
            }

        } else if (controlParams.compareTo("r") == 0) {
            for (int i = 460; i < vars; i += 20) {
                setR(i);
                System.out.println("r: " + getR());
                run(getD());
            }
        } else if (controlParams.compareTo("t") == 0) {
            int stop = (int) vars * 2;
            for (int i = 0; i <= stop; i++) {
                double tao = i * 0.5;
                setT(tao);
                System.out.println("t: " + tao);
                run(getD());
            }
        }
    }

    /**
     * @return the D
     */
    public static int getD() {
        return D;
    }

    /**
     * @param aD the D to set
     */
    public static void setD(int aD) {
        D = aD;
    }

    /**
     * @return the W
     */
    public static double getW() {
        return W;
    }

    /**
     * @param aW the W to set
     */
    public static void setW(double aW) {
        W = aW;
    }

    /**
     * @return the T
     */
    public static double getT() {
        return T;
    }

    /**
     * @param aT the T to set
     */
    public static void setT(double aT) {
        T = aT;
    }

    /**
     * @return the r
     */
    public static int getR() {
        return r;
    }

    /**
     * @param aR the r to set
     */
    public static void setR(int aR) {
        r = aR;
    }

    private static void output(int matureDetectors, int appIdentifiedTotal, double accuracy, int numberAppsMatchedTotal, double falsePositive) {
        System.out.println(matureDetectors + " " + appIdentifiedTotal + " " + accuracy + " " + numberAppsMatchedTotal + " " + falsePositive);

        //For printResults();
        results += " w: " + getW() + " " + matureDetectors + " " + appIdentifiedTotal + " " + accuracy + " " + numberAppsMatchedTotal + " " + falsePositive + "\n";
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static void printResults() {

        try (PrintWriter write = new PrintWriter("Results.txt")) {
            write.printf(results);

            write.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
