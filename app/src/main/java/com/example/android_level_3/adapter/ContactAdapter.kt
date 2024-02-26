package com.example.android_level_3.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android_level_3.R
import com.example.android_level_3.databinding.ElementContactViewBinding
import com.example.android_level_3.model.Contact

// TODO
//  1. перенести методы добавления/удаления во viewmodel
//  2. сменить адаптер на ListAdapter
//  3. добавить работу с DiffUtils
//  4. перевесить слушатели в onCreateViewHolder
//  5. переместить переменные для временного хранения удаленного пользователя в viewmodel
//  6. убрать комментарии в конце

interface ElementClickListener {
    fun onElementDeleteClick(position: Int)
    fun onElementProfileClick(position: Int)
}

class ContactAdapter(list: MutableList<Contact>, listener: ElementClickListener) : RecyclerView.Adapter<ContactAdapter.ContactHolder>() {

    private var contactList: MutableList<Contact> = list

    var temporaryContactData: Contact? = null
    var temporaryContactPosition: Int = 0

    // приходящий в конструктор слушатель нажатий
    private val onElementClickListener: ElementClickListener = listener

    // Вариант №2 создания Holder
    //    class ContactHolder(val binding: ElementContactViewBinding) : RecyclerView.ViewHolder(binding.root) {}

    inner class ContactHolder(element: View) : RecyclerView.ViewHolder(element) {
        val binding = ElementContactViewBinding.bind(element)
        fun bind(user: Contact) {
            binding.tvContactName.text = "${user.contactName} [id=${user.id}]"      // TODO убрать  [id=${user.id}] в конце
            binding.tvContactCareer.text = user.contactCareer
            Glide.with(binding.imgContactAvatar.context)                                            // v.2 можно передавать context при создании, вместе со списком
                .load(user.contactImage)
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.element_contact_view, parent, false)
        return ContactHolder(view)
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(contactList[position])

        holder.binding.imgContactDelete.setOnClickListener {
          onElementClickListener.onElementDeleteClick(position)
        }

        holder.binding.root.setOnClickListener {
            onElementClickListener.onElementProfileClick(position)
        }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    // добавление нового контакта в общий список контактов
    fun addNewContact(user: Contact) {
        contactList.add(user)
        notifyItemInserted(contactList.size+1)
    }

    fun removeContact(position: Int) {
        temporaryContactData = contactList[position]
        temporaryContactPosition = position

        contactList.removeAt(position)
        notifyItemRemoved(position)                                         // работает, но весь список не обновляется, каждый элемент сохраняет свою прежнюю позицию
                                                                            // есть (15, 16, 17, 18 элемент), удаляем 17, получаем список (15, 16, 18)

        // по сути тот же notifyDataSetChanged()
        // но перерисовывает не все элементы (чем дальше от 0 элемента, тем меньше перерисовывает)
        notifyItemRangeChanged(position, contactList.size)
    }

    // восстановление контакта
    fun restoreContact(){
        contactList.add(temporaryContactPosition, temporaryContactData!!)
        notifyItemInserted(temporaryContactPosition)
        notifyItemRangeChanged(temporaryContactPosition, contactList.size)
    }
}

//Другие методы оповещения:
//notifyItemChanged(int),
//notifyItemInserted(int),
//notifyItemRemoved(int),
//notifyItemRangeChanged(int, int),
//notifyItemRangeInserted(int, int),
//notifyItemRangeRemoved(int, int)