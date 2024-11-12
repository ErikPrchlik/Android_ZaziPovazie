package sk.sivy_vlk.zazipovazie.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import sk.sivy_vlk.zazipovazie.R
import sk.sivy_vlk.zazipovazie.activity.MapObjectDetailActivity
import sk.sivy_vlk.zazipovazie.model.MapObject
import java.io.FileInputStream

class MapObjectAdapter(
    private val context: Context,
    private val objects: List<MapObject>,
    private val mapObjectClickedListener: (MapObject) -> Unit
) : RecyclerView.Adapter<MapObjectAdapter.ObjectViewHolder>() {

    inner class ObjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val objectContainer: LinearLayout = itemView.findViewById(R.id.ll_map_object)
        val objectName: TextView = itemView.findViewById(R.id.tv_object_name)
        val objectIcon: ImageView = itemView.findViewById(R.id.iv_map_object)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_object, parent, false)
        return ObjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val mapObject = objects[position]
        if (mapObject.coordinates.size > 1) {
            holder.objectIcon.setImageDrawable(null)
            if (mapObject.selected) {
                holder.objectIcon.setBackgroundColor(Color.RED)
            } else {
                holder.objectIcon.setBackgroundColor(Color.BLUE)
            }
        } else {
            if (mapObject.icon != null) {
                val fileInputStream = FileInputStream(mapObject.icon)
                val bitmap = BitmapFactory.decodeStream(fileInputStream)
                fileInputStream.close()
                holder.objectIcon.setImageBitmap(bitmap)
            }
        }
        holder.objectName.text = mapObject.name

        holder.itemView.setOnClickListener {
            if (mapObject.selected) {
                val intent = Intent(context, MapObjectDetailActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("MAP_OBJECT", mapObject)
                context.startActivity(intent)
            } else {
                mapObject.selected = true
                mapObjectClickedListener(mapObject)
                notifyItemChanged(position)
            }
        }

        if (mapObject.selected) {
            holder.objectContainer.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.objectContainer.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int {
        return objects.size
    }
}
