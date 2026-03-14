package com.lowcarbon.platform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "mall_items")
public class MallItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(nullable = false)
    private Integer pointsCost;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Boolean enabled = true;
}
