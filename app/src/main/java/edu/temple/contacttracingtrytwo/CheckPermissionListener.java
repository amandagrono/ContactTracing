package edu.temple.contacttracingtrytwo;

public interface CheckPermissionListener {
    public static final int REQUEST_ID = 1;
    public void acquirePermission();
    public boolean hasPermission();
}
