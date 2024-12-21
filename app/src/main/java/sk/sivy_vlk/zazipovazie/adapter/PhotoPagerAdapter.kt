package sk.sivy_vlk.zazipovazie.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.fragment.FullscreenViewPagerFragment
import com.bumptech.glide.request.target.Target

class PhotoPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle, private val imageUrls: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = imageUrls.size

    override fun createFragment(position: Int): Fragment {
        return ImageFragment.newInstance(imageUrls, position)
    }

}

internal class ImageFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE_URL = "ARG_IMAGE_URL"
        private const val ARG_IMAGE_URLS = "image_urls"
        private const val ARG_INITIAL_POSITION = "initial_position"

        fun newInstance(imageUrls: List<String>, initialPosition: Int): ImageFragment {
            val fragment = ImageFragment()
            fragment.arguments  = Bundle().apply {
                putString(ARG_IMAGE_URL, imageUrls[initialPosition])
                putStringArrayList(ARG_IMAGE_URLS, ArrayList(imageUrls))
                putInt(ARG_INITIAL_POSITION, initialPosition)
            }
            return fragment
        }
    }

    private lateinit var imageView: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        imageView = view.findViewById(R.id.imageView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUrl = arguments?.getString(ARG_IMAGE_URL)
        val imageUrls = arguments?.getStringArrayList(ARG_IMAGE_URLS) ?: emptyList()
        val initialPosition = arguments?.getInt(ARG_INITIAL_POSITION) ?: 0
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(imageUrl)
                .override(Target.SIZE_ORIGINAL) // Load the original resolution
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Handle error case: set a placeholder or an error image
                        imageView.setImageResource(R.drawable.placeholder)
                        return false // Return false to let Glide handle additional tasks like logging
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Handle success case: set click listener
                        imageView.setOnClickListener {
                            openFullscreenImages(imageUrls, initialPosition, context)
                        }
                        return false // Return false to let Glide handle setting the image on the ImageView
                    }
                })
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.placeholder)
        }
    }

    private fun openFullscreenImages(
        imageUrls: List<String>,
        currentPosition: Int,
        context: Context?
    ) {
        val fullscreenFragment = FullscreenViewPagerFragment.newInstance(imageUrls, currentPosition)
        (context as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
            ?.setReorderingAllowed(true)
            ?.addToBackStack("fullscreen_view_pager")
            ?.replace(android.R.id.content, fullscreenFragment)
            ?.commit()
    }
}
