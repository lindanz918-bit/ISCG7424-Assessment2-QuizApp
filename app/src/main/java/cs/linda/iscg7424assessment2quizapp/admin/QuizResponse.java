package cs.linda.iscg7424assessment2quizapp.admin;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import cs.linda.iscg7424assessment2quizapp.model.QuestionModel;

public class QuizResponse {
    @SerializedName("response_code")
    public int responseCode;

    @SerializedName("results")
    public List<QuestionModel> results;
}