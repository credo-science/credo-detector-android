package science.credo.mobiledetector.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import science.credo.mobiledetector.R
import science.credo.mobiledetector.login.LoginActivity

class Intro4Fragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_intro4, container, false)
        val finishIntroButton = view.findViewById<Button>(R.id.finish_intro_button)
        finishIntroButton.setOnClickListener{
            startActivity(Intent(context, LoginActivity::class.java))
        }

        return view
    }
}