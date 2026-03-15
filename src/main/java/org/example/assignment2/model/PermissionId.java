package org.example.assignment2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.Data;

@Data
@Embeddable
public class PermissionId implements Serializable {
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "page_id")
    private Integer pageId;

}
