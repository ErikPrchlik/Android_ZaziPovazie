package sk.sivy_vlk.zazipovazie.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.activity.AboutActivity
import sk.sivy_vlk.zazipovazie.adapter.MapCategoryAdapter
import sk.sivy_vlk.zazipovazie.model.MapObject
import sk.sivy_vlk.zazipovazie.model.MapObjectsByCategory
import sk.sivy_vlk.zazipovazie.view_model.MapActivityViewModel
import sk.sivy_vlk.zazipovazie.view_model.State

class CategoryScrollingFragment : Fragment() {

    private val viewModel: MapActivityViewModel by activityViewModels()

    private lateinit var mapCategories: ArrayList<MapObjectsByCategory>

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

        observeState(context, view)

        return view
    }

    private fun observeState(context: Context, view: View) {
        lifecycleScope.launch {
            viewModel.mapObjectsState.collect { state ->
                when (state) {
                    is State.Success -> {
                        viewModel.mapCategories.collect { categories ->
                            mapCategories = if (categories is State.Success) categories.data else arrayListOf()
                            val categoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rv_categories)
                            categoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                            categoriesRecyclerView.adapter = MapCategoryAdapter(
                                context,
                                mapCategories,
                                { category, isChecked ->
                                    // Pass the state change to the MainActivity via the interface
                                    categoryCheckedListener?.onCategoryChecked(category, isChecked)
                                },
                                { mapObject ->
                                    categoryMapObjectClickedListener?.categoryMapObjectClicked(mapObject)
                                }
                            )
                        }
                    }
                    is State.NoData -> {}
                    is State.Error -> {}
                    State.Loading -> {}
                }
            }
        }
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
        fun newInstance(): CategoryScrollingFragment {
            return CategoryScrollingFragment()
        }
    }

}