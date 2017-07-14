package com.guardian.reader.rest.models.contents;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.models.GuardianSection;

import java.util.List;

/**
 * Created by john on 7/11/2017.
 */

public class GuardianContentResponse
{
    @SerializedName("status")
    @Expose
    public String status;

    @SerializedName("userTier")
    @Expose
    public String userTier;

    @SerializedName("total")
    @Expose
    public int total;

    @SerializedName("results")
    @Expose
    public List<GuardianContent> results;

}
