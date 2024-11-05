package sk.sivy_vlk.zazipovazie.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.activity.MapObjectDetailActivity
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.utils.serializable


class InfoWindowFragment : Fragment() {
    private var titleTextView: TextView? = null
    private var snippetTextView: TextView? = null
    private var closeImageView: ImageView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_info_window, container, false)
        titleTextView = view.findViewById(R.id.title)
        snippetTextView = view.findViewById(R.id.snippet)
        closeImageView = view.findViewById(R.id.close)

        // Set data passed from activity or fragment
        val mapObject = arguments?.serializable("MAP_OBJECT") as? MapObject
        if (mapObject != null) {
            titleTextView!!.text = mapObject.name
            snippetTextView!!.text = mapObject.category
        }

        closeImageView?.setOnClickListener {
            // Dismiss fragment
            getParentFragmentManager().beginTransaction().remove(this).commit()
        }

        view.setOnClickListener {
            // Starting a new activity
            val intent = Intent(activity, MapObjectDetailActivity::class.java)
            intent.putExtra("MAP_OBJECT", mapObject)
            startActivity(intent)
        }
        return view
    }

    companion object {
        fun newInstance(mapObject: MapObject?): InfoWindowFragment {
            val fragment = InfoWindowFragment()
            val args = Bundle()
            args.putSerializable("MAP_OBJECT", mapObject)
            fragment.arguments = args
            return fragment
        }
    }
}

