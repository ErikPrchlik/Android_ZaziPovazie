package sk.sivy_vlk.zazipovazie.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.activity.AboutActivity
import sk.sivy_vlk.zazipovazie.adapter.MapCategoryAdapter
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.model.MapObjectsByCategory
import sk.sivy_vlk.zazipovazie.utils.serializable

class CategoryScrollingFragment : Fragment() {

    interface OnCategoryCheckedListener {
        fun onCategoryChecked(category: MapObjectsByCategory, isChecked: Boolean)
    }
    interface OnCategoryMapObjectClickedListener {
        fun categoryMapObjectClicked(mapObject: MapObject)
    }

    private var categoryCheckedListener: OnCategoryCheckedListener? = null
    private var categoryMapObjectClickedListener: OnCategoryMapObjectClickedListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_category_scrolling, container, false)

        val context = requireActivity().baseContext

        val aboutView = view.findViewById<CardView>(R.id.about)
        aboutView.setOnClickListener {
            // Handle about view click event
            val intent = Intent(context, AboutActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        val mapObjectsByCategory = arguments?.serializable("MAP_OBJECTS_BY_CATEGORY") as? ArrayList<MapObjectsByCategory>
        mapObjectsByCategory?.let {
            val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rv_categories)
            categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            categoriesRecyclerView.adapter = MapCategoryAdapter(
                context,
                it,
                { category, isChecked ->
                    // Pass the state change to the MainActivity via the interface
                    categoryCheckedListener?.onCategoryChecked(category, isChecked)
                },
                { mapObject ->
                    categoryMapObjectClickedListener?.categoryMapObjectClicked(mapObject)
                }
            )
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCategoryCheckedListener) {
            categoryCheckedListener = context
        }
        if (context is OnCategoryMapObjectClickedListener) {
            categoryMapObjectClickedListener = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        categoryCheckedListener = null
        categoryMapObjectClickedListener = null
    }

    companion object {
        fun newInstance(mapObjectsByCategory: ArrayList<MapObjectsByCategory>): CategoryScrollingFragment {
            val fragment = CategoryScrollingFragment()
            val args = Bundle()
            args.putSerializable("MAP_OBJECTS_BY_CATEGORY", mapObjectsByCategory)
            fragment.arguments = args
            return fragment
        }
    }

}