package cs.linda.iscg7424assessment2quizapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
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

import cs.linda.iscg7424assessment2quizapp.R;
import cs.linda.iscg7424assessment2quizapp.model.QuestionModel;

public class QuestionPagerAdapter extends RecyclerView.Adapter<QuestionPagerAdapter.QuestionViewHolder> {

    private Context context;
    private List<QuestionModel> questionList;

    public QuestionPagerAdapter(Context context, List<QuestionModel> questionList) {
        this.context = context;
        this.questionList = questionList;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question_single, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        QuestionModel question = questionList.get(position);

        if (holder.tvCategory != null) {
            holder.tvCategory.setText(question.getCategory() != null ? question.getCategory() : "General");
        }
        if (holder.tvDifficulty != null) {
            holder.tvDifficulty.setText(question.getDifficulty() != null ? question.getDifficulty().toUpperCase() : "EASY");
        }

        if (holder.tvQuestion != null) {
            holder.tvQuestion.setText(decodeHtml(question.getQuestion()));
        }

        holder.rgOptions.removeAllViews();
        List<String> options = question.getShuffledOptions();
        if (options != null) {
            for (int i = 0; i < options.size(); i++) {
                String decodedOption = decodeHtml(options.get(i));
                RadioButton rb = new RadioButton(context);
                rb.setId(View.generateViewId());
                rb.setText(decodedOption);
                rb.setTextSize(16);
                rb.setPadding(12, 12, 12, 12);

                if (question.getUserAnswer() != null && question.getUserAnswer().equals(options.get(i))) {
                    rb.setChecked(true);
                }

                holder.rgOptions.addView(rb);
            }
        }

        if (question.isAnswered()) {
            showFeedbackUI(holder, question);
            disableRadioGroup(holder.rgOptions);
            holder.btnAction.setEnabled(false);
            holder.btnAction.setText("Submitted");
        } else {
            holder.layoutFeedback.setVisibility(View.GONE);
            enableRadioGroup(holder.rgOptions);
            holder.btnAction.setEnabled(true);
            holder.btnAction.setText("Submit Answer");
        }

        holder.btnAction.setOnClickListener(v -> {
            int selectedId = holder.rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(context, "Please select an answer first!", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadioButton = holder.itemView.findViewById(selectedId);
            String selectedAnswer = selectedRadioButton.getText().toString();

            question.setUserAnswer(selectedAnswer);
            question.setAnswered(true);

            showFeedbackUI(holder, question);
            disableRadioGroup(holder.rgOptions);
            holder.btnAction.setEnabled(false);
            holder.btnAction.setText("Submitted");
        });
    }

    private void showFeedbackUI(QuestionViewHolder holder, QuestionModel question) {
        holder.layoutFeedback.setVisibility(View.VISIBLE);

        String userAnswer = question.getUserAnswer();
        String correctAnswer = question.getCorrectAnswer();

        if (correctAnswer != null && correctAnswer.equalsIgnoreCase(userAnswer)) {
            holder.tvFeedbackResult.setText("Correct! 🎉");
            holder.tvFeedbackResult.setTextColor(Color.parseColor("#4CAF50")); // 绿色
            holder.tvCorrectAnswer.setVisibility(View.GONE);
        } else {
            holder.tvFeedbackResult.setText("Incorrect ❌");
            holder.tvFeedbackResult.setTextColor(Color.parseColor("#F44336")); // 红色
            holder.tvCorrectAnswer.setVisibility(View.VISIBLE);
            holder.tvCorrectAnswer.setText("Correct Answer: " + (correctAnswer != null ? correctAnswer : "N/A"));
        }
    }

    private void disableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(false);
        }
    }

    private void enableRadioGroup(RadioGroup radioGroup) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return questionList != null ? questionList.size() : 0;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDifficulty, tvQuestion, tvFeedbackResult, tvCorrectAnswer;
        RadioGroup rgOptions;
        LinearLayout layoutFeedback;
        Button btnAction;

        public QuestionViewHolder(@NonNull View itemView) {
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

    private String decodeHtml(String text) {
        if (text == null) return "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(text).toString();
        }
    }
}