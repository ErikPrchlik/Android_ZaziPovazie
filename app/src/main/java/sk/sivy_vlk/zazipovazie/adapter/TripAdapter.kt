package sk.sivy_vlk.zazipovazie.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.activity.TripDetailActivity
import sk.sivy_vlk.zazipovazie.model.Trip
import sk.sivy_vlk.zazipovazie.model.TripCategory

class TripAdapter(private val trips: List<Trip>) :
    RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = trips[position]
        // Bind trip data
        holder.bind(trip)
        holder.itemView.setOnClickListener {
            // Start detail activity
            val intent = Intent(holder.itemView.context, TripDetailActivity::class.java)
            intent.putExtra("TRIP", trip) // Pass any necessary data to the detail activity
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return trips.size
    }

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tripName)

        // Bind views
        fun bind(trip: Trip) {
            // Bind trip category data
            titleTextView.text = trip.name

        }
    }


}