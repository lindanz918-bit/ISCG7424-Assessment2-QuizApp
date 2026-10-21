package cs.linda.iscg7424assessment2quizapp.player;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.adapter.QuizHistoryAdapter;
import cs.linda.iscg7424assessment2quizapp.model.QuizHistoryModel;

public class PlayerHistoryFragment extends Fragment implements QuizHistoryAdapter.OnQuizActionListener{

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private FirebaseFirestore db;
    private List<QuizHistoryModel> historyList = new ArrayList<>();
    private QuizHistoryAdapter adapter;

    public PlayerHistoryFragment() {
        // Required empty public constructor
    }

    public static PlayerHistoryFragment newInstance() {
        return new PlayerHistoryFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewHistory);
        progressBar = view.findViewById(R.id.progressBarHistory);
        tvEmpty = view.findViewById(R.id.tvEmptyHistory);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new QuizHistoryAdapter(requireContext(), historyList, this);
        recyclerView.setAdapter(adapter);

        loadHistoryFromFirestore();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryFromFirestore();
    }

    private void loadHistoryFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);

        db.collection("history")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    historyList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        QuizHistoryModel model = doc.toObject(QuizHistoryModel.class);
                        historyList.add(model);
                    }
                    adapter.notifyDataSetChanged();

                    if (historyList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "Error loading history: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDelete(QuizHistoryModel history) {
        if (history.getHistoryId() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete History")
                .setMessage("Are you sure you want to delete this quiz history record?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("history").document(history.getHistoryId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "History deleted successfully", Toast.LENGTH_SHORT).show();
                                }
                                loadHistoryFromFirestore();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded() && getContext() != null) {
                                    Toast.makeText(requireContext(), "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}