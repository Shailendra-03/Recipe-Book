package com.codingsp.recipebook.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.codingsp.recipebook.R
import com.codingsp.recipebook.databinding.ItemCommentBinding
import com.codingsp.recipebook.model.Comment
import com.codingsp.recipebook.utils.GlideApp
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(
    private val fragment: Fragment, private val options: FirestorePagingOptions<Comment>
) : FirestorePagingAdapter<Comment, CommentAdapter.MyViewHolder>(options) {

    inner class MyViewHolder(item: ItemCommentBinding) : RecyclerView.ViewHolder(item.root) {
        val commentOwnerImage = item.ivCommentOwnerImage
        val commentOwnerDisplayName = item.tvCommentOwnerDisplayName
        val comment = item.tvComment
        val commentedAt = item.CommentedAt
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int, model: Comment) {
        holder.comment.text = model.comment
        holder.commentedAt.text = model.commentAt
        FirebaseFirestore.getInstance().collection("Users").document(model.commentBy).get()
            .addOnSuccessListener { doc ->
                holder.commentOwnerDisplayName.text = doc.get("userDisplayName").toString()
                val imageUrl = doc.get("userImageUrl").toString()
                GlideApp.with(fragment)
                    .load(imageUrl)
                    .error(R.drawable.ic_default_user_image)
                    .into(holder.commentOwnerImage)
            }
    }
}