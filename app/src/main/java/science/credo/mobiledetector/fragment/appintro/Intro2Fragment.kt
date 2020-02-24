package science.credo.mobiledetector.fragment.appintro

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import science.credo.mobiledetector.R

class Intro2Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_intro2, container, false)
        val textView = view.findViewById<TextView>(R.id.introDescriptionFragment)
        textView.setText(" User see this on his first journey and can launch it once more from menu / settings. ")

        val imageView = view.findViewById<ImageView>(R.id.imgMain)
        imageView.setImageResource(R.mipmap.ic_launcher)

        return view
    }
}

