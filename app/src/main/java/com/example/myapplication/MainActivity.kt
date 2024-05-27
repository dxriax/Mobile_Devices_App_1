package com.example.myapplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var etNumOptions: EditText? = null
    private var etVotingOptions: EditText? = null
    private var tvNumVotings: TextView? = null
    private var btnAddVote: Button? = null

    private var votesAmount = 0
    private var checkChangeINT: Int? = 0
    private var checkChangeLIST: MutableList<String>? = null
    private var numOptions: Int = 3
    private var trimmedStringsOption: MutableList<String> = mutableListOf()

    // This handles the result from the second activity
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val voteAdded = intent?.getIntExtra("result_vote", 0)
            if (voteAdded == 1) {
                votesAmount++
                tvNumVotings?.text = votesAmount.toString()
            } else {
                Toast.makeText(applicationContext, "Vote has been cancelled!", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        etNumOptions = findViewById(R.id.etNumOptions)
        etVotingOptions = findViewById(R.id.etVotingOptions)
        tvNumVotings = findViewById(R.id.tvNumVotings)
        btnAddVote = findViewById(R.id.btnAddVote)

        // Set OnClickListener for the Add Vote button
        btnAddVote?.setOnClickListener {
            onClick(it)
        }

        // Set onFocusChangeListener for etNumOptions to validate input when focus is lost
        etNumOptions?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkInput(etNumOptions!!)
            }
        }

        // Set onFocusChangeListener for etVotingOptions to process input when focus is lost
        etVotingOptions?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkInput(etVotingOptions!!)
            }
        }
    }

    // Handle button click events
    private fun onClick(v: View) {
        if (v == btnAddVote) {
            // Start the second activity with the options list
            startPreferenceScreenActivity(trimmedStringsOption)
        }
    }

    // Validate and process input from EditTexts
    private fun checkInput(v: View) {
        if (v == etNumOptions) {
            val numOptionsInput = etNumOptions?.text.toString()

            // Validate number of options
            if (numOptionsInput.isNotEmpty()) {
                val numOptions = numOptionsInput.toIntOrNull()
                if (numOptions != null) {
                    if (numOptions < 2) {
                        etNumOptions!!.setText("2")
                    } else if (numOptions > 10) {
                        etNumOptions!!.setText("10")
                    }
                }
            }

            // Check if the input has changed and reset votes if necessary
            if (votesAmount == 0) {
                checkChangeINT = numOptionsInput.toIntOrNull()
            } else {
                if (checkChangeINT != numOptionsInput.toIntOrNull()) {
                    votesAmount = 0
                    tvNumVotings?.text = "0"
                }
            }
        } else if (v == etVotingOptions) {
            // Get number of options or default to 3
            numOptions = etNumOptions?.text.toString().toIntOrNull() ?: 3
            var options: MutableList<String> = mutableListOf()

            // Split the input string into a list of options
            if (etVotingOptions?.text?.isNotEmpty() == true) {
                options = etVotingOptions?.text.toString().split(",").map { it.trim() }.toMutableList()
            }

            // Adjust the options list size to match numOptions
            trimmedStringsOption = adjustOptions(options, numOptions)

            // Reset votesAmount if it's not zero
            if (votesAmount != 0) {
                votesAmount = 0
                tvNumVotings?.text = "0"
            } else {
                checkChangeLIST = trimmedStringsOption
            }
        }
    }

    // Ensure the options list has the correct number of entries
    private fun adjustOptions(options: MutableList<String>?, numOptions: Int): MutableList<String> {
        var length = options?.size ?: 0

        // Add missing options if necessary
        while (length < numOptions) {
            val index: Int = length + 1
            options?.add("Option $index")
            length++
        }

        // Trim the list to the required number of options
        return options?.subList(0, numOptions)?.toMutableList() ?: mutableListOf()
    }

    // Start the second activity and pass the options list
    private fun startPreferenceScreenActivity(options: MutableList<String>) {
        val intent = Intent(this, PreferenceScreen::class.java)
        intent.putStringArrayListExtra("trimmedStringsOption", ArrayList(options))
        startForResult.launch(intent)
    }
}
