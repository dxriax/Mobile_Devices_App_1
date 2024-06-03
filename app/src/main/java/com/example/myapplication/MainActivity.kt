package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var etNumOptions: EditText? = null
    private var etVotingOptions: EditText? = null
    private var tvNumVotings: TextView? = null
    private var btnAddVote: Button? = null
    private var btnStartOver: Button? = null
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private var switchResult: Switch? = null
    private var llResults: LinearLayout? = null


    private var votesAmount = 0
    private var checkChangeINT: Int? = 0
    private var numOptions: Int = 3
    private var trimmedStringsOption: MutableList<String> = mutableListOf()
    private var resultMap: MutableMap<String, Int> = mutableMapOf()

    // This handles the result from the second activity
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
                if (votesAmount != 0) {
                    resetVotes()
                }
                checkInput(etNumOptions!!)
            }
        }


        etVotingOptions?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (votesAmount != 0 ) { // && !checkChangeLIST!!.equals(trimmedStringsOption
                    resetVotes()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        switchResult?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {

                // FOR SCHLEIFE UM TEXT VIEWS MIT RICHTIGEN ERGEBNISSEN ANZEIGEN
                val maxEntry = resultMap.maxByOrNull { it.value }
                for ((option, value: Int) in resultMap) {
                    // Create TextView to display option name
                    val nameOption = TextView(this).apply {
                        // Check if the current entry is the maximum entry
                        if (maxEntry != null && value == maxEntry.value) {
                            text = "*** $option -> $value points ***"
                        } else {
                            text = "$option -> $value points"
                        }
                        gravity = Gravity.CENTER
                    }
                    llResults!!.addView(nameOption)
                }
            } else {
                llResults?.removeAllViews()
            }
        }
    }


    // Handle button click events
    private fun onClick(v: View) {
        if (v == btnAddVote) {
            checkInput(etVotingOptions!!)
            // Start the second activity with the options list
            startPreferenceScreenActivity(trimmedStringsOption)
        } else if (v == btnStartOver) {
            resetVotes()
        }
    }

    // Validate and process input from EditTexts
    @SuppressLint("SetTextI18n")
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
                    resetVotes()
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
                    .map { it.trim() } // Entfernen von Leerzeichen um jedes Element herum
                    .filter { it.isNotEmpty() } // Entfernen leerer Strings
                    .toMutableList()
            } else {
                // Erstellen Sie Standardoptionen basierend auf numOptions
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

    private fun updateResultMap(resultsMap: MutableMap<String, Int>, voteMap: MutableMap<String, Int>) {
        for ((key, value) in voteMap) {
            resultsMap[key] = resultsMap.getOrDefault(key, 0) + value
        }
    }

    private fun resetVotes () {
        resultMap.clear()
        llResults?.removeAllViews()
        votesAmount = 0
        tvNumVotings?.text = "0"

    }


}
