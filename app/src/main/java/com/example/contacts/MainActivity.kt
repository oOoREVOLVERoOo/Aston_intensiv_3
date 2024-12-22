package com.example.contacts

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.activity.viewModels
import androidx.recyclerview.widget.ItemTouchHelper

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter
    private val contactViewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fabAddContact = findViewById<FloatingActionButton>(R.id.fabAddContact)
        fabAddContact.setOnClickListener {
            showAddContactDialog()
        }
        val layoutActions = findViewById<LinearLayout>(R.id.layoutActions)
        val buttonDelete = findViewById<Button>(R.id.buttonDelete)
        val buttonCancel = findViewById<Button>(R.id.buttonCancel)
        val imageViewDelete = findViewById<ImageView>(R.id.imageViewDelete)

        imageViewDelete.setOnClickListener {
            adapter.isSelectionMode = true
            layoutActions.visibility = View.VISIBLE
            imageViewDelete.visibility = View.GONE
            fabAddContact.visibility = View.GONE
        }

        buttonDelete.setOnClickListener {
            val selectedContacts = adapter.getSelectedItems()
            val updatedList = contactViewModel.contacts.value?.filter { it !in selectedContacts }
            updatedList?.let { contactViewModel.setContacts(it) }
            adapter.clearSelection()
            layoutActions.visibility = View.GONE
        }

        buttonCancel.setOnClickListener {
            adapter.clearSelection()
            layoutActions.visibility = View.GONE
            imageViewDelete.visibility = View.VISIBLE
        }

        contactViewModel.contacts.observe(this) { contacts ->
            adapter.submitList(contacts)
        }

        recyclerView = findViewById(R.id.recyclerView)
        adapter = ContactAdapter(
            onClick = { contact -> showEditContactDialog(contact) },
            selectionCallback = object : SelectionCallback {
                override fun onSelectionModeChanged(isEnabled: Boolean) {
                    layoutActions.visibility = if (isEnabled) View.VISIBLE else View.GONE
                    fabAddContact.visibility = if (isEnabled) View.GONE else View.VISIBLE
                    imageViewDelete.visibility = if (isEnabled) View.GONE else View.VISIBLE
                }
            }
        )

        adapter.isSelectionMode = false
        layoutActions.visibility = View.GONE

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        if (contactViewModel.contacts.value.isNullOrEmpty()) {
            val initialContacts = List(100) { i ->
                Contact(i + 1, "Имя$i", "Фамилия$i", "+790000000${i % 10}")
            }
            contactViewModel.setContacts(initialContacts)
        }

        val itemTouchHelperCallback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.onItemMove(fromPosition, toPosition) { updatedList ->
                    contactViewModel.updateContactList(updatedList)
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun isLongPressDragEnabled(): Boolean {
                return true
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun showAddContactDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        dialog.setContentView(view)

        val editTextFirstName = view.findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = view.findViewById<EditText>(R.id.editTextLastName)
        val editTextPhone = view.findViewById<EditText>(R.id.editTextPhone)
        val buttonSave = view.findViewById<Button>(R.id.buttonSaveContact)

        buttonSave.setOnClickListener {
            val firstName = editTextFirstName.text.toString()
            val lastName = editTextLastName.text.toString()
            val phone = editTextPhone.text.toString()

            if (firstName.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank()) {
                val newContact = Contact(
                    id = (contactViewModel.contacts.value?.size ?: 0) + 1,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phone
                )

                contactViewModel.addContact(newContact)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun showEditContactDialog(contact: Contact) {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        dialog.setContentView(view)

        val editTextFirstName = view.findViewById<EditText>(R.id.editTextFirstName)
        val editTextLastName = view.findViewById<EditText>(R.id.editTextLastName)
        val editTextPhone = view.findViewById<EditText>(R.id.editTextPhone)
        val buttonSave = view.findViewById<Button>(R.id.buttonSaveContact)

        editTextFirstName.setText(contact.firstName)
        editTextLastName.setText(contact.lastName)
        editTextPhone.setText(contact.phoneNumber)

        buttonSave.setOnClickListener {
            val updatedFirstName = editTextFirstName.text.toString()
            val updatedLastName = editTextLastName.text.toString()
            val updatedPhone = editTextPhone.text.toString()

            if (updatedFirstName.isNotBlank() && updatedLastName.isNotBlank() && updatedPhone.isNotBlank()) {
                val updatedContact = contact.copy(
                    firstName = updatedFirstName,
                    lastName = updatedLastName,
                    phoneNumber = updatedPhone
                )

                contactViewModel.updateContact(updatedContact)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }
}
