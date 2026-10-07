package cs.linda.iscg7424assessment2quizapp;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionItem {
    private String category;
    private String type;
    private String difficulty;
    private String question;

    @SerializedName("correct_answer")
    private String correctAnswer;

    @SerializedName("incorrect_answers")
    private List<String> incorrectAnswers;

    private boolean isAnswered = false;
    private String userAnswer;

    // Getters & Setters
    public String getCategory() { return category; }
    public String getType() { return type; }
    public String getDifficulty() { return difficulty; }
    public String getQuestion() { return question; }
    public String getCorrectAnswer() { return correctAnswer; }
    public List<String> getIncorrectAnswers() { return incorrectAnswers; }

    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }
    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }


    public List<String> getShuffledOptions() {
        List<String> options = new ArrayList<>();
        if (incorrectAnswers != null) {
            options.addAll(incorrectAnswers);
        }
        if (correctAnswer != null) {
            options.add(correctAnswer);
        }
        Collections.shuffle(options);
        return options;
    }
}
