package com.raida.tech.raida;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Network {

    @Expose
    @SerializedName("nn")
    public int nn;
    @Expose
    @SerializedName("raida")
    public RAIDANode[] raida;
}
