package cs.linda.iscg7424assessment2quizapp.player;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cs.linda.iscg7424assessment2quizapp.adapter.QuizAdapter;
import cs.linda.iscg7424assessment2quizapp.model.QuizModel;
import cs.linda.iscg7424assessment2quizapp.R;

public class QuizListTabFragment extends Fragment {

    private static final String ARG_TYPE = "quiz_type";
    private String quizType;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private ListenerRegistration quizListener;
    private List<QuizModel> quizList = new ArrayList<>();
    private QuizAdapter adapter;

    public static QuizListTabFragment newInstance(String type) {
        QuizListTabFragment fragment = new QuizListTabFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            quizType = getArguments().getString(ARG_TYPE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_list_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewQuizzes);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new QuizAdapter(requireContext(), quizList, false, new QuizAdapter.OnQuizActionListener() {
            @Override
            public void onStart(QuizModel quiz) {
                if ("ongoing".equalsIgnoreCase(quizType)) {
                    Toast.makeText(requireContext(), "Starting quiz...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireContext(), TakeQuizActivity.class);
                    intent.putExtra("quizId", quiz.getId());
                    startActivity(intent);
                } else if ("past".equalsIgnoreCase(quizType)) {
                    Toast.makeText(requireContext(), "Viewing past quiz archive", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "This quiz has not started yet!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onEdit(QuizModel quiz) {

            }

            @Override
            public void onDelete(QuizModel quiz) {

            }
        });
        recyclerView.setAdapter(adapter);

        loadQuizzesFromFirestoreRealtime();
    }

    private void loadQuizzesFromFirestoreRealtime() {
        progressBar.setVisibility(View.VISIBLE);

        if (quizListener != null) {
            quizListener.remove();
        }
        String currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        if ("past".equalsIgnoreCase(quizType)) {
            db.collection("history")
                    .whereEqualTo("userId", currentUserId)
                    .get()
                    .addOnSuccessListener(historySnapshots -> {
                        List<String> participatedQuizIds = new ArrayList<>();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : historySnapshots) {
                            String qId = doc.getString("quizId");
                            if (qId != null) participatedQuizIds.add(qId);
                        }
                        fetchQuizzes(participatedQuizIds);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Error loading history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            fetchQuizzes(new ArrayList<>());
        }
    }
    private void fetchQuizzes(List<String> participatedQuizIds) {
        quizListener = db.collection("quizzes")
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        quizList.clear();
                        Date now = new Date();

                        for (QueryDocumentSnapshot doc : value) {
                            QuizModel quiz = doc.toObject(QuizModel.class);
                            quiz.setId(doc.getId());

                            if (quiz.getStartDate() != null && quiz.getEndDate() != null) {
                                if ("ongoing".equalsIgnoreCase(quizType)) {
                                    if (quiz.getStartDate().before(now) && quiz.getEndDate().after(now)) {
                                        quizList.add(quiz);
                                    }
                                } else if ("upcoming".equalsIgnoreCase(quizType)) {
                                    if (quiz.getStartDate().after(now)) {
                                        quizList.add(quiz);
                                    }
                                } else if ("past".equalsIgnoreCase(quizType)) {
                                    if (quiz.getEndDate().before(now) && !participatedQuizIds.contains(quiz.getId())) {
                                        quizList.add(quiz);
                                    }
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (quizList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("No " + quizType + " quizzes found.");
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (quizListener != null) {
            quizListener.remove();
        }
    }
}
