package com.thomas.apps.workmanagerexample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thomas.apps.workmanagerexample.databinding.ItemBinding
import com.thomas.apps.workmanagerexample.model.WorkLog
import java.time.Instant
import java.time.ZoneId

class WorkLogAdapter : ListAdapter<WorkLog, WorkLogAdapter.ViewHolder>(WorkLogDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder private constructor(private val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkLog) {
            with(binding) {
                textViewId.text = item.id.toString()
                textViewTime.text =
                    Instant.ofEpochMilli(item.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        .toString()
                textViewRunFail.text = if (item.runFail) "Fail" else "Ok"
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }


    private class WorkLogDC : DiffUtil.ItemCallback<WorkLog>() {
        override fun areItemsTheSame(oldItem: WorkLog, newItem: WorkLog): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WorkLog, newItem: WorkLog): Boolean {
            return oldItem == newItem
        }
    }
}