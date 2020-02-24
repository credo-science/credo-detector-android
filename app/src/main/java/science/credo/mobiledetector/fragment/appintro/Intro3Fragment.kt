package science.credo.mobiledetector.fragment.appintro

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import science.credo.mobiledetector.LauncherActivity

import science.credo.mobiledetector.R

class Intro3Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_intro3, container, false)
        val textView = view.findViewById<TextView>(R.id.introDescriptionFragment)
        textView.setText("I am working on some image and txt that introduce user into our app :) ")

        val imageView = view.findViewById<ImageView>(R.id.imgMain)
        imageView.setImageResource(R.mipmap.ic_launcher)

        val finishIntroButton = view.findViewById<Button>(R.id.finish_intro_button)
        finishIntroButton.setOnClickListener{
            startActivity(Intent(context, LauncherActivity::class.java))

        }

        return view
    }
}

