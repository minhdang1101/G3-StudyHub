package org.example.assignment2.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String description;

    private String thumbnail;

    private Double price;

    private Double salePrice;

    private Integer duration;

    // liên kết category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}