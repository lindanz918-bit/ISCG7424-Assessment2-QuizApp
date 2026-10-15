package cs.linda.iscg7424assessment2quizapp.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cs.linda.iscg7424assessment2quizapp.adapter.QuizAdapter;
import cs.linda.iscg7424assessment2quizapp.model.QuizModel;
import cs.linda.iscg7424assessment2quizapp.R;

public class AdminQuizListFragment extends Fragment {
    private RecyclerView recyclerView;
    private QuizAdapter adapter;
    private List<QuizModel> quizList = new ArrayList<>();
    private FirebaseFirestore db;

    public AdminQuizListFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_quiz_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recyclerViewQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new QuizAdapter(requireContext(), quizList, Boolean.TRUE, new QuizAdapter.OnQuizActionListener(){
            @Override
            public void onStart(QuizModel quiz) {}
            @Override
            public void onEdit(QuizModel quiz) {
                showEditQuizDialog(quiz);
            }
            @Override
            public void onDelete(QuizModel quiz) {
                confirmDeleteQuiz(quiz);
            }
        });
        recyclerView.setAdapter(adapter);

        loadQuizzesFromFirestore();
    }
    private void loadQuizzesFromFirestore() {
        db.collection("quizzes")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null || !isAdded()) return;
                    quizList.clear();
                    quizList.addAll(value.toObjects(QuizModel.class));
                    adapter.notifyDataSetChanged();
                });
    }

    private void confirmDeleteQuiz(QuizModel quiz) {
        if (getContext() == null || quiz.getId() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Quiz")
                .setMessage("Are you sure you want to delete '" + quiz.getTitle() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("quizzes").document(quiz.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Quiz deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditQuizDialog(QuizModel quiz) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_quiz, null);
        TextInputEditText etEditTitle = dialogView.findViewById(R.id.etEditTitle);
        TextInputEditText etEditStartDate = dialogView.findViewById(R.id.etEditStartDate);
        TextInputEditText etEditEndDate = dialogView.findViewById(R.id.etEditEndDate);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        etEditTitle.setText(quiz.getTitle());
        if (quiz.getStartDate() != null) {
            etEditStartDate.setText(sdf.format(quiz.getStartDate()));
        }
        if (quiz.getEndDate() != null) {
            etEditEndDate.setText(sdf.format(quiz.getEndDate()));
        }

        etEditStartDate.setOnClickListener(v -> showDatePickerDialog(etEditStartDate, etEditEndDate, true));
        etEditEndDate.setOnClickListener(v -> showDatePickerDialog(etEditStartDate, etEditEndDate, false));

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newTitle = etEditTitle.getText().toString().trim();
                    String startDateStr = etEditStartDate.getText().toString().trim();
                    String endDateStr = etEditEndDate.getText().toString().trim();

                    if (TextUtils.isEmpty(newTitle) || TextUtils.isEmpty(startDateStr) || TextUtils.isEmpty(endDateStr)) {
                        Toast.makeText(requireContext(), "Please complete all fields", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        Date newStartDate = sdf.parse(startDateStr);
                        Date newEndDate = sdf.parse(endDateStr);

                        updateQuizInFirestore(quiz.getId(), newTitle, newStartDate, newEndDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Invalid Date Format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showDatePickerDialog(TextInputEditText etStartDate, TextInputEditText etEndDate, boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    if (isStart) {
                        etStartDate.setText(formattedDate);
                        etEndDate.setText(""); // 清空结束时间重新选择
                    } else {
                        etEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        if (!isStart) {
            String startStr = etStartDate.getText().toString().trim();
            if (TextUtils.isEmpty(startStr)) {
                Toast.makeText(requireContext(), "Please select Start Date first", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Date startDate = sdf.parse(startStr);
                if (startDate != null) {
                    datePickerDialog.getDatePicker().setMinDate(startDate.getTime());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        datePickerDialog.show();
    }

    private void updateQuizInFirestore(String quizId, String newTitle, Date newStartDate, Date newEndDate) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", newTitle);
        updates.put("startDate", newStartDate);
        updates.put("endDate", newEndDate);

        FirebaseFirestore.getInstance()
                .collection("quizzes")
                .document(quizId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Quiz updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}