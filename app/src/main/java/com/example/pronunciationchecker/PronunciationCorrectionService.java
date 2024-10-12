package com.example.pronunciationchecker;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PronunciationCorrectionService {
    private static final String BASE_URL = "https://api-inference.huggingface.co/";

    public static PronunciationCorrectionAPI create() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(PronunciationCorrectionAPI.class);
    }
}
