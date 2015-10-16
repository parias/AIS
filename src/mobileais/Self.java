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
public class Self {

    private ArrayList<App> apps;

    public Self() {
        apps = new ArrayList();
    }

    public App getApp(int index) {
        return apps.get(index);
    }

    public void addApp(App app) {
        apps.add(app);
    }

    public int getSize() {
        return apps.size();
    }

}
