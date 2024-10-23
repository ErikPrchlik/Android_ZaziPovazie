package sk.sivy_vlk.zazipovazie.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.model.MapObjectsByCategory

class MapCategoryAdapter(
    private val categories: List<MapObjectsByCategory>,
    private val categoryCheckedListener: (MapObjectsByCategory, Boolean) -> Unit) :
    RecyclerView.Adapter<MapCategoryAdapter.CategoryViewHolder>() {

    private fun toggleCategoryExpansion(position: Int) {
        val category = categories[position]
        category.isExpanded = !category.isExpanded
        notifyItemChanged(position)
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        val showCategory: CheckBox = itemView.findViewById(R.id.show_category)
        val objectsRecyclerView: RecyclerView = itemView.findViewById(R.id.rv_objects)
        val expandButton: View = itemView.findViewById(R.id.expandButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        // Set category name
        holder.categoryName.text = category.name

        // Set up the RecyclerView for objects in this category
//        holder.objectsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
        holder.objectsRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.objectsRecyclerView.adapter = MapObjectAdapter(category.mapObjects)

        holder.showCategory.isChecked = category.isShowed
        holder.showCategory.setOnCheckedChangeListener { _, isChecked ->
            // Notify fragment/activity about the checkbox state change
            categoryCheckedListener(category, isChecked)
        }

        holder.objectsRecyclerView.visibility = if (category.isExpanded) View.VISIBLE else View.GONE
        holder.expandButton.rotation = if (category.isExpanded) 180f else 0f

        holder.itemView.setOnClickListener {
            toggleCategoryExpansion(position)
        }
    }

    override fun getItemCount(): Int {
        return categories.size
    }
}