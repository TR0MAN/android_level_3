package com.example.android_level_3.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ElementContactViewBinding
import com.example.android_level_3.model.Contact

// TODO
//  6. убрать комментарии в конце

interface ElementClickListener {
    fun onElementDeleteClick(contact: Contact)

    fun onElementProfileClick(contact: Contact)
}

class ContactAdapter(private val clickListener: ElementClickListener) :
    ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffUtilCallback()) {

        // по шаблону заполняем данными элемент списка
    inner class ContactViewHolder(
        val binding: ElementContactViewBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(contact: Contact) {
                binding.tvContactName.text = "${contact.contactName} [id=${contact.id}]"      // TODO убрать  [id=${user.id}] в конце
                binding.tvContactCareer.text = contact.contactCareer
                Glide.with(binding.imgContactAvatar.context)                                        // v.2 тут можно передавать context при создании, вместе со списком
                    .load(contact.contactImage)
                    .circleCrop()
                    .placeholder(R.drawable.default_avatar)
                    .into(binding.imgContactAvatar)
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

        binding.imgContactDelete.setOnClickListener {
            val contact = it.tag as Contact
            Log.d("TAG", "[ADAPTER] contact = $contact")                                   // TODO DELETE
            clickListener.onElementDeleteClick(contact)
        }

        binding.root.setOnClickListener {                                        // TODO - DETAIL VIEW
            val contact = it.tag as Contact
            clickListener.onElementProfileClick(contact)
        }



//        binding.imgContactAvatar.setOnClickListener { }                   // TODO - DELETE LATER

        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
        with(holder.binding) {
            imgContactDelete.tag = getItem(position)
//            imgContactAvatar.tag = getItem(position)                      // TODO - DELETE
            root.tag = getItem(position)
        }
    }
}

    // TODO DELETE
//class ContactAdapter(
//    list: MutableList<Contact>,
//    private val clickListener: ElementClickListener
//) : RecyclerView.Adapter<ContactAdapter.ContactHolder>() {
//
//    private var contactList: MutableList<Contact> = list
//
//    // Вариант №2 создания Holder
//    //    class ContactHolder(val binding: ElementContactViewBinding) : RecyclerView.ViewHolder(binding.root) {}
//
//    inner class ContactHolder(element: View) : RecyclerView.ViewHolder(element) {
//        val binding = ElementContactViewBinding.bind(element)
//        fun bind(user: Contact) {
//            binding.tvContactName.text = "${user.contactName}"
//            binding.tvContactCareer.text = user.contactCareer
//            Glide.with(binding.imgContactAvatar.context)                                            // v.2 можно передавать context при создании, вместе со списком
//                .load(user.contactImage)
//                .circleCrop()
//                .placeholder(R.drawable.default_avatar)
//                .into(binding.imgContactAvatar)
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.element_contact_view, parent, false)
//        return ContactHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
//        holder.bind(contactList[position])
//
//        holder.binding.imgContactDelete.setOnClickListener {
////            clickListener.onElementDeleteClick(position)
//        }
//
//        holder.binding.root.setOnClickListener {
////            clickListener.onElementProfileClick(position)
//        }
//
//        holder.binding.imgContactDelete.tag
//    }
//
//    override fun getItemCount(): Int {
//        return contactList.size
//    }
//}

//notifyItemRemoved(position)                                         // работает, но весь список не обновляется, каждый элемент сохраняет свою прежнюю позицию
//                                                                            // есть (15, 16, 17, 18 элемент), удаляем 17, получаем список (15, 16, 18)

//Другие методы оповещения:
//notifyItemChanged(int),
//notifyItemInserted(int),
//notifyItemRemoved(int),
//notifyItemRangeChanged(int, int),
//notifyItemRangeInserted(int, int),
//notifyItemRangeRemoved(int, int)