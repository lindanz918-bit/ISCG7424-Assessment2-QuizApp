package cs.linda.iscg7424assessment2quizapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuestionModel {
    private String question;
    @SerializedName("correct_answer")
    private String correctAnswer;
    @SerializedName("incorrect_answers")
    private List<String> incorrectAnswers;
    private List<String> shuffledOptions;
    private String category;
    private String difficulty;
    private String type;

    private String userAnswer;
    private boolean isAnswered;

    public QuestionModel() {}

    // Getter & Setter
    public String getQuestion() { return question; }
    public void setQuestionText(String questionText) { this.question = question; }

    public List<String> getShuffledOptions() { return shuffledOptions; }
    public void setShuffledOptions(List<String> shuffledOptions) { this.shuffledOptions = shuffledOptions; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public List<String> getIncorrectAnswers() { return incorrectAnswers; }
    public void setIncorrectAnswers(List<String> incorrectAnswers) { this.incorrectAnswers = incorrectAnswers; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }
}
