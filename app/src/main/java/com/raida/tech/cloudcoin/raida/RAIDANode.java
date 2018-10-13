package com.raida.tech.cloudcoin.raida;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RAIDANode {

    @Expose
    @SerializedName("raida_index")
    public int raida_index;
    @Expose
    @SerializedName("failsEcho")
    public boolean failsEcho;
    @Expose
    @SerializedName("failsDetect")
    public boolean failsDetect;
    @Expose
    @SerializedName("failsFix")
    public boolean failsFix;
    @Expose
    @SerializedName("failsTicket")
    public boolean failsTicket;
    @Expose
    @SerializedName("location")
    public String location;
    @Expose
    @SerializedName("urls")
    public NodeURL[] urls;
}
