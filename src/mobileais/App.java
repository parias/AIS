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
public class App {

    private ArrayList<Integer> values;

    public App() {
        values = new ArrayList();
    }

    public void addValue(int value) {
        values.add(value);
    }

    public int getValue(int index) {
        return values.get(index);
    }

    public int getSize() {
        return values.size();
    }
}
