package cs.linda.iscg7424assessment2quizapp.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import cs.linda.iscg7424assessment2quizapp.model.QuestionHistoryModel;
import cs.linda.iscg7424assessment2quizapp.model.QuestionModel;
import cs.linda.iscg7424assessment2quizapp.adapter.QuestionPagerAdapter;
import cs.linda.iscg7424assessment2quizapp.model.QuizModel;
import cs.linda.iscg7424assessment2quizapp.R;

public class TakeQuizActivity extends AppCompatActivity {
    private Button btnQuitQuiz;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private Button btnPrev, btnNext;

    private FirebaseFirestore db;
    private String quizId, quizTitle, quizCategory, quizDifficulty, quizType;
    private List<QuestionModel> questionList = new ArrayList<>();
    private QuestionPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_take_quiz);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.take_quiz_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnQuitQuiz = findViewById(R.id.btnQuitQuiz);
        btnQuitQuiz.setOnClickListener(v -> showExitConfirmationDialog());

        quizId = getIntent().getStringExtra("quizId");
        quizTitle = getIntent().getStringExtra("quizTitle");
        quizCategory = getIntent().getStringExtra("quizCategory");
        quizDifficulty = getIntent().getStringExtra("quizDifficulty");
        quizType = getIntent().getStringExtra("quizType");

        if (quizId == null) {
            Toast.makeText(this, "Invalid Quiz ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        viewPager = findViewById(R.id.viewPagerQuestions);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        viewPager.setUserInputEnabled(false);

        adapter = new QuestionPagerAdapter(this, questionList);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateNavigationUI(position);
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < questionList.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            } else {
                submitQuiz();
            }
        });

        loadQuestionsFromFirestore();
    }

    private void showExitConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Quit Quiz?")
                .setMessage("Are you sure you want to exit? Your current attempt progress will not be saved.")
                .setPositiveButton("Quit", (dialog, which) -> {
                    finish(); // 确认后退出当前 Activity
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }

    private void loadQuestionsFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("quizzes").document(quizId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        QuizModel quiz = documentSnapshot.toObject(QuizModel.class);
                        if (quiz != null && quiz.getQuestions() != null) {
                            questionList.clear();
                            questionList.addAll(quiz.getQuestions());
                            adapter.notifyDataSetChanged();
                            updateNavigationUI(0);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateNavigationUI(int position) {
        if (questionList.isEmpty()) return;

        tvProgress.setText((position + 1) + " / " + questionList.size());
        btnPrev.setEnabled(position > 0);

        if (position == questionList.size() - 1) {
            btnNext.setText("Submit");
        } else {
            btnNext.setText("Next");
        }
    }

    private void submitQuiz() {
        int score = 0;
        List<QuestionHistoryModel> resultList = new ArrayList<>();

        for (QuestionModel q : questionList) {
            boolean isCorrect = q.getUserAnswer() != null && q.getUserAnswer().equalsIgnoreCase(q.getCorrectAnswer());
            if (isCorrect) {
                score++;
            }
            resultList.add(new QuestionHistoryModel(
                    q.getQuestion(),
                    q.getUserAnswer() != null ? q.getUserAnswer() : "No Answer",
                    q.getCorrectAnswer(),
                    isCorrect
            ));
        }
        saveQuizHistoryToFirestore(score, resultList);
    }

    private void saveQuizHistoryToFirestore(int score, List<QuestionHistoryModel> resultList) {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        String historyId = db.collection("history").document().getId();

        java.util.Map<String, Object> historyMap = new java.util.HashMap<>();
        historyMap.put("historyId", historyId);
        historyMap.put("userId", userId);
        historyMap.put("quizId", quizId);
        historyMap.put("quizTitle", quizTitle);
        historyMap.put("quizCategory", quizCategory);
        historyMap.put("quizDifficulty", quizDifficulty);
        historyMap.put("quizType", quizType);
        historyMap.put("score", score);
        historyMap.put("totalQuestions", questionList.size());
        historyMap.put("timestamp", System.currentTimeMillis());
        historyMap.put("questionResults", resultList);

        db.collection("history").document(historyId)
                .set(historyMap)
                .addOnSuccessListener(aVoid -> {
                    showRatingDialog(score);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showRatingDialog(score);
                });
    }
    private void showRatingDialog(int quizScore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rate_quiz, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button btnSkip = dialogView.findViewById(R.id.btnSkip);
        Button btnSubmitRating = dialogView.findViewById(R.id.btnSubmitRating);

        btnSkip.setOnClickListener(v -> {
            updateQuizStatsToFirestore(0, false);
            dialog.dismiss();
            finish();
        });

        btnSubmitRating.setOnClickListener(v -> {
            float userRating = ratingBar.getRating();
            if (userRating == 0) {
                Toast.makeText(this, "Please select at least 1 star!", Toast.LENGTH_SHORT).show();
                return;
            }
            updateQuizStatsToFirestore((int) userRating, true);
            dialog.dismiss();
            Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show();
            finish();
        });

        dialog.show();
    }
    private void updateQuizStatsToFirestore(int ratingGiven, boolean isRated) {
        db.runTransaction(transaction -> {
            com.google.firebase.firestore.DocumentReference quizRef = db.collection("quizzes").document(quizId);
            com.google.firebase.firestore.DocumentSnapshot snapshot = transaction.get(quizRef);

            long currentSubmissions = snapshot.contains("totalSubmissions") ? snapshot.getLong("totalSubmissions") : 0;
            long currentTotalRating = snapshot.contains("totalRatingScore") ? snapshot.getLong("totalRatingScore") : 0;

            long newSubmissions = currentSubmissions + 1;
            long newTotalRating = currentTotalRating + (isRated ? ratingGiven : 0);

            double newAverageRating = newSubmissions > 0 ? (double) newTotalRating / newSubmissions : 0.0;

            transaction.update(quizRef,
                    "totalSubmissions", newSubmissions,
                    "totalRatingScore", newTotalRating,
                    "averageRating", newAverageRating
            );

            return null;
        }).addOnFailureListener(e -> {
            Toast.makeText(TakeQuizActivity.this, "Failed to update statistics: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}