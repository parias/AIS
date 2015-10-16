/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobileais;

import java.util.ArrayList;

/**
 *
 * @author James
 */
public class Detector {
    
    private ArrayList<Double> values;
    
    public Detector() {
        values = new ArrayList();
    }
    
    public double getValue(int index) {
        return values.get(index);
    }
    
    public void addValue(double value) {
        values.add(value);
    }
    
    public int compareTo(App app, int r) {
        int count = 0;
        
        int index = 0;
        
        for(int i = 0; i < app.getSize(); i++) {
            if(app.getValue(i) >= this.getValue(index) && app.getValue(i) <= this.getValue(index+1)) {
                count++;
            }
            
            index += 2;
        }
        
        if(count >= r) {
            return 0;
        }
        else {
            return 1;
        }
    }
    
    public Detector splitUp(App app, ArrayList<Integer> max) {
        Detector detector = new Detector();
        
        int count = 0;
        for(int i=0; i < app.getSize(); i++) {
            if(app.getValue(i) >= this.getValue(count) && app.getValue(i) <= this.getValue(count+1)) {
                if(this.getValue(count+1) != 0) {
                    detector.addValue(app.getValue(i)+1);
                    detector.addValue(this.getValue(count+1));
                } else {
                    detector.addValue(app.getValue(i)+1);
                    detector.addValue(max.get(i));
                }
            } else {
                detector.addValue(this.getValue(count));
                detector.addValue(this.getValue(count+1));
            }
            count += 2;
        }
        return detector;
    }
    
    public Detector splitDown(App app) {
        Detector detector = new Detector();
        
        int count = 0;
        for(int i=0; i < app.getSize(); i++) {
            if(app.getValue(i) >= this.getValue(count) && app.getValue(i) <= this.getValue(count+1)) {
                if(app.getValue(i) != 0) {
                    detector.addValue(this.getValue(count));
                    detector.addValue(app.getValue(i)-1);
                } else {
                    detector.addValue(0);
                    detector.addValue(0);
                }
            } else {
                detector.addValue(this.getValue(count));
                detector.addValue(this.getValue(count+1));
            }
            count += 2;
        }
        return detector;
    }
    
    public Detector splitUpSTD(App app, ArrayList<Double> std, int T, ArrayList<Integer> max) {
        Detector detector = new Detector();
        
        int count = 0;
        for(int i=0; i < app.getSize(); i++) {
            if(app.getValue(i) >= this.getValue(count) && app.getValue(i) <= this.getValue(count+1)) {
                    double first = app.getValue(i)+std.get(i)*T;
                    double second = this.getValue(count+1);
                    if (first > second) {
                       double temp = first;
                       first = second;
                       second = temp;
                    }
                    
                    if (second > max.get(i)) {
                        second = max.get(i);
                    }
                    detector.addValue(first);
                    detector.addValue(second);
            } else {
                detector.addValue(this.getValue(count));
                detector.addValue(this.getValue(count+1));
            }
            count += 2;
        }
        return detector;
    }
    
    public Detector splitDownSTD(App app, ArrayList<Double> std, int T) {
        Detector detector = new Detector();
        
        int count = 0;
        for(int i=0; i < app.getSize(); i++) {
            if(app.getValue(i) >= this.getValue(count) && app.getValue(i) <= this.getValue(count+1)) {
                    double first = this.getValue(count);
                    double second = app.getValue(i)-std.get(i)*T;
                    if(second < first) {
                        double temp = first;
                        first = second;
                        second = temp;
                    }
                    
                    if (second < 0.0) {
                        second = 0;
                    }
                    detector.addValue(first);
                    detector.addValue(second);
            } else {
                detector.addValue(this.getValue(count));
                detector.addValue(this.getValue(count+1));
            }
            count += 2;
        }
        return detector;
    }
    
}
