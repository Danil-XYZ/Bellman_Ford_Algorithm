package com.example.bellmanfordalgorithm

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.bellmanfordalgorithm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var customView: CustomView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        customView = findViewById(R.id.customView)
        textView = findViewById(R.id.textView)
    }

    fun onBackClick(view: View) {
        customView.stepBack()
    }

    fun onDeleteClick(view: View) {
        textView.text = ""
        customView.delete()
    }

    @SuppressLint("SuspiciousIndentation")
    fun onInfoClick(view: View) {
        val dist = customView.getInfo()
            if (dist != null) {
                textView.text = customView.infoStr

                textView.text = textView.text.toString() + "\nNode: \t Dist:"
                for (i in dist.indices) {

                    textView.text = textView.text.toString() + "\n$i \t\t\t\t\t\t ${dist[i]}"
                }
            } else Toast.makeText(this, "Create the path", Toast.LENGTH_SHORT).show()

    }

    @SuppressLint("ResourceAsColor")
    fun onModeClick(view: View) = with(binding) {
        textView.text = ""
        button1.setBackgroundResource(R.drawable.left_rounded_button)
        button2.setBackgroundResource(R.drawable.middle_button)
        button3.setBackgroundResource(R.drawable.right_rounded_button)
        when (view.id) {
            R.id.button1 -> {
                customView.changeMode(CustomView.Mode.NODE)
                button1.setBackgroundResource(R.drawable.left_rounded_button_on)
            }

            R.id.button2 -> {
                customView.changeMode(CustomView.Mode.EDGE)
                button2.setBackgroundResource(R.drawable.middle_button_on)
            }

            R.id.button3 -> {
                customView.changeMode(CustomView.Mode.PATH)
                button3.setBackgroundResource(R.drawable.right_rounded_button_on)
            }
        }
    }
}