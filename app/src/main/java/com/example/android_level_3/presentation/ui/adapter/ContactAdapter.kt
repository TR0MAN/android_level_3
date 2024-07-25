package com.example.android_level_3.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ElementContactViewBinding
import com.example.android_level_3.data.retrofit.model.Contact
import com.example.android_level_3.presentation.utils.ext.gone
import com.example.android_level_3.presentation.utils.ext.visible

class ContactAdapter(
    private val clickListener: ClickListener,
    private val multiSelectState: Boolean?,
    private val usersInContactList: List<Int>?,
    private val selectedContacts: List<Int>?) :
    ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffUtilCallback()) {

    inner class ContactViewHolder(
        val binding: ElementContactViewBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(contact: Contact) {
                binding.tvContactName.text = contact.name.toString()
                binding.tvContactCareer.text = contact.career.toString()

                // v.2 тут можно передавать context при создании, вместе со списком
                Glide.with(binding.imgContactAvatar.context)
                    .load(contact.image)
                    .circleCrop()
                    .placeholder(R.drawable.default_avatar)
                    .into(binding.imgContactAvatar)

                when (multiSelectState) {
                    null -> {
                        if (usersInContactList != null) {
                            if (usersInContactList.contains(contact.id)) {
                                binding.imgContactInList.visible()
                                binding.imgContactAddToContacts.gone()
                            } else {
                                binding.imgContactInList.gone()
                                binding.imgContactAddToContacts.visible()
                            }
                        } else {
                            binding.imgContactInList.gone()
                            binding.imgContactAddToContacts.visible()
                        }
                    }
                    true -> {
                        binding.checkboxForDelete.visible()
                        binding.root.setBackgroundResource(R.drawable.element_view_style_gray)
                        selectedContacts?.let {
                            binding.checkboxForDelete.isChecked = selectedContacts.contains(contact.id)
                        }
                    }
                    false -> {
                        binding.imgContactDelete.visible()
                    }
                }
            }
    }

    // лучше вешать слушатели тут, таким образом будут созданы только слушатели для тех элементов
    // которые видны на экране (+1 сверху и +1 снизу), а не для всех элементов как в случае с onBindViewHolder
    // Например, есть 3 слушателя на элемент, всего 100 элементов, и 10 эл. на экране
    // с onBindViewHolder - будет создано 300 слушателей (100х3=300)
    // с onCreateViewHolder - 10 которые на экране (+1 сверху и +1 снизу), итого 13
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ElementContactViewBinding.inflate(inflater, parent, false)

        when (multiSelectState) {
            null -> {
                binding.imgContactAddToContacts.setOnClickListener {
                    val contact = it.tag as Contact
                    (clickListener as ElementClickListener).onElementClickAction(contact)
                }
            }
            true -> {
                binding.root.setOnClickListener {
                    val contact = it.tag as Contact
                    var checkBoxState = binding.checkboxForDelete.isChecked
                    if (!checkBoxState) {
                        binding.checkboxForDelete.isChecked = true
                        checkBoxState = true
                    } else {
                        binding.checkboxForDelete.isChecked = false
                        checkBoxState = false
                    }
                    (clickListener as ExtendedElementClickListener).onElementChecked(checkBoxState, contact.id)
                }
            }
            false -> {
                binding.imgContactDelete.setOnClickListener {
                    val contact = it.tag as Contact
                    (clickListener as ExtendedElementClickListener).onElementClickAction(contact)
                }
                binding.root.setOnClickListener {
                    val contact = it.tag as Contact
                    (clickListener as ExtendedElementClickListener).onElementProfileClick(contact)
                }
            }
        }

        // set listener on root view in group deleting
        multiSelectState?.let { state ->
            if (!state) {
                binding.root.setOnLongClickListener {
                    val contact = it.tag as Contact
                    (clickListener as ExtendedElementClickListener).onElementLongClick(contact.id)
                    return@setOnLongClickListener true
                }
            }
        }
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
        with(holder.binding) {
            imgContactAddToContacts.tag = getItem(position)
            imgContactDelete.tag = getItem(position)
            root.tag = getItem(position)
        }
    }
}
