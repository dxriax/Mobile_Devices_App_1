package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PreferenceScreen : AppCompatActivity() {

    private var btnConfirm : Button? = null
    private var btnCancel : Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_screen)

        btnConfirm = findViewById<Button>(R.id.btnConfirm) as Button
        btnCancel = findViewById<Button>(R.id.btnCancel) as Button


        val trimmedStringsList = intent.getStringArrayListExtra("trimmedStringsOption")

        val numOptions = trimmedStringsList?.size


        btnConfirm?.setOnClickListener {
            onClick(it)
        }
        btnCancel?.setOnClickListener {
            onClick(it)
        }





    }

    fun onClick(v: View) {
        // Add a Vote - will fill missing string options and remove extra ones
        //Switch Screens by pressing "Add a Vote - Button"
        if (v == btnConfirm) {
            // Perform your calculation
            val voteAdded = 1

            // noch mehr daten zum geben, result points

            // Set the result and finish the activity
            val resultIntent = Intent()
            resultIntent.putExtra("result_vote", voteAdded)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        } else if (v == btnCancel) {
            // Perform your calculation
            val voteAdded = 0

            // Set the result and finish the activity
            val resultIntent = Intent()
            resultIntent.putExtra("result_vote", voteAdded)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}