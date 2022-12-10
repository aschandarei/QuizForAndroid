package net.tchekov.quiz

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.google.gson.GsonBuilder
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {
    private var checkBoxes: List<CheckBox> = listOf()
    private var answerBoxes: MutableList<CheckBox> = mutableListOf()
    private var questions: List<Question> = listOf()
    private var index = 0

    private lateinit var textBoxQuestion: TextView
    private lateinit var textBoxInfo: TextView
    private lateinit var context: Context
    private var reveal: Boolean = false

    /// <summary>
    /// Maximum achievable score
    /// </summary>
    private var highScores: Float = 0.0F
    private var highScoresString = ""


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
            reveal = if (buttonReveal.text == getString(R.string.reveal)) {
                buttonReveal.setText(R.string.hide)
                true
            } else {
                buttonReveal.setText(R.string.reveal)
                false
            }
            setCheckBoxColor()
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
            var achievable = 0.0F
            for (box in answerBoxes) {
                val answer = box.tag as Answer
                if (answer.correct) achievable++
                if (box.isChecked && answer.correct) points++
            }

            scored[index] = points

            val dialog: Dialog
            when (points) {
                0.0F -> {
                    dialog = createToast(getString(R.string.wrong), "#ffffff", "#ff0000")
                }
                achievable -> {
                    dialog = createToast(getString(R.string.correct), "#000000", "#00ff00")
                }
                else -> {
                    dialog = createToast(getString(R.string.incomplete), "#000000", "#ffc300")
                }
            }

            object : CountDownTimer(1000, 1000) {
                override fun onTick(p: Long) {}

                override fun onFinish() {
                    dialog.dismiss()
                    resetBoxes()
                    index++
                    askQuestion()
                }
            }.start()

        }


        textBoxQuestion = findViewById(R.id.textview_question)
        textBoxInfo = findViewById(R.id.textview_info)

        val inputStream: InputStream = context.resources.openRawResource(R.raw.quiz)
        val jsonString: String = Scanner(inputStream).useDelimiter("\\A").next()
        val gson = GsonBuilder().create()
        val quiz = gson.fromJson(jsonString, Quiz::class.java)
        this.title = quiz.name
        questions = quiz.questions.shuffled()
        for (question in questions) {
            scored.add(0.0F)
            for (answer in question.answers) if (answer.correct) highScores++
        }
        highScoresString = "%.0f".format(highScores)
        askQuestion()
    }

    private fun resetBoxes() {
        for (box in checkBoxes) {
            box.visibility = View.INVISIBLE
            box.isChecked = false
        }

        setCheckBoxColor()
    }

    private fun setCheckBoxColor() {
        if (reveal) {
            for (box in answerBoxes) {
                val answer = box.tag as Answer
                if (answer.correct) {
                    box.setTextColor(Color.RED)
                } else {
                    box.setTextColor(Color.BLACK)
                }
            }
        } else {
            for (box in answerBoxes) box.setTextColor(Color.BLACK)
        }
    }

    private fun askQuestion() {
        var ownScore = 0.0F
        for (score in scored) ownScore += score
        val percent = ownScore / highScores * 100
        val scoredPercent = "%.2f".format(percent)
        val ownScoreString = "%.0f".format(ownScore)

        if (index >= questions.size) {
            createToast(
                "Finished!\r\nScored: $ownScoreString/$highScoresString\r\n$scoredPercent",
                "#000000",
                "#ffffff"
            )
            return
        }


        val question = questions[index]
        textBoxQuestion.text = question.content
        val infoText =
            "Question ${index + 1}/${questions.size} - ($ownScoreString/$highScoresString - $scoredPercent%)"
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

    private fun createToast(text: String, textColor: String, backgroundColor: String): Dialog {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_toast)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val textView = dialog.findViewById<TextView>(R.id.toast_content)
        val shape = textView.background.current
        shape.clearColorFilter()
        shape.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            Color.parseColor(backgroundColor), BlendModeCompat.SRC_ATOP
        )
        textView.text = text
        textView.setTextColor(Color.parseColor(textColor))
        dialog.show()
        return dialog
    }
}