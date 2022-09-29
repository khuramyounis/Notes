package com.khuram.notes.framework.presentation.notedetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.interactors.common.DeleteNote.Companion.DELETE_ARE_YOU_SURE
import com.khuram.notes.business.interactors.common.DeleteNote.Companion.DELETE_NOTE_SUCCESS
import com.khuram.notes.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_FAILED_PK
import com.khuram.notes.business.interactors.notedetail.UpdateNote.Companion.UPDATE_NOTE_SUCCESS
import com.khuram.notes.framework.presentation.notedetail.state.CollapsingToolbarState
import com.khuram.notes.framework.presentation.notedetail.state.NoteDetailStateEvent
import com.khuram.notes.framework.presentation.notedetail.state.NoteDetailViewState
import com.khuram.notes.framework.presentation.notedetail.state.NoteInteractionState
import com.khuram.notes.framework.presentation.notelist.NOTE_PENDING_DELETE_BUNDLE_KEY
import com.khuram.notes.R
import com.khuram.notes.business.domain.state.*
import com.khuram.notes.databinding.FragmentNoteDetailBinding
import com.khuram.notes.databinding.LayoutNoteDetailToolbarBinding
import com.khuram.notes.framework.presentation.common.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


const val NOTE_DETAIL_STATE_BUNDLE_KEY = "com.khuram.notes.framework.presentation.notedetail.state"

@FlowPreview
@ExperimentalCoroutinesApi
class NoteDetailFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseNoteFragment(R.layout.fragment_note_detail) {

    val viewModel: NoteDetailViewModel by viewModels {
        viewModelFactory
    }

    private var _binding: FragmentNoteDetailBinding? = null
    private val binding get() = _binding!!

    private var _toolbarBinding: LayoutNoteDetailToolbarBinding? = null
    private val toolbarBinding get() = _toolbarBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteDetailBinding.inflate(inflater, container, false)
        _toolbarBinding = binding.noteDetailToolbar

        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupOnBackPressDispatcher()
        subscribeObservers()

        binding.noteTitle.setOnClickListener {
            onClick_noteTitle()
        }

        binding.noteBody.setOnClickListener {
            onClick_noteBody()
        }

