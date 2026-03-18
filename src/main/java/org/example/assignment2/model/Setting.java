package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "setting")
@Data
public class Setting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_id")
    private Integer typeId;

    // Mapping để HIỂN THỊ tên cha (Object)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private Setting parent;

    @Column(name = "name")
    private String name;

    @Column(name = "value")
    private String value;

    @Column(name = "order_index")
    private Integer orderIndex;

    @Column(name = "status")
    private String status;

    @Column(name = "description")
    private String description;
}