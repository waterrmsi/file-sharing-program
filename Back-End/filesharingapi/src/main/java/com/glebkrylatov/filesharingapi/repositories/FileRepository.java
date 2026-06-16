package com.glebkrylatov.filesharingapi.repositories;

import com.glebkrylatov.filesharingapi.models.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {
    public Optional<File> getFileById(UUID fileId);
    public List<File> getFilesByOwnerId(String ownerId);
}
