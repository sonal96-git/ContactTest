package com.pwc.actueel.xml.adapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * Adapter to handle transformation of category list into a String of values separated by '|'.
 */
public class CategoryListAdapter extends XmlAdapter<String, List<String>> {
    
    private final String CATEGORY_SEPARATOR = "|";

    @Override
    public List<String> unmarshal(final String v) throws Exception {
        return v == null || v.isEmpty() ? new ArrayList<String>() : Arrays.asList(v.split(CATEGORY_SEPARATOR));
    }

    @Override
    public String marshal(final List<String> v) throws Exception {
        return v == null || v.isEmpty() ? "" : StringUtils.join(v, CATEGORY_SEPARATOR);
    }
}
