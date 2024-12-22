package com.example.contacts

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

interface SelectionCallback {
    fun onSelectionModeChanged(isEnabled: Boolean)
}

class ContactAdapter(
    private val onClick: (Contact) -> Unit,
    private val selectionCallback: SelectionCallback
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(DiffCallback()) {

    private val selectedItems = mutableSetOf<Contact>()
    var isSelectionMode = false

    fun onItemMove(fromPosition: Int, toPosition: Int, updateViewModel: (List<Contact>) -> Unit): Boolean {
        val currentList = currentList.toMutableList()
        val movedItem = currentList.removeAt(fromPosition)
        currentList.add(toPosition, movedItem)
        submitList(currentList)
        updateViewModel(currentList)
        return true
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            contact: Contact,
            isSelected: Boolean,
            onClick: (Contact) -> Unit,
            onLongClick: (Contact, View) -> Unit
        ) {
            itemView.apply {
                findViewById<TextView>(R.id.textViewName).text = "${contact.firstName} ${contact.lastName}"
                findViewById<TextView>(R.id.textViewPhone).text = contact.phoneNumber
                setBackgroundColor(if (isSelected) Color.LTGRAY else Color.TRANSPARENT)

                setOnClickListener { onClick(contact) }
                setOnLongClickListener {
                    onLongClick(contact, this)
                    true
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(
            contact,
            selectedItems.contains(contact),
            onClick = { clickedContact ->
                if (isSelectionMode) toggleSelection(clickedContact)
                else onClick(clickedContact)
            },
            onLongClick = { _, _ ->
            }
        )
    }

    private fun toggleSelection(contact: Contact) {
        val position = currentList.indexOf(contact)
        if (position != -1) {
            if (selectedItems.contains(contact)) selectedItems.remove(contact)
            else selectedItems.add(contact)
            notifyItemChanged(position)
        }
    }

    fun getSelectedItems(): List<Contact> = selectedItems.toList()

    fun clearSelection() {
        val previouslySelected = selectedItems.toList()
        selectedItems.clear()
        isSelectionMode = false
        previouslySelected.forEach { contact ->
            val position = currentList.indexOf(contact)
            if (position != -1) notifyItemChanged(position)
        }
        selectionCallback.onSelectionModeChanged(false)
    }

    class DiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) = oldItem == newItem
    }
}