package org.koin.sample.androidx

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    // inject & ViewModel
    val coffeeViewModel : CoffeeViewModel by viewModel()

    private val button : Button by lazy { findViewById(R.id.main_button) }
    private val textView : TextView by lazy { findViewById(R.id.main_text) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity)
        title = "Android Coffee Maker"

        button.setOnClickListener {
            textView.text = "I need coffee"
        }

        coffeeViewModel.coffeeMaker.brew()
    }
}