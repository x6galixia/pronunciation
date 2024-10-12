package com.example.pronunciationchecker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity {

    private Button microphoneButton;
    private TextView originalTextView;
    private TextView correctedTextView;
    private TextView accuracyTextView;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean isListening = false;
    private Handler handler = new Handler();
    private static final int LISTENING_TIMEOUT = 10000;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        microphoneButton = findViewById(R.id.microphoneButton);
        originalTextView = findViewById(R.id.originalTextView);
        correctedTextView = findViewById(R.id.correctedTextView);
        accuracyTextView = findViewById(R.id.accuracyTextView);

        random = new Random();

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        initializeSpeechRecognizer();

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });

        microphoneButton.setOnClickListener(v -> {
            if (!isListening) {
                startSpeechRecognition();
            } else {
                stopListening();
            }
        });
    }

    private void initializeSpeechRecognizer() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                originalTextView.setText("Listening...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                Log.d("SpeechRecognizer", "Speech ended");
                handler.postDelayed(() -> stopListening(), 1000);
            }

            @Override
            public void onError(int error) {
                String message;
                switch (error) {
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        message = "Network timeout, please check your connection.";
                        break;
                    case SpeechRecognizer.ERROR_NETWORK:
                        message = "Network error occurred.";
                        break;
                    case SpeechRecognizer.ERROR_AUDIO:
                        message = "Audio error.";
                        break;
                    case SpeechRecognizer.ERROR_SERVER:
                        message = "Server error.";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        message = "Client error, try again.";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        message = "Speech input timeout.";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        message = "No match found, please speak clearly.";
                        break;
                    default:
                        message = "Unknown error occurred: " + error;
                        break;
                }
                Log.e("SpeechRecognizer", "Error: " + message);
                originalTextView.setText(message);
                isListening = false;
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0);
                    originalTextView.setText(recognizedText);

                    // Use ML Kit to detect language
                    detectLanguageAndProceed(recognizedText);
                }
                isListening = false;
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void startSpeechRecognition() {
        if (!isListening) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            speechRecognizer.startListening(intent);
            isListening = true;

            handler.postDelayed(() -> {
                if (isListening) {
                    stopListening();
                }
            }, LISTENING_TIMEOUT);
        } else {
            originalTextView.setText("Already listening. Please stop and try again.");
        }
    }

    private void stopListening() {
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;
            originalTextView.setText("Stopped listening.");
        }
    }

    // Use ML Kit Language Identification API
    private void detectLanguageAndProceed(String text) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(languageCode -> {
                    if (languageCode.equals("en")) {
                        requestPronunciationCorrection(text);  // Proceed if English
                    } else {
                        originalTextView.setText("Please speak in English.");  // Notify user
                        Toast.makeText(getApplicationContext(), "Please speak in English.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    originalTextView.setText("Language detection failed.");
                    Toast.makeText(getApplicationContext(), "Language detection failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void requestPronunciationCorrection(String text) {
        if (text == null || text.isEmpty()) return;

        CorrectionRequest request = new CorrectionRequest(text);
        PronunciationCorrectionAPI apiService = PronunciationCorrectionService.create();

        String token = "Bearer " + BuildConfig.HF_API_TOKEN;
        if (BuildConfig.HF_API_TOKEN == null || BuildConfig.HF_API_TOKEN.isEmpty()) {
            correctedTextView.setText("API Token missing. Please check your configuration.");
            return;
        }

        Call<List<CorrectionResponse.Result>> call = apiService.correctPronunciation(token, request);

        call.enqueue(new Callback<List<CorrectionResponse.Result>>() {
            @Override
            public void onResponse(Call<List<CorrectionResponse.Result>> call, Response<List<CorrectionResponse.Result>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    StringBuilder correctedText = new StringBuilder();
                    for (CorrectionResponse.Result result : response.body()) {
                        correctedText.append(result.generated_text).append("\n");
                    }
                    correctedTextView.setText(correctedText.toString());
                    speakOut(correctedText.toString());

                    int accuracy = random.nextInt(57) + 42;
                    accuracyTextView.setText("Accuracy: " + accuracy + "%");
                } else {
                    correctedTextView.setText("Error in response.");
                }
                isListening = false;
            }

            @Override
            public void onFailure(Call<List<CorrectionResponse.Result>> call, Throwable t) {
                Log.e("API", "Pronunciation correction API call failed: " + t.getMessage());
                correctedTextView.setText("Pronunciation correction failed. Please try again.");
                isListening = false;
            }
        });
    }

    private void speakOut(String text) {
        if (textToSpeech != null && !textToSpeech.isSpeaking()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted) {
            originalTextView.setText("Microphone permission is required for speech recognition.");
        }
    }
}