        getSelectedNoteFromPreviousFragment()
        restoreInstanceState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onErrorRetrievingNoteFromPreviousFragment(){
        viewModel.setStateEvent(
            NoteDetailStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = NOTE_DETAIL_ERROR_RETRIEVING_SELECTED_NOTE,
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                )
            )
        )
    }

    private fun onClick_noteTitle(){
        if(!viewModel.isEditingTitle()){
            updateBodyInViewModel()
            updateNote()
            viewModel.setNoteInteractionTitleState(NoteInteractionState.EditState)
        }
    }

    private fun onClick_noteBody(){
        if(!viewModel.isEditingBody()){
            updateTitleInViewModel()
            updateNote()
            viewModel.setNoteInteractionBodyState(NoteInteractionState.EditState)
        }
    }

    private fun onBackPressed() {
        view?.hideKeyboard()
        if(viewModel.checkEditState()){
            updateBodyInViewModel()
            updateTitleInViewModel()
            updateNote()
            viewModel.exitEditState()
            displayDefaultToolbar()
        }
        else{
            findNavController().popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        updateTitleInViewModel()
        updateBodyInViewModel()
        updateNote()
    }

    private fun subscribeObservers(){

        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->

            if (viewState != null) {
                viewState.note?.let { note ->
                    setNoteTitle(note.title)
                    setNoteBody(note.body)
                }
            }
        }

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner) {
            uiController.displayProgressBar(it)
        }

        viewModel.stateMessage.observe(viewLifecycleOwner) { stateMessage ->

            stateMessage?.response?.let { response ->

                when (response.message) {

                    UPDATE_NOTE_SUCCESS -> {
                        viewModel.setIsUpdatePending(false)
                        viewModel.clearStateMessage()
                    }

                    DELETE_NOTE_SUCCESS -> {
                        viewModel.clearStateMessage()
                        onDeleteSuccess()
                    }

                    else -> {
                        uiController.onResponseReceived(
                            response = stateMessage.response,
                            stateMessageCallback = object : StateMessageCallback {
                                override fun removeMessageFromStack() {
                                    viewModel.clearStateMessage()
                                }
                            }
                        )
                        when (response.message) {

                            UPDATE_NOTE_FAILED_PK -> {
                                findNavController().popBackStack()
                            }

                            NOTE_DETAIL_ERROR_RETRIEVING_SELECTED_NOTE -> {
                                findNavController().popBackStack()
                            }

                            else -> { }
                        }
                    }
                }
            }
        }

        viewModel.collapsingToolbarState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is CollapsingToolbarState.Expanded -> {
                    transitionToExpandedMode()
                }
                is CollapsingToolbarState.Collapsed -> {
                    transitionToCollapsedMode()
                }
            }
        }

        viewModel.noteTitleInteractionState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is NoteInteractionState.EditState -> {
                    binding.noteTitle.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }
                is NoteInteractionState.DefaultState -> {
                    binding.noteTitle.disableContentInteraction()
                }
            }
        }

        viewModel.noteBodyInteractionState.observe(viewLifecycleOwner) { state ->

            when (state) {
                is NoteInteractionState.EditState -> {
                    binding.noteBody.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }
                is NoteInteractionState.DefaultState -> {
                    binding.noteBody.disableContentInteraction()
                }
            }
        }
    }

    private fun displayDefaultToolbar(){

        activity?.let { a ->
            toolbarBinding.toolbarPrimaryIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_arrow_back_grey_24dp,
                    a.application.theme
                )
            )
            toolbarBinding.toolbarSecondaryIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_delete,
                    a.application.theme
                )
            )
        }
    }

    private fun displayEditStateToolbar(){
        activity?.let { a ->
            toolbarBinding.toolbarPrimaryIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_close_grey_24dp,
                    a.application.theme
                )
            )
            toolbarBinding.toolbarSecondaryIcon.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_done_grey_24dp,
                    a.application.theme
                )
            )
        }
    }

    private fun setNoteTitle(title: String) {
        binding.noteTitle.setText(title)
    }

    private fun getNoteTitle(): String{
        return binding.noteTitle.text.toString()
    }

    private fun getNoteBody(): String{
        return binding.noteBody.text.toString()
    }

    private fun setNoteBody(body: String?){
        binding.noteBody.setText(body)
    }

    private fun getSelectedNoteFromPreviousFragment(){
        arguments?.let { args ->
            (args.getParcelable(NOTE_DETAIL_SELECTED_NOTE_BUNDLE_KEY) as Note?)?.let { selectedNote ->
                viewModel.setNote(selectedNote)
            }?: onErrorRetrievingNoteFromPreviousFragment()
        }
    }

    private fun restoreInstanceState(){
        arguments?.let { args ->
            (args.getParcelable(NOTE_DETAIL_STATE_BUNDLE_KEY) as NoteDetailViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)

                // One-time check after rotation
                if(viewModel.isToolbarCollapsed()){
                    binding.appBar.setExpanded(false)
                    transitionToCollapsedMode()
                }
                else{
                    binding.appBar.setExpanded(true)
                    transitionToExpandedMode()
                }
            }
        }
    }

    private fun updateTitleInViewModel(){
        if(viewModel.isEditingTitle()){
            viewModel.updateNoteTitle(getNoteTitle())
        }
    }

    private fun updateBodyInViewModel(){
        if(viewModel.isEditingBody()){
            viewModel.updateNoteBody(getNoteBody())
        }
    }

    private fun setupUI(){
        binding.noteTitle.disableContentInteraction()
        binding.noteBody.disableContentInteraction()
        displayDefaultToolbar()
        transitionToExpandedMode()

        binding.appBar.addOnOffsetChangedListener { _, offset ->

            if (offset < COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD) {
                updateTitleInViewModel()
                if (viewModel.isEditingTitle()) {
                    viewModel.exitEditState()
                    displayDefaultToolbar()
                    updateNote()
                }
                viewModel.setCollapsingToolbarState(CollapsingToolbarState.Collapsed)
            } else {
                viewModel.setCollapsingToolbarState(CollapsingToolbarState.Expanded)
            }
        }

        toolbarBinding.toolbarPrimaryIcon.setOnClickListener {
            if(viewModel.checkEditState()){
                view?.hideKeyboard()
                viewModel.triggerNoteObservers()
                viewModel.exitEditState()
                displayDefaultToolbar()
            }
            else{
                onBackPressed()
            }
        }

        toolbarBinding.toolbarSecondaryIcon.setOnClickListener {
            if(viewModel.checkEditState()){
                view?.hideKeyboard()
                updateTitleInViewModel()
                updateBodyInViewModel()
                updateNote()
                viewModel.exitEditState()
                displayDefaultToolbar()
            }
            else{
                deleteNote()
            }
        }
    }

    private fun deleteNote(){
        viewModel.setStateEvent(
            NoteDetailStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.getNote()?.let { note ->
                                        initiateDeleteTransaction(note)
                                    }
                                }
                                override fun cancel() {
                                    // do nothing
                                }
                            }
                        ),
                        messageType = MessageType.Info
                    )
                )
            )
        )
    }

    private fun initiateDeleteTransaction(note: Note){
        viewModel.beginPendingDelete(note)
    }

    private fun onDeleteSuccess(){
        val bundle = bundleOf(NOTE_PENDING_DELETE_BUNDLE_KEY to viewModel.getNote())
        viewModel.setNote(null) // clear note from ViewState
        viewModel.setIsUpdatePending(false) // prevent update onPause
        findNavController().navigate(
            R.id.action_note_detail_fragment_to_noteListFragment,
            bundle
        )
    }

    private fun setupOnBackPressDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun updateNote() {
        if(viewModel.getIsUpdatePending()){
            viewModel.setStateEvent(
                NoteDetailStateEvent.UpdateNoteEvent
            )
        }
    }

    private fun transitionToCollapsedMode() {
        binding.noteTitle.fadeOut()
        displayToolbarTitle(toolbarBinding.toolBarTitle, getNoteTitle(), true)
    }

    private fun transitionToExpandedMode() {
        binding.noteTitle.fadeIn()
        displayToolbarTitle(toolbarBinding.toolBarTitle, null, true)
    }

    override fun inject() { }

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.getCurrentViewStateOrNew()
        outState.putParcelable(NOTE_DETAIL_STATE_BUNDLE_KEY, viewState)
        super.onSaveInstanceState(outState)
    }
}
