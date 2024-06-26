package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Declare UI elements
    private var etNumOptions: EditText? = null
    private var etVotingOptions: EditText? = null
    private var tvNumVotings: TextView? = null
    private var btnAddVote: Button? = null
    private var btnStartOver: Button? = null
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private var switchResult: Switch? = null
    private var llResults: LinearLayout? = null

    // Declare variables for managing votes and options
    private var votesAmount = 0
    private var checkChangeNumOptions: Int? = 0
    private var numOptions: Int = 3
    private var trimmedStringsOption: MutableList<String> = mutableListOf()
    private var resultMap: MutableMap<String, Int> = mutableMapOf()

    // Handle the result from the second activity
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val voteAdded = intent?.getIntExtra("result_vote", 0)
                val voteMap = intent?.getSerializableExtra("vote_Map") as? MutableMap<String, Int>
                if (voteAdded == 1) {

                    if (votesAmount == 0) {
                        for (key in trimmedStringsOption) {
                            resultMap[key] = 0
                        }
                    }

                    if (voteMap != null) {
                        updateResultMap(resultMap, voteMap)
                    }

                    votesAmount++
                    tvNumVotings?.text = votesAmount.toString()
                    if (switchResult!!.isChecked) { // special case: if switch is checked, make sure to update values
                        displayResults()
                    }

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Vote has been cancelled!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        etNumOptions = findViewById(R.id.etNumOptions)
        etVotingOptions = findViewById(R.id.etVotingOptions)
        tvNumVotings = findViewById(R.id.tvNumVotings)
        btnAddVote = findViewById(R.id.btnAddVote)
        btnStartOver = findViewById(R.id.btnStartOver)
        switchResult = findViewById(R.id.swResults)
        llResults = findViewById(R.id.llResults)

        // Set OnClickListener for the Add Vote button
        btnAddVote?.setOnClickListener {
            onClick(it)
        }

        // Set OnClickListener for the Start Over button
        btnStartOver?.setOnClickListener {
            onClick(it)
        }



        // Set onFocusChangeListener for etNumOptions to validate input when focus is lost
        etNumOptions?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                checkInput(etNumOptions!!)
            }
        }


        // Add a TextWatcher to etVotingOptions to reset votes when text changes
        etVotingOptions?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (votesAmount != 0) {
                    resetVotes("All votes resetted!")

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        // Set OnCheckedChangeListener for switchResult to display results
        switchResult?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                displayResults()
            } else {
                llResults?.removeAllViews()
            }
        }
    }


    // Handle button click events
    private fun onClick(v: View) {
        if (v == btnAddVote) {
            if (etNumOptions!!.text.isEmpty()) {
                etNumOptions!!.setText("3")
            }
            checkInput(etNumOptions!!)
            // must be in this order because the filtered num of options need to be set, before creating the optionlist
            checkInput(etVotingOptions!!)

            // Start the second activity with the options list
            startPreferenceScreenActivity(trimmedStringsOption)
        } else if (v == btnStartOver) {
            resetVotes("Starting anew!")

        }
    }

    // Validate and process input from EditTexts
    @SuppressLint("SetTextI18n")
    private fun checkInput(v: View) {
        if (v == etNumOptions) {
            val numOptionsInput = etNumOptions?.text.toString()

            // Validate number of options
            if (numOptionsInput.isNotEmpty()) {
                val numOptions = numOptionsInput.toInt()
                if (numOptions < 2) {
                    etNumOptions!!.setText("2")
                } else if (numOptions > 10) {
                    etNumOptions!!.setText("10")
                }
            }

            // Check if the input has changed and reset votes if necessary
            if (votesAmount == 0) {
                checkChangeNumOptions = etNumOptions?.text.toString().toIntOrNull() ?: 3
            } else {
                // Special Case: if focus isnt lost (etNumOptions) focus and there for the changes cannot be detected, this handles it
                if(numOptionsInput != checkChangeNumOptions.toString() && etNumOptions!!.text.isNotEmpty()) {
                    resetVotes("All votes resetted!")

                }
            }


        } else if (v == etVotingOptions) {

            // Get number of options or default to 3
            numOptions = etNumOptions?.text.toString().toIntOrNull() ?: 3

            // Handle empty or invalid input
            val options: MutableList<String>
            if (etVotingOptions?.text?.isNotEmpty() == true) {
                options = etVotingOptions?.text
                    .toString()
                    .split(",")
                    .map { it.trim() } // Remove spaces around each element
                    .filter { it.isNotEmpty() } // Remove empty strings
                    .toMutableList()
            } else {
                // Create default options based on numOptions
                options = (1..numOptions).map { "Option $it" }.toMutableList()
            }

            trimmedStringsOption = adjustOptions(options, numOptions)
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

    // Update the resultMap with new voteMap
    private fun updateResultMap(resultsMap: MutableMap<String, Int>, voteMap: MutableMap<String, Int>) {
        for ((key, value) in voteMap) {
            resultsMap[key] = resultsMap.getOrDefault(key, 0) + value
        }
    }

    // Reset the votes and results
    private fun resetVotes(message: String) {
        resultMap.clear()
        llResults?.removeAllViews()
        votesAmount = 0
        tvNumVotings?.text = "0"

        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        switchResult!!.isChecked = false
    }

    private fun displayResults() {
        if (resultMap.isNotEmpty()) {
            llResults?.removeAllViews()
            val maxEntry = resultMap.maxByOrNull { it.value }
            for ((option, value: Int) in resultMap) {
                // Create TextView to display option name
                val nameOption = TextView(this).apply {
                    if (maxEntry != null && value == maxEntry.value) {
                        text = "*** $option -> $value points ***"
                    } else {
                        text = "$option -> $value points"
                    }
                    gravity = Gravity.CENTER
                }
                llResults!!.addView(nameOption)
            }
        }
    }

}
