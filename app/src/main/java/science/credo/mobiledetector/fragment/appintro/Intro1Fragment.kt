package science.credo.mobiledetector.fragment.appintro

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ImageView
import science.credo.mobiledetector.R


class Intro1Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_intro1, container, false)
        val textView = view.findViewById<TextView>(R.id.introDescriptionFragment)
        textView.setText("I am working on some image, background and txt that introduce user into our app :) ")

        val imageView = view.findViewById<ImageView>(R.id.imgMain)
        imageView.setImageResource(R.mipmap.ic_launcher)

        return view
    }
}

