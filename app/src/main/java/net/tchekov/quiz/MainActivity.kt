package net.tchekov.quiz

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private var checkBoxes: List<CheckBox> = listOf()
    private var answerBoxes: MutableList<CheckBox> = mutableListOf()
    private var questions: List<Question> = listOf()
    private var index = 0

    private var quizName: String = "Quiz"
    private lateinit var textBoxQuestion: TextView
    private lateinit var textBoxInfo: TextView
    private lateinit var context: Context

    /// <summary>
    /// Maximum achievable score
    /// </summary>
    private var highScores: Float = 0.0F

    /// <summary>
    /// Achieved score
    /// </summary>
    private var scored: MutableList<Float> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        context = this

        val buttonReveal = findViewById<Button>(R.id.buttonReveal)
        buttonReveal.setOnClickListener {
            for (box in answerBoxes) {
                val answer = box.tag as Answer
                if (answer.correct) {
                    box.setTextColor(Color.RED)
                } else {
                    box.setTextColor(Color.BLACK)
                }
            }
        }

        val checkBoxA = findViewById<CheckBox>(R.id.checkboxA)
        val checkBoxB = findViewById<CheckBox>(R.id.checkboxB)
        val checkBoxC = findViewById<CheckBox>(R.id.checkboxC)
        val checkBoxD = findViewById<CheckBox>(R.id.checkboxD)
        val checkBoxE = findViewById<CheckBox>(R.id.checkboxE)
        checkBoxes = listOf(checkBoxA, checkBoxB, checkBoxC, checkBoxD, checkBoxE)

        resetBoxes()

        val buttonBack = findViewById<Button>(R.id.buttonBack)
        buttonBack.setOnClickListener {
            resetBoxes()
            index--
            if (index < 0) index = 0
            scored[index] = 0.0F
            askQuestion()
        }

        val buttonNext = findViewById<Button>(R.id.buttonNext)
        buttonNext.setOnClickListener {
            var points = 0.0F
            for (box in answerBoxes) {
                if (box.isChecked) {
                    val answer = box.tag as Answer
                    if (answer.correct) points++
                }
            }
            scored[index] = points

            resetBoxes()
            index++
            askQuestion()
        }

        textBoxQuestion = findViewById(R.id.textview_question)
        textBoxInfo = findViewById(R.id.textview_info)

        val inputStream: InputStream = context.resources.openRawResource(R.raw.quiz)
        val jsonString: String = Scanner(inputStream).useDelimiter("\\A").next()
        val gson = GsonBuilder().create()
        val quiz = gson.fromJson(jsonString, Quiz::class.java)
        quizName = quiz.name
        questions = quiz.questions.shuffled()
        for (question in questions) {
            scored.add(0.0F)
            for (answer in question.answers) if (answer.correct) highScores++
        }
        askQuestion()
    }

    private fun resetBoxes() {
        for (box in checkBoxes) {
            box.visibility = View.INVISIBLE
            box.isChecked = false
            box.setTextColor(Color.BLACK)
        }
    }

    private fun askQuestion() {
        var ownScore = 0.0F
        for (score in scored) ownScore += score
        val percent = ownScore / highScores * 100
        val scoredPercent = "%.2f".format(percent)

        if (index >= questions.size) {
            Toast.makeText(
                context,
                "Finished!\r\nScored: $ownScore/$highScores\r\n$scoredPercent",
                Toast.LENGTH_LONG
            ).show()
            return
        }


        val question = questions[index]
        textBoxQuestion.text = question.content
        val infoText =
            "$quizName - Question: ${index + 1}/${questions.size} - ($ownScore/$highScores - $scoredPercent%)"
        textBoxInfo.text = infoText

        val answerCount = question.answers.size - 1
        answerBoxes = mutableListOf()
        for (i in 0..answerCount) {
            answerBoxes.add(checkBoxes[i])
        }

        // Shuffle answer check boxes if more than just two
        if (answerCount > 2) answerBoxes = answerBoxes.shuffled() as MutableList<CheckBox>

        for (i in 0..answerCount) {
            val answer = question.answers[i]
            answerBoxes[i].visibility = View.VISIBLE
            answerBoxes[i].text = answer.content
            answerBoxes[i].tag = answer
        }
    }
}