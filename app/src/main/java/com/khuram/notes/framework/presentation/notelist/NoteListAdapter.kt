package com.khuram.notes.framework.presentation.notelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.framework.presentation.common.changeColor
import com.khuram.notes.util.printLogD
import com.khuram.notes.R
import com.khuram.notes.databinding.LayoutNoteListItemBinding


class NoteListAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val selectedNotes: LiveData<ArrayList<Note>>,
    private val dateUtil: DateUtil
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return NoteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_note_list_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            selectedNotes,
            dateUtil
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NoteViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Note>) {
        val commitCallback = Runnable {
            // if process died must restore list position
            // very annoying
            interaction?.restoreListPosition()
        }
        printLogD("listadapter", "size: ${list.size}")
        differ.submitList(list, commitCallback)
    }

    fun getNote(index: Int): Note? {
        return try{
            differ.currentList[index]
        }catch (e: IndexOutOfBoundsException){
            e.printStackTrace()
            null
        }
    }

    class NoteViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val selectedNotes: LiveData<ArrayList<Note>>,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(itemView) {

        private val COLOR_GREY = R.color.app_background_color
        private val COLOR_PRIMARY = R.color.colorPrimary
        private lateinit var note: Note

        val binding = LayoutNoteListItemBinding.bind(itemView)

        fun bind(item: Note) = with(itemView) {
            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, note)
            }
            setOnLongClickListener {
                interaction?.activateMultiSelectionMode()
                interaction?.onItemSelected(adapterPosition, note)
                true
            }
            note = item
            binding.noteTitle.text = item.title
            binding.noteTimestamp.text = dateUtil.removeTimeFromDateString(item.updated_at)

            selectedNotes.observe(lifecycleOwner) { notes ->
                if (notes != null) {
                    if (notes.contains(note)) {
                        changeColor(
                            newColor = COLOR_GREY
                        )
                    } else {
                        changeColor(
                            newColor = COLOR_PRIMARY
                        )
                    }
                } else {
                    changeColor(
                        newColor = COLOR_PRIMARY
                    )
                }
            }
        }
    }

    interface Interaction {

        fun onItemSelected(position: Int, item: Note)

        fun restoreListPosition()

        fun isMultiSelectionModeEnabled(): Boolean

        fun activateMultiSelectionMode()

        fun isNoteSelected(note: Note): Boolean
    }
}
