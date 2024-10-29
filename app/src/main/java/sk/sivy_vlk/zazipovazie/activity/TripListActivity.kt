package sk.sivy_vlk.zazipovazie.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.adapter.TripCategoryAdapter
import sk.sivy_vlk.zazipovazie.databinding.ActivityTripListBinding
import sk.sivy_vlk.zazipovazie.model.Trip
import sk.sivy_vlk.zazipovazie.model.TripCategory

class TripListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTripListBinding

    private lateinit var tripCategoryAdapter: TripCategoryAdapter
    private var tripCategories: List<TripCategory> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTripListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setTitleTextColor(Color.BLACK)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.title = getString(R.string.trip_recommendations)

        // Provide data to the adapters
        tripCategories =
            listOf(
                TripCategory(
                    getString(R.string.whole_day_trips),
                    isExpanded = false,
                    listOf(
                        Trip("Trip 1..."),
                        Trip("Trip 2..."),
                    )
                ),
                TripCategory(
                    getString(R.string.short_term_trips),
                    isExpanded = false,
                    listOf(
                        Trip("Trip 3..."),
                        Trip("Trip 4..."),
                    )
                )

            )

        // Initialize adapters
        tripCategoryAdapter = TripCategoryAdapter(this, tripCategories)

        // Set adapters to RecyclerViews
        val verticalSpacingHeight = this.resources.getDimensionPixelSize(R.dimen.vertical_spacing) // Adjust as needed
        val itemDecoration = TripCategoryAdapter.TripCategoryViewHolder.VerticalSpaceItemDecoration(
            verticalSpacingHeight
        )

        val categoryRecyclerView: RecyclerView = binding.tripListContent.tripCategoriesRecyclerView
        categoryRecyclerView.addItemDecoration(itemDecoration)
        categoryRecyclerView.layoutManager = LinearLayoutManager(this)
        categoryRecyclerView.adapter = tripCategoryAdapter



    }
}