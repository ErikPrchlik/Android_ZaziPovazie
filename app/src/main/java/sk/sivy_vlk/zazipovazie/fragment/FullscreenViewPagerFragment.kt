package sk.sivy_vlk.zazipovazie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.adapter.FullPhotoPagerAdapter

class FullscreenViewPagerFragment : Fragment() {

    private lateinit var imageUrls: List<String>
    private var initialPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fullscreen_view_pager, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUrls = arguments?.getStringArrayList(ARG_IMAGE_URLS) ?: emptyList()
        initialPosition = arguments?.getInt(ARG_INITIAL_POSITION) ?: 0

        val viewPager = view.findViewById<ViewPager2>(R.id.fullscreenViewPager)
        val adapter = FullPhotoPagerAdapter(childFragmentManager, lifecycle, imageUrls)
        viewPager.adapter = adapter
        viewPager.setCurrentItem(initialPosition, false)
    }

    companion object {
        private const val ARG_IMAGE_URLS = "image_urls"
        private const val ARG_INITIAL_POSITION = "initial_position"

        fun newInstance(imageUrls: List<String>, initialPosition: Int) =
            FullscreenViewPagerFragment().apply {
                arguments = Bundle().apply {
                    putStringArrayList(ARG_IMAGE_URLS, ArrayList(imageUrls))
                    putInt(ARG_INITIAL_POSITION, initialPosition)
                }
            }
    }
}
