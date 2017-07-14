package com.guardian.reader.rest;

import com.guardian.reader.rest.models.contents.HttpContentResponse;
import com.guardian.reader.rest.models.sections.HttpSectionResponse;

import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by john on 7/11/2017.
 */

public interface GuadianService
{
    @GET("/sections")
    Observable<HttpSectionResponse> getSectionNames(@Query(value = "sections", encoded = true) String sectionId,
                                                    @Query(value = "api-key", encoded = true) String apiKey);


    @GET("/{sectionId}")
    Observable<HttpContentResponse> getNewsContents(@Path("sectionId") String sectionId,
                                                    @Query("api-key") String apiKey);


}
