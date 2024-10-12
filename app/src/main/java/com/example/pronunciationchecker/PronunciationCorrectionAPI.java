package com.example.pronunciationchecker;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import java.util.List;

public interface PronunciationCorrectionAPI {
    @POST("models/grammarly/coedit-large")
    Call<List<CorrectionResponse.Result>> correctPronunciation(
            @Header("Authorization") String token,
            @Body CorrectionRequest request
    );
}