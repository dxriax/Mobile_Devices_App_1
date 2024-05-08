package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private lateinit var etNumOptions : EditText
    private lateinit var etVotingOptions :EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNumOptions = findViewById(R.id.etNumOptions)
        etVotingOptions = findViewById(R.id.etVotingOptions)



    etNumOptions.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
            // Hier wird der Code ausgeführt, wenn der Fokus vom EditText-Feld etNumOptions weggeht
            checkInput()
        }
    }


}



    private fun checkInput () {
        val numOptionsInput = etNumOptions.text.toString()

        // Check number of Options
        if (numOptionsInput.isNotEmpty() && numOptionsInput.toIntOrNull() !in 2..10) {
            etNumOptions.setText("")
            Toast.makeText(this@MainActivity, "Number of Options must be minimum 2 and maximum 10", Toast.LENGTH_SHORT).show()
        }
    }


    // Add a Vote - will fill missing string options and remove extra ones
    //Switch Screens by pressing "Add a Vote - Button"
    fun onAddVoteButtonClick() { // 1. diese funktion wird aufgerufen , und nacheinander die nächsten ausgeführt innerhalb
        val numOptions = etNumOptions.text.toString().toIntOrNull() ?: 3
        var trimmedStringsOption = etVotingOptions.text.toString().split(",").map { it.trim() }.toMutableList()

        // String adjustment - size of the list will be equal to number of options entered
        trimmedStringsOption = adjustOptions(trimmedStringsOption, numOptions) // 2. fun adjustOptions

        // switch acvtivity managed by other function
        startPreferenceScreenActivity(numOptions, trimmedStringsOption)
    }

    private fun adjustOptions(options: MutableList<String>, numOptions: Int): MutableList<String> {
        // Ensure that the size of trimmedStringsOption matches numOptions
        while (options.size < numOptions) {
            // Add missing options with "Option x" naming convention
            val index: Int = options.size + 1
            options.add("Option $index")
        }

        return options.subList(0, numOptions).toMutableList()
    }

    private fun startPreferenceScreenActivity(numOptions: Int, options: MutableList<String>) { // 3. fun Screen wechsel
        val intent = Intent(this, PreferenceScreen::class.java)
        intent.putExtra("numOptions", numOptions)
        intent.putStringArrayListExtra("trimmedStringsOption", ArrayList(options))
        startActivity(intent)
    }








}
