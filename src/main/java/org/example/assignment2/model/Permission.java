package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permission")
public class Permission {
    @EmbeddedId
    private PermissionId id;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Setting role;

    @ManyToOne
    @MapsId("pageId")
    @JoinColumn(name = "page_id")
    private Setting page;

    @Column(name = "can_read")
    private Boolean canRead;

    @Column(name = "can_add")
    private Boolean canAdd;

    @Column(name = "can_edit")
    private Boolean canEdit;

    @Column(name = "can_delete")
    private Boolean canDelete;
}
