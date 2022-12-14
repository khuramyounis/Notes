package com.khuram.notes.framework.datasource.cache.mappers

import com.khuram.notes.business.domain.model.Note
import com.khuram.notes.business.domain.util.DateUtil
import com.khuram.notes.business.domain.util.EntityMapper
import com.khuram.notes.framework.datasource.cache.model.NoteCacheEntity
import javax.inject.Inject


/**
 * Maps Note to NoteCacheEntity or NoteCacheEntity to Note.
 */
class CacheMapper
@Inject
constructor(
    private val dateUtil: DateUtil
): EntityMapper<NoteCacheEntity, Note> {

    fun entityListToNoteList(entities: List<NoteCacheEntity>): List<Note>{
        val list: ArrayList<Note> = ArrayList()
        for(entity in entities){
            list.add(mapFromEntity(entity))
        }
        return list
    }

    fun noteListToEntityList(notes: List<Note>): List<NoteCacheEntity>{
        val entities: ArrayList<NoteCacheEntity> = ArrayList()
        for(note in notes){
            entities.add(mapToEntity(note))
        }
        return entities
    }

    override fun mapFromEntity(entity: NoteCacheEntity): Note {
        return Note(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            updated_at = entity.updated_at,
            created_at = entity.created_at
        )
    }

    override fun mapToEntity(domainModel: Note): NoteCacheEntity {
        return NoteCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            updated_at = domainModel.updated_at,
            created_at = domainModel.created_at
        )
    }
}