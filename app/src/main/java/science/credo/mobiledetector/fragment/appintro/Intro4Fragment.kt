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

class Intro4Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_intro4, container, false)
        val finishIntroButton = view.findViewById<Button>(R.id.finish_intro_button)
        finishIntroButton.setOnClickListener{
            startActivity(Intent(context, LauncherActivity::class.java))
        }

        return view
    }
}

