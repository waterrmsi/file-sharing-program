package com.glebkrylatov.filesharingapi.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.naming.Name;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "files", schema = "public")
@Getter
@Setter
public class File {
    @Id
    @Column(name = "file_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_user_id")
    private String ownerId;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "bucket")
    private String bucket;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size")
    private Long size;

    @Column(name = "created_at")
    Timestamp createdAt;

    @Column(name = "is_public")
    private boolean isPublic;
}
