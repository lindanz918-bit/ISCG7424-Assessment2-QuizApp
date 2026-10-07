package cs.linda.iscg7424assessment2quizapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CreateQuizActivity extends AppCompatActivity {
    private TextInputEditText etQuizTitle, etAmount;
    private AutoCompleteTextView spinnerDifficulty, spinnerCategory;
    private Button btnCreateQuiz;
    private ProgressBar progressBar;
    private EditText etStartDate, etEndDate;
    private FirebaseFirestore db;
    private ApiService apiService;
    private List<QuestionItem> fetchedQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_create_quiz), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        initRetrofit();
        initUI();
        btnCreateQuiz.setOnClickListener(v -> createAndSaveQuiz());
    }

    private void initUI() {
        etQuizTitle = findViewById(R.id.etQuizTitle);
        etAmount = findViewById(R.id.etAmount);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        progressBar = findViewById(R.id.progressBar);
        btnCreateQuiz = findViewById(R.id.btnCreateQuiz);

        String[] difficulties = new String[]{"any", "easy", "medium", "hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        String[] categories = new String[]{"Art", "Sports", "History", "Computers", "Animals"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);


        etStartDate.setOnClickListener(v -> showDatePickerDialog("start"));
        etEndDate.setOnClickListener(v -> showDatePickerDialog("end"));

    }

    private void initRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);
    }

    private void createAndSaveQuiz() {
        String quizTitle = etQuizTitle.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (TextUtils.isEmpty(quizTitle)) {
            Toast.makeText(this, "Please enter quiz title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please enter question amount", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateQuiz.setEnabled(false);

        int amount = Integer.parseInt(amountStr);

        Integer categoryId = null;
        String selectedCategory = spinnerCategory.getText().toString();
        if (!"Any Category".equalsIgnoreCase(selectedCategory)) {
            if ("Sports".equalsIgnoreCase(selectedCategory)) {
                categoryId = 21;
            } else if ("Vehicles".equalsIgnoreCase(selectedCategory)) {
                categoryId = 28;
            } else if ("General Knowledge".equalsIgnoreCase(selectedCategory)) {
                categoryId = 9;
            } else if ("History".equalsIgnoreCase(selectedCategory)) {
                categoryId = 23;
            } else if ("Animals".equalsIgnoreCase(selectedCategory)) {
                categoryId = 27;
            } else if ("Computers".equalsIgnoreCase(selectedCategory)) {
                categoryId = 18;
            } else if ("Art".equalsIgnoreCase(selectedCategory)) {
                categoryId = 25;
            } else if ("Geography".equalsIgnoreCase(selectedCategory)) {
                categoryId = 22;
            } else if ("Music".equalsIgnoreCase(selectedCategory)) {
                categoryId = 12;
            } else if ("Celebrities".equalsIgnoreCase(selectedCategory)) {
                categoryId = 26;
            } else if ("Science & Nature".equalsIgnoreCase(selectedCategory)) {
                categoryId = 17;
            } else {
                Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String selectedDifficulty = spinnerDifficulty.getText().toString();
        String difficultyParam = "any".equals(selectedDifficulty) ? null : selectedDifficulty;
        String typeParam = "multiple";

        apiService.getQuestions(amount, categoryId, difficultyParam, typeParam).enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().results != null) {
                    List<QuestionItem> questions = response.body().results;

                    if (questions.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        btnCreateQuiz.setEnabled(true);
                        Toast.makeText(CreateQuizActivity.this, "No questions returned from API", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveToFireStore(quizTitle, questions);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnCreateQuiz.setEnabled(true);
                    Toast.makeText(CreateQuizActivity.this, "Failed to fetch questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateQuiz.setEnabled(true);
                Toast.makeText(CreateQuizActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFireStore(String quizTitle, List<QuestionItem> questions) {
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", quizTitle);
        quizData.put("questionCount", questions.size());
        quizData.put("difficulty", spinnerDifficulty.getText().toString());
        quizData.put("category", spinnerCategory.getText().toString());
        quizData.put("startDate", etStartDate.getText().toString());
        quizData.put("endDate", etEndDate.getText().toString());
        quizData.put("createdAt", FieldValue.serverTimestamp());
        quizData.put("questions", questions);

        db.collection("quizzes")
                .add(quizData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(CreateQuizActivity.this, "Quiz created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateQuiz.setEnabled(true);
                    Toast.makeText(CreateQuizActivity.this, "Error saving quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

    private void showDatePickerDialog(String type) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);

                    if ("start".equals(type)) {
                        etStartDate.setText(formattedDate);
                        etEndDate.setText("");
                    } else if ("end".equals(type)) {
                        etEndDate.setText(formattedDate);
                    }
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        if ("end".equals(type)) {
            String startDateStr = etStartDate.getText().toString().trim();
            if (TextUtils.isEmpty(startDateStr)) {
                Toast.makeText(this, "Please select Start Date first", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date startDate = sdf.parse(startDateStr);
                if (startDate != null) {
                    datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        datePickerDialog.show();
    }
}