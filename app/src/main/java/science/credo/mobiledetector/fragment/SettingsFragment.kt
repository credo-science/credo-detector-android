package science.credo.mobiledetector.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.doAsync
import science.credo.mobiledetector.CredoApplication
import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.DataManager
import science.credo.mobiledetector.detection.Hit
import science.credo.mobiledetector.info.ConfigurationInfo
import science.credo.mobiledetector.info.IdentityInfo
import science.credo.mobiledetector.info.LocationInfo

/**
 * Created by poznan on 18/09/2017.
 */



class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }


    private var mListener: OnFragmentInteractionListener? = null



    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context is OnFragmentInteractionListener) {
            mListener = context
            ConfigurationInfo(context).isDetectionOn = false
            (context.applicationContext as CredoApplication).turnOffDetection()
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {

        val TAG = "SettingsFragment"


        fun newInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            return fragment
        }
    }
}// Required empty public constructor
