package cs.linda.iscg7424assessment2quizapp.player;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.adapter.QuestionHistoryAdapter;
import cs.linda.iscg7424assessment2quizapp.model.QuestionHistoryModel;
import cs.linda.iscg7424assessment2quizapp.model.QuizHistoryModel;

public class HistoryDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle, tvScore, tvDate;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String historyId;
    private List<QuestionHistoryModel> questionResults = new ArrayList<>();
    private QuestionHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_detail_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        historyId = getIntent().getStringExtra("historyId");
        if (historyId == null) {
            Toast.makeText(this, "Invalid History ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvScore = findViewById(R.id.tvDetailScore);
        tvDate = findViewById(R.id.tvDetailDate);
        recyclerView = findViewById(R.id.recyclerViewDetails);
        progressBar = findViewById(R.id.progressBarDetail);

        btnBack.setOnClickListener(v -> {
            android.util.Log.d("HistoryDetail", "Back button is clicked!");
            Toast.makeText(this, "Back clicked", Toast.LENGTH_SHORT).show();
            finish();
        });

        View layoutToolbar = findViewById(R.id.layoutToolbar);
        if (layoutToolbar != null) {
            layoutToolbar.setOnClickListener(v -> finish());
        }

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuestionHistoryAdapter(this, questionResults);
        recyclerView.setAdapter(adapter);

        loadHistoryDetailFromFirestore();
    }

    private void loadHistoryDetailFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("history").document(historyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        QuizHistoryModel history = documentSnapshot.toObject(QuizHistoryModel.class);
                        if (history != null) {
                            tvTitle.setText(history.getQuizTitle());
                            tvScore.setText("Score: " + history.getScore() + " / " + history.getTotalQuestions());

                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            tvDate.setText("Completed: " + sdf.format(new Date(history.getTimestamp())));

                            if (history.getQuestionResults() != null) {
                                questionResults.clear();
                                questionResults.addAll(history.getQuestionResults());
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                        Toast.makeText(this, "History record not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}