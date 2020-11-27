package com.tandg.labourscanningapp.webservice;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface SpreadsheetWebService {

    @POST("1FAIpQLSfwOMcQRN279wGnp9m9iQQmTNUta2jX9SBHgUG9KBpz22rsqg/formResponse")
    @FormUrlEncoded
    Call<Void> submitTime(
            @Field("entry.1376490372") String houseRowNumber,
            @Field("entry.1475615516") String startTime,
            @Field("entry.72760431") String endTime

    );

}
