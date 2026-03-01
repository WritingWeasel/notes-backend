package com.notesapp.service;

import com.notesapp.model.Note;
import com.notesapp.model.Tag;
import com.notesapp.repository.NoteRepository;
import com.notesapp.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    public List<Note> getAllNotes() {
        return noteRepository.findAllOrderByUpdatedAtDesc();
    }

    public Optional<Note> getNoteById(Long id) {
        return noteRepository.findById(id);
    }

    public Note createNote(Note note) {
        processNoteTags(note);
        return noteRepository.save(note);
    }

    @Transactional
    public Note updateNote(Long id, Note noteDetails) {
        return noteRepository.findById(id)
                .map(existingNote -> {
                    existingNote.setTitle(noteDetails.getTitle());
                    existingNote.setContent(noteDetails.getContent());

                    // Clear existing tags
                    existingNote.getTags().clear();

                    // Process tags
                    if (noteDetails.getTags() != null && !noteDetails.getTags().isEmpty()) {
                        Set<Tag> managedTags = noteDetails.getTags().stream()
                                .map(tag -> {
                                    if (tag.getId() != null) {
                                        // Existing tag - fetch managed entity
                                        return tagRepository.findById(tag.getId())
                                                .orElseThrow(() -> new RuntimeException("Tag not found: " + tag.getId()));
                                    } else {
                                        // New tag - check if exists by name, otherwise create
                                        return tagRepository.findByName(tag.getName())
                                                .orElseGet(() -> {
                                                    Tag newTag = new Tag(tag.getName());
                                                    return tagRepository.save(newTag);
                                                });
                                    }
                                })
                                .collect(Collectors.toSet());

                        existingNote.setTags(managedTags);
                    }

                    return noteRepository.save(existingNote);
                })
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
    }



    public void deleteNote(Long id) {
        noteRepository.deleteById(id);
    }

    public List<Note> searchNotes(String keyword) {
        return noteRepository.findByTitleContainingOrContentContaining(keyword);
    }

    public List<Note> getNotesByTag(String tagName) {
        return noteRepository.findByTagsName(tagName);
    }

    private void processNoteTags(Note note) {
        if (note.getTags() == null || note.getTags().isEmpty()) {
            note.setTags(new HashSet<>());
            return;
        }

        Set<Tag> managedTags = note.getTags().stream()
                .map(tag -> {
                    if (tag.getId() != null) {
                        // For existing tags, fetch the managed entity from database
                        return tagRepository.findById(tag.getId())
                                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tag.getId()));
                    } else if (tag.getName() != null && !tag.getName().trim().isEmpty()) {
                        // For new tags, check if exists by name, otherwise create new
                        return tagRepository.findByName(tag.getName().trim())
                                .orElseGet(() -> {
                                    Tag newTag = new Tag(tag.getName().trim());
                                    return tagRepository.save(newTag);
                                });
                    } else {
                        throw new RuntimeException("Invalid tag: must have either ID or name");
                    }
                })
                .collect(Collectors.toSet());

        note.setTags(managedTags);
    }

}
