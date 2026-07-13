package science.credo.mobiledetector.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.UserInfoWrapper

class LinksFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_links, container, false)
        
        val userLinkView = view.findViewById<TextView>(R.id.user_detection_link)
        val username = UserInfoWrapper(context!!).login
        if (username.isNotEmpty()) {
            val baseText = "My Detections:\nhttps://api.credo.science/web/user/"
            userLinkView.text = baseText + username + "/"
        }
        
        return view
    }

    companion object {
        fun newInstance(): LinksFragment = LinksFragment()
    }
}
