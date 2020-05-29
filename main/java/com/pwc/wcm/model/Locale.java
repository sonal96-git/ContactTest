package com.pwc.wcm.model;

import java.util.ArrayList;

/**
 * Created by rjiang022 on 6/12/2015.
 */
public class Locale {
    private String locale;
    private ArrayList<Microsite> microsites;

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public ArrayList<Microsite> getMicrosites() {
        return microsites;
    }

    public void setMicrosites(ArrayList<Microsite> microsites) {
        this.microsites = microsites;
    }
}
