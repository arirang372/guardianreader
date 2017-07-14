package com.guardian.reader.rest.models.contents;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.guardian.reader.models.GuardianContent;
import com.guardian.reader.rest.models.sections.GuardianSectionResponse;


/**
 * Created by john on 7/12/2017.
 */

public class HttpContentResponse
{
    @SerializedName("response")
    @Expose
    public GuardianContentResponse response;



}
