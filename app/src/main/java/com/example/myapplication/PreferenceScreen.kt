package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PreferenceScreen : AppCompatActivity() {

    private var btnConfirm: Button? = null
    private var btnCancel: Button? = null
    private var voteMap: MutableMap<String, Int> = mutableMapOf()
    private var options: ArrayList<String> = arrayListOf()
    private var hasNonUniqueValues = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preference_screen)

        // Initialize UI elements
        btnConfirm = findViewById<Button>(R.id.btnConfirm)
        btnCancel = findViewById<Button>(R.id.btnCancel)

        // Retrieve options from the intent
        options = intent.getStringArrayListExtra("trimmedStringsOption") ?: arrayListOf()

        // Finish the activity if no options are provided
        if (options.isEmpty()) {
            Toast.makeText(this, "No options provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Initialize the vote map with options and default values
        voteMap = options.associateWith { 0 }.toMutableMap()

        // Find LinearLayouts for displaying options and values
        val llColoredView = findViewById<LinearLayout>(R.id.vColoredView)
        val llPoints = findViewById<LinearLayout>(R.id.llPoints)

        // Create TextView and SeekBar for each option
        for (option in options) {
            // Create TextView to display option name
            val nameOption = TextView(this).apply {
                text = option
            }
            llColoredView.addView(nameOption)

            // Create SeekBar for selecting option value
            val seekBar = SeekBar(this).apply {
                progress = 0
                max = 10
            }
            llColoredView.addView(seekBar)

            // Create TextView to display option value
            val tvOptionPoints = TextView(this).apply {
                text = ""
                gravity = Gravity.CENTER
            }
            llPoints.addView(tvOptionPoints)

            // Set SeekBar change listener to update values
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val index = llColoredView.indexOfChild(seekBar) / 2
                    val optionText = options.getOrNull(index) ?: return
                    voteMap[optionText] = progress
                    updateValues(llPoints)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Set OnClickListener for Confirm and Cancel buttons
        btnConfirm?.setOnClickListener {
            onClick(it)
        }
        btnCancel?.setOnClickListener {
            onClick(it)
        }
    }

    // Function to update option values displayed in TextViews
    fun updateValues(linearLayout: LinearLayout) {
        val nonUniqueValues = voteMap.entries.groupBy { it.value }.filterValues { it.size > 1 }.keys
        hasNonUniqueValues = nonUniqueValues.isNotEmpty()
        for ((index, option) in options.withIndex()) {
            val valueTextView = linearLayout.getChildAt(index) as TextView
            val value = voteMap[option] ?: 0
            valueTextView.text = when {
                hasNonUniqueValues && value in nonUniqueValues -> "$option -> <not unique>"
                else -> "$option -> $value"
            }
        }
    }

    // Function to handle button clicks
    private fun onClick(v: View) {
        val voteAdded = if (v == btnConfirm) 1 else 0
        if (hasNonUniqueValues && voteAdded == 1) {
            Toast.makeText(this, "Votes are not unique!", Toast.LENGTH_SHORT).show()
            return
        }
        // Return the vote result to the calling activity
        val resultIntent = Intent().apply {
            putExtra("result_vote", voteAdded)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}
