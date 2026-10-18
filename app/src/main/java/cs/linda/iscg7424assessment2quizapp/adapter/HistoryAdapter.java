package cs.linda.iscg7424assessment2quizapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private Context context;
    private List<QuizHistoryModel> historyList;

    public HistoryAdapter(Context context, List<QuizHistoryModel> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_quiz_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizHistoryModel history = historyList.get(position);
        holder.tvTitle.setText(history.getQuizTitle());
        holder.tvScore.setText("Score: " + history.getScore() + " / " + history.getTotalQuestions());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText("Completed: " + sdf.format(new Date(history.getTimestamp())));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryDetailActivity.class);
            intent.putExtra("historyId", history.getHistoryId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return historyList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvScore, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHistoryTitle);
            tvScore = itemView.findViewById(R.id.tvHistoryScore);
            tvDate = itemView.findViewById(R.id.tvHistoryDate);
        }
    }
}