package cs.linda.iscg7424assessment2quizapp.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.UnknownNullability;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cs.linda.iscg7424assessment2quizapp.model.QuestionModel;
import cs.linda.iscg7424assessment2quizapp.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminCreateQuizFragment extends Fragment {
    private TextInputEditText etQuizTitle, etAmount;
    private AutoCompleteTextView spinnerDifficulty, spinnerCategory;
    private Button btnCreateQuiz;
    private ProgressBar progressBar;
    private EditText etStartDate, etEndDate;
    private FirebaseFirestore db;
    private ApiService apiService;

    public AdminCreateQuizFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_create_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();
        initRetrofit();
        initUI(view);
        btnCreateQuiz.setOnClickListener(v -> createAndSaveQuiz());

    }

    private void initUI(View view) {
        etQuizTitle = view.findViewById(R.id.etQuizTitle);
        etAmount = view.findViewById(R.id.etAmount);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        progressBar = view.findViewById(R.id.progressBar);
        btnCreateQuiz = view.findViewById(R.id.btnCreateQuiz);

        String[] difficulties = new String[]{"any", "easy", "medium", "hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        String[] categories = new String[]{"Art", "Sports", "History", "Computers", "Animals"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
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
            Toast.makeText(requireContext(), "Please enter quiz title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            Toast.makeText(requireContext(), "Please enter question amount", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Invalid category", Toast.LENGTH_SHORT).show();
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
                    List<QuestionModel> questions = response.body().results;

                    if (questions.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        btnCreateQuiz.setEnabled(true);
                        Toast.makeText(requireContext(), "No questions returned from API", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveToFireStore(quizTitle, questions);
                } else {
                    progressBar.setVisibility(View.GONE);
                    btnCreateQuiz.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to fetch questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnCreateQuiz.setEnabled(true);
                Toast.makeText(requireContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToFireStore(String quizTitle, @UnknownNullability List<QuestionModel> questions) {
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", quizTitle);
        quizData.put("questionCount", questions.size());
        quizData.put("difficulty", spinnerDifficulty.getText().toString());
        quizData.put("category", spinnerCategory.getText().toString());

        //Transfer sting date to Date which firestore recognized as timestamp type.
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = dateFormat.parse(etStartDate.getText().toString());
            Date endDate = dateFormat.parse(etEndDate.getText().toString());

            quizData.put("startDate", startDate);
            quizData.put("endDate", endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            // if error, set date to null
            progressBar.setVisibility(View.GONE);
            btnCreateQuiz.setEnabled(true);
            Toast.makeText(requireContext(), "Invalid date format!", Toast.LENGTH_SHORT).show();
            return;
        }

        quizData.put("createdAt", FieldValue.serverTimestamp());
        quizData.put("questions", questions);
        quizData.put("totalSubmissions", 0);
        quizData.put("totalRatingScore", 0);
        quizData.put("averageRating", 0.0);

        db.collection("quizzes")
                .add(quizData)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Quiz created successfully!", Toast.LENGTH_SHORT).show();
                    btnCreateQuiz.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnCreateQuiz.setEnabled(true);
                    Toast.makeText(requireContext(), "Error saving quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }

    private void showDatePickerDialog(String type) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
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
                Toast.makeText(requireContext(), "Please select Start Date first", Toast.LENGTH_SHORT).show();
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