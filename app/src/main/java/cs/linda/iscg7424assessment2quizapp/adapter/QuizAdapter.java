package cs.linda.iscg7424assessment2quizapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.model.QuizModel;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
    private Context context;
    private List<QuizModel> quizList;
    private OnQuizActionListener listener;
    private boolean isAdmin;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public interface OnQuizActionListener {
        void onStart(QuizModel quiz);

        void onEdit(QuizModel quiz);

        void onDelete(QuizModel quiz);
    }

    public QuizAdapter(Context context, List<QuizModel> quizList, Boolean isAdmin, OnQuizActionListener listener) {
        this.context = context;
        this.quizList = quizList;
        this.listener = listener;
        this.isAdmin = isAdmin != null ? isAdmin : false;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_card, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuizModel quiz = quizList.get(position);

        holder.tvQuizTitle.setText(quiz.getTitle());
        holder.tvQuizCategory.setText(String.format("Category: %s", quiz.getCategory()));
        holder.tvQuizDifficulty.setText(String.format("Difficulty: %s", quiz.getDifficulty()));
        if (holder.tvQuizQuestions != null) {
            holder.tvQuizQuestions.setText("Questions: " + quiz.getQuestionCount());
        }
        String startStr = quiz.getStartDate() != null ? dateFormat.format(quiz.getStartDate()) : "N/A";
        String endStr = quiz.getEndDate() != null ? dateFormat.format(quiz.getEndDate()) : "N/A";
        holder.tvQuizDateRange.setText(String.format("Date: %s - %s", startStr, endStr));

        if (quiz.getTotalSubmissions() == 0) {
            holder.tvRatingScore.setText("★ N/A");
            holder.tvSubmissionCount.setText("(No submissions yet)");
        } else {
            holder.tvRatingScore.setText(String.format(Locale.getDefault(), "★ %.1f", quiz.getAverageRating()));
            holder.tvSubmissionCount.setText(String.format(Locale.getDefault(), "(%d submissions)", quiz.getTotalSubmissions()));
        }

        String status = quiz.getStatus();

        if (isAdmin) {
            holder.btnStartQuiz.setVisibility(View.GONE);
            if ("UPCOMING".equals(status)) {
                holder.tvStatusBadge.setText("Upcoming");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#2196F3"));

                holder.btnEditQuiz.setVisibility(View.VISIBLE);
                holder.btnEditQuiz.setEnabled(true);
                holder.btnEditQuiz.setOnClickListener(v -> listener.onEdit(quiz));

            } else if ("ACTIVE".equals(status)) {
                holder.tvStatusBadge.setText("Active");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#4CAF50"));

                holder.btnEditQuiz.setVisibility(View.GONE);

            } else {
                holder.tvStatusBadge.setText("Ended");
                holder.tvStatusBadge.setBackgroundColor(Color.parseColor("#9E9E9E"));

                holder.btnEditQuiz.setVisibility(View.GONE);
            }

            holder.btnDeleteQuiz.setVisibility(View.VISIBLE);
            holder.btnDeleteQuiz.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(quiz);
            });

        } else {
            holder.btnEditQuiz.setVisibility(View.GONE);
            holder.btnDeleteQuiz.setVisibility(View.GONE);
            holder.btnStartQuiz.setVisibility(View.VISIBLE);
            if ("ACTIVE".equals(status)) {
                holder.tvStatusBadge.setText("Active");
                holder.btnStartQuiz.setEnabled(true);
                holder.btnStartQuiz.setText("Start");
                holder.btnStartQuiz.setOnClickListener(v -> {
                    if (listener != null) listener.onStart(quiz);
                });
            } else if ("UPCOMING".equals(status)) {
                holder.tvStatusBadge.setText("Upcoming");
                holder.btnStartQuiz.setEnabled(false);
                holder.btnStartQuiz.setText("Upcoming");
                holder.btnStartQuiz.setOnClickListener(null);
            } else {
                holder.tvStatusBadge.setText("Ended");
                holder.btnStartQuiz.setEnabled(false);
                holder.btnStartQuiz.setText("Ended");
                holder.btnStartQuiz.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return quizList != null ? quizList.size() : 0;
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuizTitle, tvStatusBadge, tvQuizCategory, tvQuizDifficulty, tvQuizQuestions, tvQuizDateRange, tvRatingScore, tvSubmissionCount;
        Button btnStartQuiz, btnEditQuiz, btnDeleteQuiz;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuizTitle = itemView.findViewById(R.id.tvQuizTitle);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvQuizCategory = itemView.findViewById(R.id.tvQuizCategory);
            tvQuizDifficulty = itemView.findViewById(R.id.tvQuizDifficulty);
            tvQuizQuestions = itemView.findViewById(R.id.tvQuizQuestions);
            tvQuizDateRange = itemView.findViewById(R.id.tvQuizDateRange);
            tvRatingScore = itemView.findViewById(R.id.tvRatingScore);
            tvSubmissionCount = itemView.findViewById(R.id.tvSubmissionCount);
            btnStartQuiz = itemView.findViewById(R.id.btnStartQuiz);
            btnEditQuiz = itemView.findViewById(R.id.btnEditQuiz);
            btnDeleteQuiz = itemView.findViewById(R.id.btnDeleteQuiz);
        }
    }
}