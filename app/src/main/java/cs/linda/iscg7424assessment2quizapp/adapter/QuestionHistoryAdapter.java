package cs.linda.iscg7424assessment2quizapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.model.QuestionHistoryModel;

public class QuestionHistoryAdapter extends RecyclerView.Adapter<QuestionHistoryAdapter.ViewHolder> {
    private Context context;
    private List<QuestionHistoryModel> list;

    public QuestionHistoryAdapter(Context context, List<QuestionHistoryModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuestionHistoryModel model = list.get(position);

        holder.tvQuestion.setText("Q" + (position + 1) + ": " + model.getQuestionText());
        holder.tvUserAnswer.setText("Your Answer: " + model.getUserAnswer());
        holder.tvCorrectAnswer.setText("Correct Answer: " + model.getCorrectAnswer());

        if (model.isCorrect()) {
            holder.tvStatus.setText("✓ Correct");
            holder.tvStatus.setTextColor(Color.parseColor("#4CAF50")); // 绿色
        } else {
            holder.tvStatus.setText("✗ Incorrect");
            holder.tvStatus.setTextColor(Color.parseColor("#F44336")); // 红色
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvStatus, tvUserAnswer, tvCorrectAnswer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestionText);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvUserAnswer = itemView.findViewById(R.id.tvUserAnswer);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
        }
    }
}