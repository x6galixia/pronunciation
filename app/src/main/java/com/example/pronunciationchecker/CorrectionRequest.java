package com.example.pronunciationchecker;

public class CorrectionRequest {
    private String[] inputs;

    public CorrectionRequest(String text) {
        this.inputs = new String[]{text};
    }

    public String[] getInputs() {
        return inputs;
    }

    public void setInputs(String[] inputs) {
        this.inputs = inputs;
    }
}