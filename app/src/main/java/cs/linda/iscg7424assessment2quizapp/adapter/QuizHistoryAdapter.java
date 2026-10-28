package cs.linda.iscg7424assessment2quizapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.model.QuizHistoryModel;
import cs.linda.iscg7424assessment2quizapp.player.HistoryDetailActivity;

public class QuizHistoryAdapter extends RecyclerView.Adapter<QuizHistoryAdapter.ViewHolder> {
    private Context context;
    private List<QuizHistoryModel> historyList;
    private QuizHistoryAdapter.OnQuizActionListener listener;

    public interface OnQuizActionListener{
        void onDelete(QuizHistoryModel history);
    };


    public QuizHistoryAdapter(Context context, List<QuizHistoryModel> historyList, OnQuizActionListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_card_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizHistoryModel history = historyList.get(position);
        holder.tvTitle.setText(history.getQuizTitle());
        holder.tvCategory.setText("Category: " + history.getQuizCategory());
        holder.tvDifficulty.setText("Difficulty: " + history.getQuizDifficulty());
        holder.tvType.setText("Type: " + history.getQuizType());
        holder.tvScore.setText("Score: " + history.getScore() + " / " + history.getTotalQuestions());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText("Completed: " + sdf.format(new Date(history.getTimestamp())));

        holder.btnViewQuiz.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            intent.putExtra("historyId", history.getHistoryId());
            context.startActivity(intent);
        });
        holder.btnDeleteQuiz.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(history);
        });
    }

    @Override
    public int getItemCount() { return historyList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDifficulty, tvType, tvScore, tvDate;
        Button btnViewQuiz, btnDeleteQuiz;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvCategory = itemView.findViewById(R.id.tvQuizCategory);
            tvDifficulty = itemView.findViewById(R.id.tvQuizDifficulty);
            tvType = itemView.findViewById(R.id.tvQuizType);
            tvScore = itemView.findViewById(R.id.tvHistoryScore);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
            btnViewQuiz = itemView.findViewById(R.id.btnViewQuiz);
            btnDeleteQuiz = itemView.findViewById(R.id.btnDeleteQuiz);
        }
    }
}