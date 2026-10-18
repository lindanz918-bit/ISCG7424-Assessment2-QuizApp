package cs.linda.iscg7424assessment2quizapp.model;

public class QuestionHistoryModel {
    private String questionText;
    private String userAnswer;
    private String correctAnswer;
    private boolean correct;

    public QuestionHistoryModel() {}

    public QuestionHistoryModel(String questionText, String userAnswer, String correctAnswer, boolean correct) {
        this.questionText = questionText;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
    }

    public String getQuestionText() { return questionText; }
    public String getUserAnswer() { return userAnswer; }
    public String getCorrectAnswer() { return correctAnswer; }
    public boolean isCorrect() { return correct; }
}