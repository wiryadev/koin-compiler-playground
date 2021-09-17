package org.koin.sample.androidx

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.main_activity.*
import org.koin.sample.android.R

class MainActivity : AppCompatActivity() {

    // inject & ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        title = "Android Coffee Maker"

        main_button.setOnClickListener {
            findViewById<TextView>(R.id.main_text).text = "I need coffee"
        }
    }
}