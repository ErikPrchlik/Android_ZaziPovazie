package sk.sivy_vlk.zazipovazie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.Target
import sk.sivy_vlk.zazipovazie.R

class FullscreenImageFragment : Fragment() {

    private var imageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_fullscreen_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUrl = arguments?.getString(ARG_IMAGE_URL)

        val imageView = view.findViewById<ImageView>(R.id.fullscreenImageView)
        // Load the image (use your favorite image-loading library, e.g., Glide or Coil)
        Glide.with(requireContext())
            .load(imageUrl)
            .override(Target.SIZE_ORIGINAL) // Load the original resolution
            .into(imageView)

    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String) = FullscreenImageFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_IMAGE_URL, imageUrl)
            }
        }
    }
}
