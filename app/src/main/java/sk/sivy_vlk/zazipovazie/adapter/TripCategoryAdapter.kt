package sk.sivy_vlk.zazipovazie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.model.TripCategory

class TripCategoryAdapter(private val tripCategories: List<TripCategory>) :
    RecyclerView.Adapter<TripCategoryAdapter.TripCategoryViewHolder>() {

    inner class TripCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Bind views and set click listeners
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip_category, parent, false)
        return TripCategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripCategoryViewHolder, position: Int) {
        val tripCategory = tripCategories[position]
        // Bind trip category data
        holder.itemView.setOnClickListener {
            // Handle item click to expand/collapse
        }
        // Bind sub-items if expanded
        if (tripCategory.isExpanded) {
            val tripsRecyclerView: RecyclerView = holder.itemView.findViewById(R.id.tripsRecyclerView)
            tripsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
            tripsRecyclerView.adapter = TripAdapter(tripCategory.trips)
        }
    }

    override fun getItemCount(): Int {
        return tripCategories.size
    }
}

