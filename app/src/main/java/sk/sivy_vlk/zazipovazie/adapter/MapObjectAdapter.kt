package sk.sivy_vlk.zazipovazie.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.model.MapObject
import java.io.FileInputStream

class MapObjectAdapter(private val objects: List<MapObject>) :
    RecyclerView.Adapter<MapObjectAdapter.ObjectViewHolder>() {

    inner class ObjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val objectName: TextView = itemView.findViewById(R.id.tv_object_name)
        val objectIcon: ImageView = itemView.findViewById(R.id.iv_map_object)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_object, parent, false)
        return ObjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val mapObject = objects[position]
        if (mapObject.icon != null) {
            val fileInputStream = FileInputStream(mapObject.icon)
            val bitmap = BitmapFactory.decodeStream(fileInputStream)
            fileInputStream.close()
            holder.objectIcon.setImageBitmap(bitmap)
        }
        holder.objectName.text = mapObject.name
    }

    override fun getItemCount(): Int {
        return objects.size
    }
}
