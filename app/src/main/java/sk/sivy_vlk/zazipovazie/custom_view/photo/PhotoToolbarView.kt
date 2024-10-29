package sk.sivy_vlk.zazipovazie.custom_view.photo

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.adapter.PhotoPagerAdapter

class PhotoToolbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Toolbar(context, attrs, defStyleAttr) {

    private val viewPager: ViewPager2
    private val tabLayout: TabLayout
//    private val loadingView: ProgressBar

    init {
        View.inflate(context, R.layout.photo_toolbar_view, this)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    fun setImages(fragmentManager: FragmentManager, lifecycle: Lifecycle, imageUrls: List<String>) {
        // Show loading view
//        loadingView.visibility = View.VISIBLE

        // Load images asynchronously
        val adapter = PhotoPagerAdapter(fragmentManager, lifecycle, imageUrls)
//        val adapter = PhotoPagerAdapter(context, imageUrls) { hideLoadingView() }
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position -> }.attach()
    }

    private fun hideLoadingView() {
        // Hide loading view when images are loaded
//        loadingView.visibility = View.GONE
    }
}