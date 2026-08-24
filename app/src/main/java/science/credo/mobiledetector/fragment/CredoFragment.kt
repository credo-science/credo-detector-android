package science.credo.mobiledetector.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import kotlinx.android.synthetic.main.activity_launcher.*
//import kotlinx.android.synthetic.main.activity_launcher.view.*
//import kotlinx.android.synthetic.main.fragment_credo.view.*
import science.credo.mobiledetector.databinding.FragmentCredoBinding
import science.credo.mobiledetector.R
import science.credo.mobiledetector.database.ConfigurationWrapper


class CredoFragment : Fragment() {
    private var _binding: FragmentCredoBinding? = null
    private val binding get() = _binding!!
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCredoBinding.inflate(inflater, container, false)

        binding.goButton.setOnClickListener {
            mListener!!.onGoToExperiment()
        }

        binding.go2Button.setOnClickListener {
            mListener!!.onGoToExperiment()
        }

        return binding.root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    interface OnFragmentInteractionListener {
        fun onGoToExperiment()
    }

    companion object {


        fun newInstance(): CredoFragment {
            return CredoFragment()
        }
    }
}
