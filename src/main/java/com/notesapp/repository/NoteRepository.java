package com.notesapp.repository;

import com.notesapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("SELECT n FROM Note n WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword%")
    List<Note> findByTitleContainingOrContentContaining(@Param("keyword") String keyword);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t.name = :tagName")
    List<Note> findByTagsName(@Param("tagName") String tagName);

    @Query("SELECT n FROM Note n ORDER BY n.updatedAt DESC")
    List<Note> findAllOrderByUpdatedAtDesc();

    @Query("SELECT n FROM Note n ORDER BY n.createdAt DESC")
    List<Note> findAllOrderByCreatedAtDesc();
}
