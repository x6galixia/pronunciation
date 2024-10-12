package com.example.pronunciationchecker;

import java.util.List;

public class CorrectionResponse {
    public List<Result> data;

    public static class Result {
        public String generated_text;
    }
}
