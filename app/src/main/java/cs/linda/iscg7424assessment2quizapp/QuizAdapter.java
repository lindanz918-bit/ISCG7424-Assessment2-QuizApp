package cs.linda.iscg7424assessment2quizapp;


import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {

    private List<QuestionItem> questionList;
    private OnNextClickListener onNextClickListener;
    private boolean isPreviewMode = false;

    public interface OnNextClickListener {
        void onNextClick(int currentPosition);
    }

    public QuizAdapter(List<QuestionItem> questionList, OnNextClickListener listener) {
        this.questionList = questionList;
        this.onNextClickListener = listener;
        this.isPreviewMode = false;
    }

    public QuizAdapter(List<QuestionItem> questionList, boolean isPreviewMode) {
        this.questionList = questionList;
        this.onNextClickListener = null;
        this.isPreviewMode = isPreviewMode;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_card, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        QuestionItem item = questionList.get(position);

        holder.tvQuestion.setText(Html.fromHtml(item.getQuestion(), Html.FROM_HTML_MODE_LEGACY));
        holder.tvCategory.setText(item.getCategory());
        holder.tvDifficulty.setText(item.getDifficulty().toUpperCase());

        holder.rgOptions.removeAllViews();
        List<String> options = item.getShuffledOptions();

        for (int i = 0; i < options.size(); i++) {
            RadioButton rb = new RadioButton(holder.itemView.getContext());
            rb.setId(View.generateViewId());
            rb.setText(Html.fromHtml(options.get(i), Html.FROM_HTML_MODE_LEGACY));
            rb.setTextSize(16);
            rb.setPadding(16, 16, 16, 16);
            holder.rgOptions.addView(rb);
        }

        if (isPreviewMode) {
            holder.btnAction.setVisibility(View.GONE);
            holder.layoutFeedback.setVisibility(View.GONE);
            for (int i = 0; i < holder.rgOptions.getChildCount(); i++) {
                holder.rgOptions.getChildAt(i).setEnabled(false);
            }
        } else {
            holder.btnAction.setVisibility(View.VISIBLE);
            if (!item.isAnswered()) {
                holder.rgOptions.setEnabled(true);
                for (int i = 0; i < holder.rgOptions.getChildCount(); i++) {
                    holder.rgOptions.getChildAt(i).setEnabled(true);
                }
                holder.layoutFeedback.setVisibility(View.GONE);
                holder.btnAction.setText("Submit Answer");
            } else {
                showFeedback(holder, item);
            }
        }

        holder.btnAction.setOnClickListener(v -> {
            if (!item.isAnswered()) {
                int selectedId = holder.rgOptions.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(v.getContext(), "Please select an answer!", Toast.LENGTH_SHORT).show();
                    return;
                }

                RadioButton selectedRb = holder.itemView.findViewById(selectedId);
                String selectedAnswer = selectedRb.getText().toString();

                item.setAnswered(true);
                item.setUserAnswer(selectedAnswer);

                showFeedback(holder, item);
            } else {
                if (onNextClickListener != null) {
                    onNextClickListener.onNextClick(holder.getAdapterPosition());
                }
            }
        });
    }

    private void showFeedback(QuizViewHolder holder, QuestionItem item) {

        for (int i = 0; i < holder.rgOptions.getChildCount(); i++) {
            holder.rgOptions.getChildAt(i).setEnabled(false);
        }

        holder.layoutFeedback.setVisibility(View.VISIBLE);
        holder.btnAction.setText("Next Question");

        String decodedCorrect = Html.fromHtml(item.getCorrectAnswer(), Html.FROM_HTML_MODE_LEGACY).toString();

        if (item.getUserAnswer().equals(decodedCorrect)) {
            // correct
            holder.tvFeedbackResult.setText("Correct! 🎉");
            holder.tvFeedbackResult.setTextColor(Color.GREEN);
            holder.tvCorrectAnswer.setVisibility(View.GONE);
        } else {
            // wrong
            holder.tvFeedbackResult.setText("Incorrect! ❌");
            holder.tvFeedbackResult.setTextColor(Color.RED);
            holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
            holder.tvCorrectAnswer.setText("Correct Answer: " + decodedCorrect);
        }
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class QuizViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDifficulty, tvQuestion, tvFeedbackResult, tvCorrectAnswer;
        RadioGroup rgOptions;
        LinearLayout layoutFeedback;
        Button btnAction;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            rgOptions = itemView.findViewById(R.id.rgOptions);
            layoutFeedback = itemView.findViewById(R.id.layoutFeedback);
            tvFeedbackResult = itemView.findViewById(R.id.tvFeedbackResult);
            tvCorrectAnswer = itemView.findViewById(R.id.tvCorrectAnswer);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}