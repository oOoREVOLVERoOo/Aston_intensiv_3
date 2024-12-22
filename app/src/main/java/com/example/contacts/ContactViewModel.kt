package com.example.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class ContactViewModel : ViewModel() {
    private val _contacts = MutableLiveData<List<Contact>>(emptyList())
    val contacts: LiveData<List<Contact>> get() = _contacts

    fun setContacts(newList: List<Contact>) {
        _contacts.value = newList
    }

    fun addContact(contact: Contact) {
        val updatedList = _contacts.value?.toMutableList() ?: mutableListOf()
        updatedList.add(contact)
        _contacts.value = updatedList
    }

    fun updateContact(updatedContact: Contact) {
        val updatedList = _contacts.value?.map {
            if (it.id == updatedContact.id) updatedContact else it
        } ?: emptyList()
        _contacts.value = updatedList
    }

    fun updateContactList(newList: List<Contact>) {
        _contacts.value = newList
    }
}