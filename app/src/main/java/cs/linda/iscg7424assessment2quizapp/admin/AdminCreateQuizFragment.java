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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

    private TextInputLayout layoutQuizTitle, layoutAmount, layoutCategory, layoutDifficulty, layoutType, layoutStartDate, layoutEndDate;
    private TextInputEditText etQuizTitle, etAmount, etStartDate, etEndDate;
    private AutoCompleteTextView spinnerDifficulty, spinnerCategory, spinnerType;
    private Button btnCreateQuiz;
    private ProgressBar progressBar;
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
        spinnerType = view.findViewById(R.id.spinnerType);
        etStartDate = view.findViewById(R.id.etStartDate);
        etEndDate = view.findViewById(R.id.etEndDate);
        progressBar = view.findViewById(R.id.progressBar);
        btnCreateQuiz = view.findViewById(R.id.btnCreateQuiz);

        String[] categories = new String[]{"Any Category", "Sports", "Vehicles", "General Knowledge", "History", "Animals", "Computers", "Art", "Geography", "Music", "Celebrities", "Science & Nature"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        String[] difficulties = new String[]{"Any Difficulty", "Easy", "Medium", "Hard"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        String[] types = new String[]{"Any Type", "Multiple Choice", "True/False"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

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
        String quizTitle = etQuizTitle.getText() != null ? etQuizTitle.getText().toString().trim() : "";
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "";
        String difficulty = spinnerDifficulty.getText() != null ? spinnerDifficulty.getText().toString().trim() : "";
        String type = spinnerType.getText() != null ? spinnerType.getText().toString().trim() : "";
        String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";

        boolean isValid = true;
        TextInputLayout titleLayout = (TextInputLayout) etQuizTitle.getParent().getParent();

        if (TextUtils.isEmpty(quizTitle)) {
            titleLayout.setError("Please enter quiz title");
            isValid = false;
        } else {
            titleLayout.setError(null);
        }

        TextInputLayout amountLayout = (TextInputLayout) etAmount.getParent().getParent();
        if (TextUtils.isEmpty(amountStr)) {
            amountLayout.setError("Please enter question amount");
            isValid = false;
        } else {
            try {
                int amount = Integer.parseInt(amountStr);
                if (amount < 1 || amount > 50) {
                    amountLayout.setError("Amount must be between 1 and 50");
                    isValid = false;
                } else {
                    amountLayout.setError(null);
                }
            } catch (NumberFormatException e) {
                amountLayout.setError("Invalid number format");
                isValid = false;
            }
        }

        TextInputLayout categoryLayout = (TextInputLayout) spinnerCategory.getParent().getParent();
        if (TextUtils.isEmpty(category)) {
            categoryLayout.setError("Please select a category");
            isValid = false;
        } else {
            categoryLayout.setError(null);
        }

        TextInputLayout diffLayout = (TextInputLayout) spinnerDifficulty.getParent().getParent();
        if (TextUtils.isEmpty(difficulty)) {
            diffLayout.setError("Please select difficulty");
            isValid = false;
        } else {
            diffLayout.setError(null);
        }
        TextInputLayout typeLayout = (TextInputLayout) spinnerType.getParent().getParent();
        if (TextUtils.isEmpty(type)) {
            typeLayout.setError("Please select type");
            isValid = false;
        } else {
            typeLayout.setError(null);
        }

        TextInputLayout startLayout = (TextInputLayout) etStartDate.getParent().getParent();
        if (TextUtils.isEmpty(startDate)) {
            startLayout.setError("Please select start date");
            isValid = false;
        } else {
            startLayout.setError(null);
        }

        TextInputLayout endLayout = (TextInputLayout) etEndDate.getParent().getParent();
        if (TextUtils.isEmpty(endDate)) {
            endLayout.setError("Please select end date");
            isValid = false;
        } else {
            endLayout.setError(null);
        }

        if (!isValid) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnCreateQuiz.setEnabled(false);

        int amount = Integer.parseInt(amountStr);
        Integer categoryId = getCategoryId(category);
        if (categoryId == null && !"Any Category".equalsIgnoreCase(category)) {
            progressBar.setVisibility(View.GONE);
            btnCreateQuiz.setEnabled(true);
            categoryLayout.setError("Invalid category");
            return;
        }


        String difficultyParam = "Any Difficulty".equals(difficulty) ? null : difficulty.trim().toLowerCase();
        String typeParam = "Any Type".equals(type) ? null : getType(type);

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

    private Integer getCategoryId(String selectedCategory) {
        if ("Sports".equalsIgnoreCase(selectedCategory)) return 21;
        if ("Vehicles".equalsIgnoreCase(selectedCategory)) return 28;
        if ("General Knowledge".equalsIgnoreCase(selectedCategory)) return 9;
        if ("History".equalsIgnoreCase(selectedCategory)) return 23;
        if ("Animals".equalsIgnoreCase(selectedCategory)) return 27;
        if ("Computers".equalsIgnoreCase(selectedCategory)) return 18;
        if ("Art".equalsIgnoreCase(selectedCategory)) return 25;
        if ("Geography".equalsIgnoreCase(selectedCategory)) return 22;
        if ("Music".equalsIgnoreCase(selectedCategory)) return 12;
        if ("Celebrities".equalsIgnoreCase(selectedCategory)) return 26;
        if ("Science & Nature".equalsIgnoreCase(selectedCategory)) return 17;
        return null;
    }

    private String getType(String selectedType) {
        if (selectedType == null) return null;

        if ("Multiple Choice".equalsIgnoreCase(selectedType)) {
            return "multiple";
        } else if ("True/False".equalsIgnoreCase(selectedType)) {
            return "boolean";
        }
        return null;
    }

    private void saveToFireStore(String quizTitle, @UnknownNullability List<QuestionModel> questions) {

        if (questions != null) {
            for (QuestionModel q : questions) {
                java.util.List<String> options = new java.util.ArrayList<>();
                if (q.getIncorrectAnswers() != null) {
                    options.addAll(q.getIncorrectAnswers());
                }
                if (q.getCorrectAnswer() != null) {
                    options.add(q.getCorrectAnswer());
                }
                java.util.Collections.shuffle(options);
                q.setShuffledOptions(options);

                if (q.getCategory() != null) {
                    q.setCategory(q.getCategory().replace("&amp;", "&"));
                }
            }
        }

        Map<String, Object> quizData = new HashMap<>();
        quizData.put("title", quizTitle);
        quizData.put("questionCount", questions != null ? questions.size() : 0);
        quizData.put("category", spinnerCategory.getText().toString());
        quizData.put("difficulty", spinnerDifficulty.getText().toString());
        quizData.put("type", spinnerType.getText().toString());

        // Transfer string date to Date which firestore recognized as timestamp type.
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
                    // go back to quiz list
                    BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottomNavigationView);
                    if (bottomNav != null) {
                        bottomNav.setSelectedItemId(R.id.nav_quizzes);
                    }
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