package com.heymahesh.agent.core.actions

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.heymahesh.agent.core.models.ContactEntity

class ContactsProviderHelper(private val context: Context) {

    fun searchContacts(query: String): List<ContactEntity> {
        val contacts = mutableListOf<ContactEntity>()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI
        )

        val cleanQuery = query.trim()
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$cleanQuery%")

        try {
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val labelCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL)
                val photoCol = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                while (it.moveToNext()) {
                    val id = it.getString(idCol) ?: ""
                    val name = it.getString(nameCol) ?: ""
                    val number = it.getString(numCol) ?: ""
                    val typeInt = it.getInt(typeCol)
                    val label = it.getString(labelCol)
                    val photo = it.getString(photoCol)

                    val tag = when (typeInt) {
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
                        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
                        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
                        ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> label ?: "Custom"
                        else -> "Phone"
                    }

                    // Avoid duplicate phone numbers
                    if (contacts.none { c -> c.phoneNumber.replace(" ", "") == number.replace(" ", "") }) {
                        contacts.add(
                            ContactEntity(
                                id = id,
                                name = name,
                                phoneNumber = number,
                                tag = tag,
                                photoUri = photo
                            )
                        )
                    }
                }
            }
        } catch (e: SecurityException) {
            // Permission not yet granted
        }

        return contacts
    }
}
