package com.codingsp.recipebook.adapters

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemNotificationBinding
import com.codingsp.recipebook.model.Notification
import com.codingsp.recipebook.utils.GlideApp
import com.codingsp.recipebook.utils.TimeToTimeAgo
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class NotificationAdapter(
    private val fragment: Fragment,
    private val options: FirestoreRecyclerOptions<Notification>,
    private val listener: NotificationListener
) : FirestoreRecyclerAdapter<Notification, NotificationAdapter.MyViewHolder>(options) {

    inner class MyViewHolder(itemView: ItemNotificationBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        val ivUser = itemView.ivUser
        val tvNotification = itemView.tvNotification
        val notificationAt = itemView.tvNotificationAt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding =
            ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Notification) {
        holder.itemView.setOnClickListener {
            listener.onNotificationClick(model)
        }
        var notificationByName = "";
        Firebase.firestore.collection("Users").document(model.notificationBy).get()
            .addOnSuccessListener {
                val getImageUrl = it["userImageUrl"] as String
                notificationByName = it["userDisplayName"] as String
                GlideApp.with(fragment).load(getImageUrl).error(R.drawable.ic_default_person_image)
                    .into(holder.ivUser)
                holder.notificationAt.text =
                    TimeToTimeAgo().getTimeAgo(model.notificationAt.toLong())
                if (model.type == "like") {
                    val text = HtmlCompat.fromHtml("<b> $notificationByName </b> liked your post.",HtmlCompat.FROM_HTML_MODE_LEGACY)
                    holder.tvNotification.text = text
                } else {
                    val text = HtmlCompat.fromHtml("<b> $notificationByName </b> commented on your post.",HtmlCompat.FROM_HTML_MODE_LEGACY)
                    holder.tvNotification.text = text
                }
            }
    }

}

interface NotificationListener {
    fun onNotificationClick(notification: Notification)
}