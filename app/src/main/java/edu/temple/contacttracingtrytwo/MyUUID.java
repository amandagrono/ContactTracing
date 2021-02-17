package edu.temple.contacttracingtrytwo;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class MyUUID implements Serializable {

    private Date date;
    private UUID uuid;

    public MyUUID(){



    }

    public void setDate(Date date) {
        this.date = date;
    }
    public void setUuid(UUID uuid){
        this.uuid = uuid;
    }

    public Date getDate() {
        return date;
    }
    public UUID getUuid(){
        return uuid;
    }
}
