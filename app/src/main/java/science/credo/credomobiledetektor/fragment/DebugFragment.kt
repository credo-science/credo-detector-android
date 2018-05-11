package science.credo.credomobiledetektor.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import science.credo.credomobiledetektor.info.ConfigurationInfo
import science.credo.credomobiledetektor.info.IdentityInfo
import science.credo.credomobiledetektor.info.LocationInfo
import science.credo.credomobiledetektor.R
import science.credo.credomobiledetektor.database.DataManager
import science.credo.credomobiledetektor.detection.Hit
import science.credo.credomobiledetektor.info.HitInfo
import science.credo.credomobiledetektor.network.ServerInterface


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [StatusFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [StatusFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DebugFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    data class Test(
        val locationInfo: LocationInfo.LocationData,
        val identityInfo: IdentityInfo.IdentityData,
        val configurationInfo: ConfigurationInfo.ConfigurationData
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i(TAG, activity.toString())
        Log.i(TAG, "onCreateView")
        val v = inflater!!.inflate(R.layout.fragment_statistics, container, false)
        val tv = v.findViewById<TextView>(R.id.statfragment_textview) as TextView
        tv.text = mParam1 + " Fragment"
        val butgen = v.findViewById<Button>(R.id.statfragment_button_gen) as Button
        val butupl = v.findViewById<Button>(R.id.statfragment_button_upl) as Button
        val butclr = v.findViewById<Button>(R.id.statfragment_button_clrdb)
        val butcclr = v.findViewById<Button>(R.id.statfragment_button_clrcdb)

        var resp = "RESPONSE: null";

        val ld = LocationInfo.getInstance(activity!!.applicationContext)
        val id = IdentityInfo.getInstance(activity!!.applicationContext)
        val ci = ConfigurationInfo(activity!!.applicationContext)

        var hitCounter: Int = 0
        val dataManager = DataManager.getInstance(activity!!.applicationContext)
        val networkInterface = ServerInterface.getDefault(activity!!.applicationContext)

        butgen.setOnClickListener {
            Log.d(TAG, "generate button pressed")
            //@TODO fill missing data
            val hit = Hit(
                HitInfo.FrameData("FRAME_CONTENT", 0, 0, 0, 0, 0, 0, 0),
                LocationInfo.LocationData(12344.0, 13444.0, 3242134.0, 1234.0f, "GSM", 0),
                HitInfo.FactorData(0, 0, 0, 0)
            )
            dataManager.storeHit(hit)
            postUpdate(tv, dataManager)
        }
        butupl.setOnClickListener {
            Log.d(TAG, "upload button pressed")
            doAsync {
//                networkInterface.sendHitsToNetwork()
//                uiThread {
                dataManager.sendHitsToNetwork()
                uiThread{
                    postUpdate(tv, dataManager)
                }
            }


        }
        butclr.setOnClickListener {
            Log.d(TAG, "clear db pressed")
            val hits = dataManager.getHits()
            for (hit in hits) {
                dataManager.removeHit(hit)
            }
            postUpdate(tv, dataManager)
        }
        butcclr.setOnClickListener {
            Log.d(TAG, "clear cached db pressed")
            val hits = dataManager.getCachedHits()
            for (hit in hits) {
                dataManager.removeHit(hit)
            }
            postUpdate(tv, dataManager)
        }


        tv.text = getString(R.string.debug_upload_hits) + dataManager.getHitsNumber() +
                " \n" + getString(R.string.debug_cached_hits) + " " +
                dataManager.getCachedHitsNumber()
        tv.postInvalidate()

        return v
    }

    fun postUpdate(tv: TextView, dataManager: DataManager) {
        tv.text = getString(R.string.debug_to_upload_hits) + dataManager.getHitsNumber() +
                " \n" + getString(R.string.debug_cached_hits) + " " +
                dataManager.getCachedHitsNumber()
        tv.postInvalidate()

    }

    fun onButtonPressed(uri: Uri) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
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

        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        val TAG = "StatusFragment"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatusFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String, param2: String): DebugFragment {
            val fragment = DebugFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
