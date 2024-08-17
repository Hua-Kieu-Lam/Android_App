package com.example.android_app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatAIActivity extends AppCompatActivity {

    private TextInputEditText queryEdt;
    private ScrollView chatScrollView;
    private LinearLayout chatContainer;
    private ImageButton sendButton;
    private ImageButton stopButton;
    private ImageButton attachButton;
    private String url = "https://yescale.one/v1/chat/completions";
    private JsonObjectRequest currentRequest;
    private RequestQueue requestQueue;
    private ImageButton opencamButton;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private ImageButton voiceInputButton;
    private final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;
    private static final int REQUEST_CODE_SPEECH_INPUT = 1; // Add this line


    // Variable to store loaded file content
    private String trainingData = "";

    // Hugging Face API URL
    private final String imageAnalysisApiUrl = "https://api-inference.huggingface.co/models/google/vit-base-patch16-224";

    // Hugging Face API Key
    private final String huggingFaceApiKey = "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";

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        addImageToChat(bitmap, true);
                        analyzeImageWithHuggingFace(bitmap); // Analyze the image using Hugging Face API
                        //analyzeFaceDetailsWithHuggingFace(bitmap);
                    } catch (Exception e) {
                        Log.e("IMAGE_ERROR", "Error converting image: " + e.getMessage());
                        Toast.makeText(this, "Error converting image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private final ActivityResultLauncher<String[]> selectFileLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    handleSelectedFile(uri);
                }
            });

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatai);

        queryEdt = findViewById(R.id.idEdtQuery);
        chatScrollView = findViewById(R.id.idSVChat);
        chatContainer = findViewById(R.id.idLLChatContainer);
        sendButton = findViewById(R.id.idBtnSend);
        stopButton = findViewById(R.id.idBtnStop);
        attachButton = findViewById(R.id.idBtnAttachFile);
        voiceInputButton = findViewById(R.id.idBtnVoiceInput);
        opencamButton = findViewById(R.id.idBtnCamera);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Display greeting message when app opens
        addMessageToChat("Xin chào, tôi có thể giúp gì cho bạn?", false);

        queryEdt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });

        opencamButton.setOnClickListener(v -> {
            // Khởi tạo Intent để mở CameraActivity
            Intent intent = new Intent(ChatAIActivity.this, CameraActivity.class);
            startActivity(intent);
        });

        sendButton.setOnClickListener(v -> sendMessage());

        stopButton.setOnClickListener(v -> stopRequest());

        attachButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Chọn loại tệp")
                    .setItems(new String[]{"Hình ảnh", "Tệp văn bản"}, (dialog, which) -> {
                        if (which == 0) {
                            selectImageLauncher.launch("image/*");
                        } else {
                            selectFileLauncher.launch(new String[]{"text/plain"});
                        }
                    })
                    .show();
        });

        voiceInputButton.setOnClickListener(v -> startVoiceInput());


    }


    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói điều gì đó...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Thiết bị của bạn không hỗ trợ nhập liệu bằng giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                queryEdt.setText(result.get(0));
                sendMessage(); // Automatically send the message after setting the text
            }
        }
    }




    private void sendMessage() {
        String userQuery = queryEdt.getText().toString();
        if (!userQuery.isEmpty()) {
            addMessageToChat(userQuery, true);
            getResponse(userQuery);
        } else {
            Toast.makeText(this, "Vui lòng nhập câu hỏi của bạn...", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRequest() {
        if (currentRequest != null) {
            currentRequest.cancel();
            Toast.makeText(this, "Yêu cầu đã dừng", Toast.LENGTH_SHORT).show();
        }
    }

    private void getResponse(String query) {
        queryEdt.setText("");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("model", "gpt-3.5-turbo");
            JSONArray messagesArray = new JSONArray();

            // Add training data as context
            if (!trainingData.isEmpty()) {
                JSONObject contextObject = new JSONObject();
                contextObject.put("role", "system");
                contextObject.put("content", "Training data: " + trainingData);
                messagesArray.put(contextObject);
            }

            // Add user query
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", query);
            messagesArray.put(messageObject);

            jsonObject.put("messages", messagesArray);
            jsonObject.put("temperature", 0.7);  // Adjust temperature for more human-like responses
            jsonObject.put("max_tokens", 300);   // Increase max tokens for longer responses
            jsonObject.put("top_p", 1);
            jsonObject.put("frequency_penalty", 0.0);
            jsonObject.put("presence_penalty", 0.0);
        } catch (Exception e) {
            Log.e("JSON_ERROR", "Error creating JSON object: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON object", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    try {
                        Log.d("API_RESPONSE", response.toString());
                        JSONArray choicesArray = response.getJSONArray("choices");
                        JSONObject firstChoice = choicesArray.getJSONObject(0);
                        String responseMsg = firstChoice.getJSONObject("message").getString("content");

                        addMessageToChat(responseMsg, false);
                    } catch (Exception e) {
                        Log.e("API_RESPONSE", "Error parsing response: " + e.getMessage());
                        Toast.makeText(ChatAIActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("API_ERROR", "Error: " + error.getMessage() + "\n" + error);
                    Toast.makeText(ChatAIActivity.this, "Request error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer sk-MHWxdWXl8J4MOcrE91630c16292746CaA356Aa376667AdC1");
                return headers;
            }
        };

        postRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) {
                // Do nothing
            }
        });

        currentRequest = postRequest;
        requestQueue.add(postRequest);
    }

    private void handleSelectedFile(Uri uri) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
            reader.close();

            trainingData = text.toString(); // Save the content to memory
            addMessageToChat("Dữ liệu huấn luyện đã được tải.", true);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Không thể đọc tệp", Toast.LENGTH_SHORT).show();
        }
    }

    private void addImageToChat(Bitmap bitmap, boolean isUser) {
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = isUser ? android.view.Gravity.END : android.view.Gravity.START;
        imageView.setLayoutParams(layoutParams);

        chatContainer.addView(imageView);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void addMessageToChat(String message, boolean isUser) {
        TextView messageTextView = new TextView(this);
        messageTextView.setText(message);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = isUser ? android.view.Gravity.END : android.view.Gravity.START;
        messageTextView.setLayoutParams(layoutParams);
        messageTextView.setBackgroundResource(isUser ? R.drawable.user_message_background : R.drawable.bot_message_background);
        messageTextView.setTextColor(getResources().getColor(android.R.color.black));
        messageTextView.setPadding(16, 16, 16, 16);
        messageTextView.setTextSize(16);

        chatContainer.addView(messageTextView);
        chatScrollView.post(() -> chatScrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void analyzeImageWithHuggingFace(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("inputs", encodedImage);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, imageAnalysisApiUrl,
                    response -> {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                JSONObject firstResult = jsonArray.getJSONObject(0);
                                // Check for face detection results first
                                if (firstResult.has("face_detected")) {
                                    boolean faceDetected = firstResult.getBoolean("face_detected");
                                    addMessageToChat(faceDetected ? "Face detected in the image." : "No face detected in the image.", false);
                                } else if (firstResult.has("label")) {
                                    String label = firstResult.getString("label");
                                    // Send the label to ChatGPT for more information
                                    getResponse("Thông tin về  " + label + "?");
                                } else if (firstResult.has("description")) {
                                    String description = firstResult.getString("description");
                                    addMessageToChat("Description: " + description, false);
                                } else {
                                    addMessageToChat("Unable to analyze image. No recognizable data found.", false);
                                }
                            } else {
                                addMessageToChat("Không thể nhận diện hình ảnh này.", false);
                            }
                        } catch (Exception e) {
                            Log.e("HUGGINGFACE_ERROR", "Error parsing response: " + e.getMessage());
                            Toast.makeText(ChatAIActivity.this, "Error parsing image analysis response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 503) {
                                Toast.makeText(ChatAIActivity.this, "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ChatAIActivity.this, "Lỗi mạng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChatAIActivity.this, "Lỗi mạng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Log.e("HUGGINGFACE_ERROR", "Error: " + error.getMessage());
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + huggingFaceApiKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                }
            };

            stringRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) {
                    // Optional: Implement retry logic if needed
                }
            });

            requestQueue.add(stringRequest);

        } catch (Exception e) {
            Log.e("HUGGINGFACE_ERROR", "Error processing image: " + e.getMessage());
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }
    private void analyzeFaceDetailsWithHuggingFace(Bitmap bitmap) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("inputs", encodedImage);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, imageAnalysisApiUrl,
                    response -> {
                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            if (jsonArray.length() > 0) {
                                JSONObject firstResult = jsonArray.getJSONObject(0);
                                // Check if the response includes face detection results
                                if (firstResult.has("face_detected")) {
                                    boolean faceDetected = firstResult.getBoolean("face_detected");
                                    if (faceDetected) {
                                        // Analyze facial details
                                        if (firstResult.has("facial_details")) {
                                            JSONObject facialDetails = firstResult.getJSONObject("facial_details");
                                            String emotions = facialDetails.getString("emotions");
                                            String landmarks = facialDetails.getString("landmarks");
                                            double attractivenessRating = facialDetails.getDouble("attractiveness_rating");

                                            // Provide detailed feedback
                                            String feedback = "Facial Details:\n" +
                                                    "Emotions: " + emotions + "\n" +
                                                    "Landmarks: " + landmarks + "\n" +
                                                    "Attractiveness Rating: " + attractivenessRating + "/10";

                                            addMessageToChat(feedback, false);
                                        } else {
                                            addMessageToChat("Face detected but no additional details available.", false);
                                        }
                                    } else {
                                        addMessageToChat("No face detected in the image.", false);
                                    }
                                } else {
                                    addMessageToChat("Face detection not supported in the response.", false);
                                }
                            } else {
                                addMessageToChat("Unable to detect or analyze face in the image.", false);
                            }
                        } catch (Exception e) {
                            Log.e("FACE_ANALYSIS_ERROR", "Error parsing response: " + e.getMessage());
                            Toast.makeText(ChatAIActivity.this, "Error parsing face analysis response", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            if (statusCode == 503) {
                                Toast.makeText(ChatAIActivity.this, "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ChatAIActivity.this, "Lỗi mạng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChatAIActivity.this, "Lỗi mạng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Log.e("FACE_ANALYSIS_ERROR", "Error: " + error.getMessage());
                    }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + huggingFaceApiKey);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }

                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes(StandardCharsets.UTF_8);
                }
            };

            stringRequest.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 50000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 50000;
                }

                @Override
                public void retry(VolleyError error) {
                    // Optional: Implement retry logic if needed
                }
            });

            requestQueue.add(stringRequest);

        } catch (Exception e) {
            Log.e("FACE_ANALYSIS_ERROR", "Error processing image: " + e.getMessage());
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }


}