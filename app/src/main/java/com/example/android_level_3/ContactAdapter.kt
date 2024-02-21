package com.example.android_level_3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_level_3.databinding.ElementContactViewBinding

class ContactAdapter(
    private val listener: ElementClickListener,
) :
    ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    interface ElementClickListener {
        fun onElementDeleteClick(contact: Contact)
        fun onElementProfileClick(contact: Contact)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        return ContactViewHolder(
            binding = ElementContactViewBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ElementContactViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.tvContactName.text = contact.userName
            binding.tvContactCareer.text = contact.userCareer
            Glide.with(binding.imgContactAvatar.context) // v.2 можно передавать context при создании, вместе со списком
                .load(contact.userImage)
                .circleCrop()
                .placeholder(R.drawable.default_avatar)
                .into(binding.imgContactAvatar)

            setListeners(contact)
        }

        private fun setListeners(contact: Contact) {
            binding.imgContactDelete.setOnClickListener {
                listener.onElementDeleteClick(contact)
            }

            binding.root.setOnClickListener {
                listener.onElementProfileClick(contact)
            }
        }
    }
}

//Другие методы оповещения:
//notifyItemChanged(int),
//notifyItemInserted(int),
//notifyItemRemoved(int),
//notifyItemRangeChanged(int, int),
//notifyItemRangeInserted(int, int),
//notifyItemRangeRemoved(int, int)