package com.example.skb.farmingassistance;


import com.example.skb.farmingassistance.FiveWeathers;

import java.util.List;

public class Forecast {

    private List<FiveWeathers> list;

    public Forecast(List<FiveWeathers> list) {
        this.list = list;
    }

    public List<FiveWeathers> getList() {
        return list;
    }
}
