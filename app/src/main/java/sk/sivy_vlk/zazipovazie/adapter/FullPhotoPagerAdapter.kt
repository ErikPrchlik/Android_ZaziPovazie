package sk.sivy_vlk.zazipovazie.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import sk.sivy_vlk.zazipovazie.fragment.FullscreenImageFragment

class FullPhotoPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val imageUrls: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = imageUrls.size

    override fun createFragment(position: Int): Fragment {
        return FullscreenImageFragment.newInstance(imageUrls[position])
    }
}
