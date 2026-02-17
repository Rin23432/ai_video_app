package com.animegen.app.ui.screen.nativeview

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.animegen.app.AnimeGenApp
import com.animegen.app.R
import com.animegen.app.data.network.Work
import com.animegen.app.data.repo.AppResult
import kotlinx.coroutines.launch

class NativeWorkCenterActivity : AppCompatActivity() {

    private lateinit var loadingView: ProgressBar
    private lateinit var errorView: TextView
    private lateinit var emptyView: TextView
    private lateinit var selectionView: TextView
    private lateinit var workListView: RecyclerView

    private val adapter = NativeWorkAdapter { selectedCount ->
        selectionView.text = getString(R.string.native_work_center_selected_format, selectedCount)
    }

    private var rows: List<NativeWorkRow> = emptyList()
    private var descOrder: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_work_center)

        loadingView = findViewById(R.id.loadingView)
        errorView = findViewById(R.id.errorView)
        emptyView = findViewById(R.id.emptyView)
        selectionView = findViewById(R.id.selectionView)
        workListView = findViewById(R.id.workListView)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        findViewById<Button>(R.id.reloadButton).setOnClickListener { loadWorks() }
        findViewById<Button>(R.id.sortButton).setOnClickListener {
            descOrder = !descOrder
            applyRows(rows)
        }
        findViewById<Button>(R.id.copyButton).setOnClickListener {
            copySelectedIds(adapter.selectedIds())
        }

        workListView.layoutManager = LinearLayoutManager(this)
        workListView.setHasFixedSize(true)
        workListView.adapter = adapter
        selectionView.text = getString(R.string.native_work_center_selected_format, 0)

        loadWorks()
    }

    private fun loadWorks() {
        loadingView.isVisible = true
        errorView.isVisible = false
        emptyView.isVisible = false
        workListView.isVisible = false

        lifecycleScope.launch {
            when (val result = (application as AnimeGenApp).container.worksRepository.listWorks(limit = 60)) {
                is AppResult.Success -> {
                    rows = result.data.map { NativeWorkRow(work = it) }
                    applyRows(rows)
                    loadingView.isVisible = false
                    errorView.isVisible = false
                    emptyView.isVisible = rows.isEmpty()
                    workListView.isVisible = rows.isNotEmpty()
                }

                is AppResult.Failure -> {
                    loadingView.isVisible = false
                    workListView.isVisible = false
                    emptyView.isVisible = false
                    errorView.isVisible = true
                    errorView.text = result.error.displayMessage
                }
            }
        }
    }

    private fun applyRows(rawRows: List<NativeWorkRow>) {
        val sortedRows = if (descOrder) {
            rawRows.sortedByDescending { it.work.id }
        } else {
            rawRows.sortedBy { it.work.id }
        }
        adapter.submitList(sortedRows)
        selectionView.text = getString(R.string.native_work_center_selected_format, adapter.selectedIds().size)
    }

    private fun copySelectedIds(ids: List<Long>) {
        if (ids.isEmpty()) {
            Toast.makeText(this, R.string.native_work_center_select_first, Toast.LENGTH_SHORT).show()
            return
        }
        val content = ids.joinToString(separator = ",")
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText("work_ids", content))
        Toast.makeText(this, getString(R.string.native_work_center_copied_format, ids.size), Toast.LENGTH_SHORT).show()
    }
}

private data class NativeWorkRow(
    val work: Work,
    val selected: Boolean = false
)

private class NativeWorkAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : ListAdapter<NativeWorkRow, NativeWorkViewHolder>(NativeWorkRowDiff()) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = getItem(position).work.id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NativeWorkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_native_work_row, parent, false)
        return NativeWorkViewHolder(view)
    }

    override fun onBindViewHolder(holder: NativeWorkViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            toggleSelection(holder.bindingAdapterPosition)
        }
        holder.checkBox.setOnClickListener {
            toggleSelection(holder.bindingAdapterPosition)
        }
    }

    override fun onBindViewHolder(holder: NativeWorkViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(SELECTION_PAYLOAD)) {
            holder.bindSelection(getItem(position).selected)
            return
        }
        onBindViewHolder(holder, position)
    }

    fun selectedIds(): List<Long> {
        return currentList.filter { it.selected }.map { it.work.id }
    }

    private fun toggleSelection(position: Int) {
        if (position == RecyclerView.NO_POSITION) return
        val current = currentList.getOrNull(position) ?: return
        val updated = current.copy(selected = !current.selected)
        val next = currentList.toMutableList()
        next[position] = updated
        submitList(next)
        onSelectionChanged(next.count { it.selected })
    }

    private class NativeWorkRowDiff : DiffUtil.ItemCallback<NativeWorkRow>() {
        override fun areItemsTheSame(oldItem: NativeWorkRow, newItem: NativeWorkRow): Boolean {
            return oldItem.work.id == newItem.work.id
        }

        override fun areContentsTheSame(oldItem: NativeWorkRow, newItem: NativeWorkRow): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: NativeWorkRow, newItem: NativeWorkRow): Any? {
            return if (oldItem.selected != newItem.selected) SELECTION_PAYLOAD else null
        }
    }

    companion object {
        private const val SELECTION_PAYLOAD = "selection_payload"
    }
}

private class NativeWorkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val titleView: TextView = itemView.findViewById(R.id.titleView)
    private val subtitleView: TextView = itemView.findViewById(R.id.subtitleView)
    private val promptView: TextView = itemView.findViewById(R.id.promptView)
    private val statusView: TextView = itemView.findViewById(R.id.statusView)
    val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)

    fun bind(item: NativeWorkRow) {
        titleView.text = item.work.title.ifBlank { itemView.context.getString(R.string.native_work_center_untitled) }
        subtitleView.text = itemView.context.getString(
            R.string.native_work_center_meta_format,
            item.work.id,
            item.work.updatedAt ?: "-"
        )
        promptView.text = item.work.prompt
        statusView.text = item.work.status
        bindSelection(item.selected)
    }

    fun bindSelection(selected: Boolean) {
        checkBox.isChecked = selected
        itemView.isActivated = selected
    }
}
