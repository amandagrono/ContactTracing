package edu.temple.contacttracingtrytwo;

import java.io.Serializable;

public interface DashboardButtonListener  extends Serializable {
    public void onStartTracking();
    public void onStopTracking();

}
