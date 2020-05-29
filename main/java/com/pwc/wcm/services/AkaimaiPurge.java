package com.pwc.wcm.services;

import java.util.List;

import org.json.JSONObject;


/**
 * Created by Rui on 2016/11/28.
 */
public interface AkaimaiPurge {
    public void purge(List<String> urls);
    public void purge(JSONObject jsonObj);
}
