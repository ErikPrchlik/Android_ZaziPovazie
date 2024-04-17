package sk.sivy_vlk.zazipovazie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.model.TripCategory

class TripCategoryAdapter(private val tripCategories: List<TripCategory>) :
    RecyclerView.Adapter<TripCategoryAdapter.TripCategoryViewHolder>() {

    fun toggleCategoryExpansion(position: Int) {
        val category = tripCategories[position]
        category.isExpanded = !category.isExpanded
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_category, parent, false)
        return TripCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripCategoryViewHolder, position: Int) {
        val tripCategory = tripCategories[position]
        holder.bind(tripCategory)
        holder.itemView.setOnClickListener {
            toggleCategoryExpansion(position)
        }
    }

    override fun getItemCount(): Int {
        return tripCategories.size
    }

    class TripCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryTitleTextView: TextView = itemView.findViewById(R.id.categoryTitleTextView)
        private val tripsRecyclerView: RecyclerView = itemView.findViewById(R.id.tripsRecyclerView)

        fun bind(tripCategory: TripCategory) {
            // Bind trip category data
            categoryTitleTextView.text = tripCategory.category

            // Set up sub-items RecyclerView
            tripsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)
            val tripAdapter = TripAdapter(tripCategory.trips)
            tripsRecyclerView.adapter = tripAdapter

            // Set visibility of sub-items RecyclerView based on expanded state
            tripsRecyclerView.visibility = if (tripCategory.isExpanded) View.VISIBLE else View.GONE
        }
    }
}