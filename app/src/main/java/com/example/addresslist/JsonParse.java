package com.example.addresslist;

import com.example.addresslist.entity.SimplyContector;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class JsonParse {
    public static List<SimplyContector> showAllContactors(String json){
        Gson gson = new Gson();
        Type listType = new TypeToken<List<SimplyContector>>(){}.getType();
        List<SimplyContector> simplyContectors=gson.fromJson(json,listType);
        return simplyContectors;
    }
}
