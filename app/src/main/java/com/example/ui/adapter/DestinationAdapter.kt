package com.example.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.R
import com.example.model.Destination
import java.util.Locale

/**
 * Adaptador tradicional de RecyclerView para mostrar el catálogo de destinos
 * dentro de tarjetas (CardView), usando Glide para la carga de imágenes.
 */
class DestinationAdapter(
    private val onEditClick: (Destination) -> Unit,
    private val onDeleteClick: (Destination) -> Unit
) : ListAdapter<Destination, DestinationAdapter.DestinationViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destination, parent, false)
        return DestinationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(getItem(position), onEditClick, onDeleteClick)
    }

    class DestinationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val image: ImageView = itemView.findViewById(R.id.image_destination)
        private val badgeCountry: TextView = itemView.findViewById(R.id.badge_country)
        private val badgePrice: TextView = itemView.findViewById(R.id.badge_price)
        private val name: TextView = itemView.findViewById(R.id.text_name)
        private val description: TextView = itemView.findViewById(R.id.text_description)
        private val btnEdit: TextView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: TextView = itemView.findViewById(R.id.btn_delete)

        fun bind(
            destination: Destination,
            onEditClick: (Destination) -> Unit,
            onDeleteClick: (Destination) -> Unit
        ) {
            name.text = destination.name
            description.text = destination.description
            badgeCountry.text = destination.country
            badgePrice.text = String.format(
                Locale.US,
                "$%.2f USD",
                destination.price
            )

            // Carga de imagen con Glide, con placeholder y error de respaldo
            Glide.with(itemView.context)
                .load(destination.imageUri)
                .centerCrop()
                .placeholder(R.drawable.travel_cancun)
                .error(R.drawable.travel_cancun)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(image)

            itemView.findViewById<View>(R.id.card_destination).contentDescription = destination.name

            btnEdit.setOnClickListener { onEditClick(destination) }
            btnDelete.setOnClickListener { onDeleteClick(destination) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Destination>() {
            override fun areItemsTheSame(oldItem: Destination, newItem: Destination): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Destination, newItem: Destination): Boolean {
                return oldItem == newItem
            }
        }
    }
}