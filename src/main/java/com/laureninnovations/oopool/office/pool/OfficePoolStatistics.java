package com.laureninnovations.oopool.office.pool;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class OfficePoolStatistics {
    @Expose
    private List<OfficeInstance.Statistics> officeInstanceStats = new ArrayList<OfficeInstance.Statistics>();

    public void addOfficeInstance(OfficeInstance instance) {
        officeInstanceStats.add(instance.getStatistics());
    }
}
