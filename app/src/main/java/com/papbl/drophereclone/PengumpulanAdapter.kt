package com.papbl.drophereclone

import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PengumpulanAdapter(
    val items: ArrayList<SenderData>,
    val deadline: String,
    val ownerId: String,
    val uniqueCode: String
) :
    RecyclerView.Adapter<PengumpulanHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengumpulanHolder {
        val inflater = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_card_submit, parent, false)
        return PengumpulanHolder(inflater, deadline, ownerId, uniqueCode)
    }

    override fun onBindViewHolder(holder: PengumpulanHolder, position: Int) {
        val senderData: SenderData = items[position]
        holder.bind(senderData)
    }
}

class PengumpulanHolder(
    v: View,
    val deadline: String,
    val ownerId: String,
    val uniqueCode: String
) :
    RecyclerView.ViewHolder(v), View.OnClickListener {
    private val storageRef = Firebase.storage.reference
    private lateinit var fileRef: StorageReference

    private var chipSubmitStatus = itemView.findViewById<Chip>(R.id.chip_submit_status)
    private var tvSendDate = itemView.findViewById<MaterialTextView>(R.id.tv_sender_date)
    private var tvSenderName = itemView.findViewById<MaterialTextView>(R.id.tv_sender_name)
    private var tvSenderFileName = itemView.findViewById<MaterialTextView>(R.id.tv_sender_file)

    init {
        v.setOnClickListener(this)
    }

    fun bind(senderData: SenderData) {
        fileRef = storageRef.child(ownerId + "_" + uniqueCode + "/" + senderData.fileName)
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        dateFormat.setTimeZone(TimeZone.getDefault())
        val deadlineDate = dateFormat.parse(deadline)
        val submitDate = senderData.submitAt.toDate()
        if (submitDate > deadlineDate) {
            chipSubmitStatus.text = itemView.context.getString(R.string.chips_submit_late)
            chipSubmitStatus.setChipBackgroundColorResource(R.color.colorError)
        } else {
            chipSubmitStatus.text = itemView.context.getString(R.string.chips_submit_on_time)
        }
        tvSendDate.text = dateFormat.format(submitDate)
        tvSenderName.text = senderData.fullName
        tvSenderFileName.text = senderData.fileName
    }

    override fun onClick(v: View) {
        val senderName = tvSenderName.text.toString()
        val fileName = tvSenderFileName.text.toString()
        val fileDir =
            v.context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS + "/" + ownerId + "_" + uniqueCode)
        val file = File(fileDir, fileName)
        Log.d("Cek ", fileRef.toString())
        fileRef.getFile(file).addOnSuccessListener {
            val toast = Toast.makeText(
                v.context,
                "Berhasil mengunduh file milik ${senderName}",
                Toast.LENGTH_SHORT
            )
            val layout = toast.view as LinearLayout
            val text = layout.getChildAt(0) as TextView
            text.gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
            toast.show()
        }.addOnFailureListener() {
            Log.e("PengumpulanAdapter", it.message.toString())
        }
    }
}