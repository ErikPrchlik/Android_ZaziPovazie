package sk.sivy_vlk.zazipovazie.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.squareup.picasso.Picasso
import sk.sivy_vlk.zazipovazie.R

class PhotoPagerAdapter(
    fragmentManager: FragmentManager, lifecycle: Lifecycle, private val imageUrls: List<String>
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = imageUrls.size

    override fun createFragment(position: Int): Fragment {
        return ImageFragment.newInstance(imageUrls[position])
    }

}

internal class ImageFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE_URL = "ARG_IMAGE_URL"

        fun newInstance(imageUrl: String): ImageFragment {
            val fragment = ImageFragment()
            val args = Bundle()
            args.putString(ARG_IMAGE_URL, imageUrl)
            fragment.arguments = args
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
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder_error)
                .into(imageView, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {

                    }
                    override fun onError(e: Exception?) {
                        imageView.setImageResource(R.drawable.placeholder_error)
                    }
                })
        } else {
            imageView.setImageResource(R.drawable.placeholder_error)
        }    }
}
//class PhotoPagerAdapter(
//    private val context: Context,
//    private val imageUrls: List<String>,
//    private val onLoadComplete: () -> Unit
//) : PagerAdapter() {
//
//    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        val imageView = ImageView(context)
//        container.addView(imageView)
//
//        val imageUrl = imageUrls[position]
//
//        if (imageUrl.isNotEmpty()) {
//            Picasso.get()
//                .load(imageUrl)
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.placeholder_error)
//                .into(imageView, object : com.squareup.picasso.Callback {
//                    override fun onSuccess() {
//                        onLoadComplete.invoke()
//                    }
//                    override fun onError(e: Exception?) {
//                        imageView.setImageResource(R.drawable.placeholder_error)
//                    }
//                })
//        } else {
//            imageView.setImageResource(R.drawable.placeholder_error)
//        }
//
//        return imageView
//    }
//
//    override fun getCount(): Int = imageUrls.size
//
//    override fun isViewFromObject(view: View, `object`: Any): Boolean {
//        return view == `object`
//    }
//
//    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        container.removeView(`object` as View)
//    }
//}
