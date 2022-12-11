# QuizForAndroid

Simple Android App for multiple choice tests

Test content will be needed in order to use the app  

The test content must be placed in the quiz.json file in the \data folder of your device
(or you can adjust the source code and make it look on some other place)

The JSON Structure is slightly different from the json for Desktop (Windows) application
```
  {
    "name": "Place the name of your quiz here",
    "questions": [ <--- questions as array
      {
        "content": "First question", <--- question content
        "answers": [ <--- answers as array
          {
            "content": "First answer", <--- answer content
            "correct": false <--- this answer is not correct
          },
          {
            "content": "Second answer",
            "correct": false
          },
          {
            "content": "Correct answer",
            "correct": true <--- this is the correct answer (more than one answer can be marked as correct)
          }
        ]
      },
      ...
      ]
  }
```

Every correct answer will count as one point, so the maximal achievable points may be greater than the count of questions.

The app calculates achieved scores and percentage after every answered question (press of button Next)

Cheating is possible. Button Reveal will show which answers are correct.

Button Back allows to go one question back - while resetting the achieved points for that question.
